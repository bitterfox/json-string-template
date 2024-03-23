/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package io.github.bitterfox.json.string.template.core;

import java.util.List;
import java.util.stream.Collectors;

import io.github.bitterfox.json.string.template.core.JsonPosition.FragmentPosition;
import io.github.bitterfox.json.string.template.core.JsonPosition.ValuePosition;

public class JsonStringTemplatePrinter {
    @VisibleForTesting
    char expressionEscapeChar = '\\';

    List<List<String>> lines(StringTemplate template) {
        return template.fragments().stream()
                       .map(l -> l + " ") // add dummy space to keep last empty line
                       .map(String::lines)
                       .map(s -> s.collect(Collectors.toList()))
                       .peek(l -> {
                           String lastLine = l.removeLast();
                           l.addLast(lastLine.substring(0, lastLine.length() - 1)); // remove dummy space
                       })
                       .toList();
    }

    String print(StringTemplate template, JsonPosition highlight, boolean stopAtHighlight) {
        return print(template, new JsonPositionRange(highlight, highlight), stopAtHighlight);
    }
    String print(StringTemplate template, JsonPositionRange highlight, boolean stopAtHighlight) {
        List<List<String>> lines = lines(template);
        StringBuilder builder = new StringBuilder();
        StringBuilder highlightLine = new StringBuilder();
        boolean highlighted = false;

        for (int i = 0; i < lines.size(); i++) {
            if (i != 0) {
                int valueIndex = i - 1;
                String valueExpression = STR."\{expressionEscapeChar}{\{valueIndex }}";
                builder.append(valueExpression);
                if (inRange(highlight, valueIndex + 1, -1)) {
                    highlightLine.append("^".repeat(valueExpression.length()));
                    highlighted = true;
                } else {
                    highlightLine.append(" ".repeat(valueExpression.length()));
                }
            }

            int lineStartCursor = 0;
            for (String line : lines.get(i)) {
                if (lineStartCursor != 0) {
                    builder.append(System.lineSeparator());
                    if (inRange(highlight, i, lineStartCursor - 1)) {
                        highlightLine.append("^");
                        highlighted = true;
                    }
                    if (highlighted) {
                        highlighted = false;
                        builder.append(highlightLine.toString().replaceFirst("(\s)+$", ""))
                                .append(System.lineSeparator());
                        if (stopAtHighlight && !inRange(highlight, i, lineStartCursor)) {
                            return builder.toString();
                        }
                    }
                    highlightLine = new StringBuilder();
                }

                builder.append(escape(line));

                int lineEndCursor = lineStartCursor + line.length();
                for (int j = lineStartCursor; j < lineEndCursor; j++) {
                    if (inRange(highlight, i, j)) {
                        highlightLine.append("^".repeat(charSize(line.charAt(j - lineStartCursor))));
                        highlighted = true;
                    } else {
                        highlightLine.append(" ");
                    }
                }

                lineStartCursor = lineEndCursor + 1;
            }
        }

        if (highlighted) {
            builder.append(System.lineSeparator())
                   .append(highlightLine.toString().replaceFirst("(\s)+$", ""))
                   .append(System.lineSeparator());
        }
        return builder.toString();
    }

    boolean inRange(JsonPositionRange range, int index, int cursor) {
        if (fixRange(range) instanceof JsonPositionRange(
                FragmentPosition(int startIndex, int startCursor),
                FragmentPosition(int endIndex, int endCursor))) {
            if (startIndex <= index && index <= endIndex) {
                if (startIndex == endIndex && endIndex == index) {
                    return startCursor <= cursor && cursor <= endCursor;
                } else if (startIndex == index) {
                    return startCursor <= cursor;
                } else if (endIndex == index) {
                    return cursor <= endCursor;
                } else {
                    return true;
                }
            }
        }

        return false;
    }

    JsonPositionRange fixRange(JsonPositionRange range) {
        JsonPosition start = range.startInclusive();
        if (start instanceof ValuePosition(int index)) {
            start = new FragmentPosition(index + 1, -1);
        }

        JsonPosition end = range.endInclusive();
        if (end instanceof ValuePosition(int index)) {
            end = new FragmentPosition(index + 1, -1);
        }

        return new JsonPositionRange(start, end);
    }

    String escape(String line) {
        return line.replace("\t", "\\t");
    }

    int charSize(char ch) {
        // TODO Support 2-width chars like Japanese
        String escaped = escape(String.valueOf(ch));
        return escaped.length();
    }
}
