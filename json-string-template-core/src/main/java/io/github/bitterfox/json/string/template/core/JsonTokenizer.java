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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.github.bitterfox.json.string.template.core.JsonCharacter.JCCh;
import io.github.bitterfox.json.string.template.core.JsonCharacter.JCObj;
import io.github.bitterfox.json.string.template.core.JsonCharacter.JCWhitespace;
import io.github.bitterfox.json.string.template.core.JsonStringTemplateException.Phase;
import io.github.bitterfox.json.string.template.core.JsonToken.JTJavaObject;
import io.github.bitterfox.json.string.template.core.JsonToken.JTNumber;
import io.github.bitterfox.json.string.template.core.JsonToken.JTString;

public class JsonTokenizer implements Iterator<JsonToken> {
    private JsonCharacterIterator iterator;
    private JsonToken next;
    private final JsonStringTemplateConfiguration config;

    private JsonTokenizer(JsonCharacterIterator iterator, JsonStringTemplateConfiguration config) {
        this.iterator = iterator;
        this.config = config;
        readNext();
    }


    public JsonTokenizer(StringTemplate template, JsonStringTemplateConfiguration config) {
        this(new JsonCharacterIterator(template, config), config);
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public JsonToken next() {
        var n = next;
        readNext();
        return n;
    }

    public JsonToken peek() {
        return next;
    }

    private void readNext() {
        skipMeaninglessChars();

        if (!iterator.hasNext()) {
            next = null;
            return;
        }

        next = switch (iterator.peek()) {
            case JCCh(char ch, var pos) -> switch (ch) {
                case '{' -> read(JsonToken.OBJECT_OPEN);
                case '}' -> read(JsonToken.OBJECT_CLOSE);
                case '[' -> read(JsonToken.ARRAY_OPEN);
                case ']' -> read(JsonToken.ARRAY_CLOSE);
                case ',' -> read(JsonToken.COMMA);
                case ':' -> read(JsonToken.COLON);
                case '"' -> readString('"');
                // can be true, false or null
                case 't', 'f', 'n' -> readLiteral();
                case '-' -> readNumber();
                default -> {
                    if (config.singleQuoteForStringSeparatorAllowed() && ch == '\'') {
                        yield readString('\'');
                    }

                    if (Character.isDigit(ch)) {
                        yield readNumber();
                    } else {
                        throw new JsonStringTemplateException(STR."Unexpected char \{ch}", Phase.LEXER_TOKENIZER, iterator.getTemplate(), pos);
                    }
                }
            };
            case JCObj(Object o, var pos) -> read(new JTJavaObject(o, pos));
            case JCWhitespace _ -> throw new IllegalStateException("Unexpected branch");
        };
    }

    private JTString readString(char stringSeparator) {
        List<String> fragments = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        var startPos = accept(stringSeparator).pos();
        while (iterator.hasNext()) {
            JsonCharacter jch = iterator.next();
            switch (jch) {
                case JCCh(char ch, _) -> {
                    if (ch == '\\') {
                        // TODO Use substring
                        sb.append(readEscape(stringSeparator));
                    } else if (ch == stringSeparator) {
                        fragments.add(sb.toString());
                        return new JTString(fragments, values, new JsonPositionRange(startPos, jch.pos()));
                    } else {
                        sb.append(ch);
                    }
                }
                case JCObj(Object obj, _) -> {
                    // TODO Use substring
                    fragments.add(sb.toString());
                    sb = new StringBuilder();
                    values.add(obj);
                }
                case JCWhitespace(char ch, _) -> sb.append(ch);
            }
        }
        throw new IllegalStateException(STR."String is not closed \{sb}");
    }

    private char readEscape(char stringSeparator) {
        return switch (iterator.next()) {
            case JCCh(char ch, _) when ch == stringSeparator -> ch;
            case JCCh(char ch, _) ->
                switch (ch) {
                    case '\\', '/' -> ch;
                    case 'b' -> '\b'; // BS (backspace) 8
                    case 'f' -> '\f'; // FF (NP form feed) 12
                    case 'n' -> '\n'; // LF (NL line feed) 10
                    case 'r' -> '\r'; // CR (carriage return) 13
                    case 't' -> '\t'; // TAB (horizontal tab) 9
                    case 'u' -> readEscapedHex();
                    default -> throw new IllegalStateException(STR."Unknown escape char \\\{ch}");
                };
            case JsonCharacter it -> throw new IllegalStateException(STR."Expected escaped char, but \{it} is not escaped char");
        };
    }

    private char readEscapedHex() {
        int hex = readHex();
        hex = hex * 16 + readHex();
        hex = hex * 16 + readHex();
        hex = hex * 16 + readHex();

        if (hex > 0xFFFF) {
            // Should not happen
            throw new IllegalStateException(STR."Hex char exceeds limit, \{hex} > \{0xFFFF}");
        }

        return (char) hex;
    }
    private int readHex() {
        return switch (iterator.next()) {
            case JCCh(char ch, _) -> readHex(ch);
            case JsonCharacter it -> throw new IllegalStateException(STR."Expected escaped char, but \{it} is not escaped char");
        };
    }

    private int readHex(char ch) {
        if (Character.isDigit(ch)) {
            return ch - '0';
        } else {
            char lower = Character.toLowerCase(ch);
            if ('a' <= lower && lower <= 'f') {
                return 10 + (lower - 'a');
            }
        }

        throw new IllegalStateException(STR."Expected hex char, but \{ch} is not hex char");
    }

    private JsonToken readLiteral() {
        return switch (iterator.peek()) {
            case JCCh(char ch, _) -> switch (ch) {
                case 't' -> accept("true", JsonToken.TRUE);
                case 'f' -> accept("false", JsonToken.FALSE);
                case 'n' -> accept("null", JsonToken.NULL);
                default -> throw new IllegalStateException(STR."Unexpected token json char \{ch}");
            };
            default -> throw new IllegalStateException("Unexpected branch");
        };
    }

    private JsonToken accept(String expected, JsonToken it) {
        for (int i = 0; i < expected.length(); i++) {
            accept(expected.charAt(i));
        }
        return it;
    }

    private JsonCharacter accept(char expected) {
        JsonCharacter jch;
        if ((jch = iterator.next()) instanceof JCCh(char actual, _) && expected == actual) {
            return jch;
        }

        throw new IllegalStateException(
                        STR."Unexpected char: expected \{expected}, but \{jch} found");
    }

    private JsonToken readNumber() {
        StringBuilder number = new StringBuilder();
        JsonPosition start = iterator.peek().pos();
        JsonPosition end = start;

        // -
        switch (iterator.peek()) {
            case JCCh(char ch, _) when ch == '-' -> {
                end = iterator.next().pos();
                number.append(ch);
            }
            case JCCh _ -> {}
            case JsonCharacter it -> throw new IllegalStateException(STR."Unexpected token json char \{it}");
        }

        // 0
        // or digit1-9 {digit}
        if (iterator.peek() instanceof JCCh(char ch, _)) {
            if (ch == '0') {
                end = iterator.next().pos();
                number.append(ch);
            } else if (Character.isDigit(ch)) { // digit 1-9
                end = iterator.next().pos();
                number.append(ch);
                while (iterator.peek() instanceof JCCh(char ch1, _) && Character.isDigit(ch1)) {
                    end = iterator.next().pos();
                    number.append(ch1);
                }
            }
        }

        // fraction
        // [. digit {digit}]
        if (iterator.peek() instanceof JCCh(char ch, _) && ch == '.') {
            end = iterator.next().pos();
            number.append(ch);

            if (iterator.peek() instanceof JCCh(char ch1, _) && Character.isDigit(ch1)) {
                end = iterator.next().pos();
                number.append(ch1);
            } else {
                throw new IllegalStateException(STR."Expected digit after . for fraction, but \{iterator.peek()} found");
            }

            while (iterator.peek() instanceof JCCh(char ch2, _) && Character.isDigit(ch2)) {
                end = iterator.next().pos();
                number.append(ch2);
            }
        }

        // exponent
        // [(E|e) [(-|+)] digit {digit}]
        if (iterator.peek() instanceof JCCh(char ch, _) && (ch == 'E' || ch == 'e')) {
            end = iterator.next().pos();
            number.append(ch);

            if (iterator.peek() instanceof JCCh(char ch1, _) && (ch1 == '+' || ch1 == '-')) {
                end = iterator.next().pos();
                number.append(ch1);
            }

            if (iterator.peek() instanceof JCCh(char ch1, _) && Character.isDigit(ch1)) {
                end = iterator.next().pos();
                number.append(ch1);
            } else {
                throw new IllegalStateException(STR."Expected digit after E/e for exponent, but \{iterator.peek()} found");
            }

            while (iterator.peek() instanceof JCCh(char ch2, _) && Character.isDigit(ch2)) {
                end = iterator.next().pos();
                number.append(ch2);
            }
        }

        return new JTNumber(number.toString(), new JsonPositionRange(start, end));
    }

    private JsonToken read(JsonToken token) {
        iterator.next();
        return token;
    }

    /**
     * Skip whitespace, skip line comment (started with // until line end), block comment (started with /*, ended with *\/) if allowed
     */
    private void skipMeaninglessChars() {
        while (iterator.peek() instanceof JCWhitespace _) {
            iterator.next();
        }

        if (config.commentAllowed() && iterator.peek() instanceof JCCh(char ch, _) && ch == '/') {
            iterator.next();

            switch (iterator.peek()) {
                case JCCh(char ch1, _) when ch1 == '/' -> {
                    iterator.next();
                    while (iterator.hasNext()) {
                        if (iterator.next() instanceof JCWhitespace(char ch2, _) && ch2 == '\n') {
                            break;
                        }
                    }
                    skipMeaninglessChars();
                }
                case JCCh(char ch1, _) when ch1 == '*' -> {
                    iterator.next();
                    skipUntilBlockCommentEnd();
                    skipMeaninglessChars();
                }
                default -> {}
            }
        }
    }

    private void skipUntilBlockCommentEnd() {
        while (iterator.hasNext()) {
            if (iterator.next() instanceof JCCh(char ch, _) && ch == '*') {
                if (iterator.peek() instanceof JCCh(char ch1, _) && ch1 == '/') {
                    iterator.next();
                    break;
                }
            }
        }
    }
}
