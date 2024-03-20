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

import java.util.Iterator;

import io.github.bitterfox.json.string.template.core.JsonCharacter.JCCh;
import io.github.bitterfox.json.string.template.core.JsonCharacter.JCObj;
import io.github.bitterfox.json.string.template.core.JsonCharacter.JCWhitespace;
import io.github.bitterfox.json.string.template.core.JsonToken.JTArrayClose;
import io.github.bitterfox.json.string.template.core.JsonToken.JTArrayOpen;
import io.github.bitterfox.json.string.template.core.JsonToken.JTColon;
import io.github.bitterfox.json.string.template.core.JsonToken.JTComma;
import io.github.bitterfox.json.string.template.core.JsonToken.JTFalse;
import io.github.bitterfox.json.string.template.core.JsonToken.JTJavaObject;
import io.github.bitterfox.json.string.template.core.JsonToken.JTNull;
import io.github.bitterfox.json.string.template.core.JsonToken.JTObjectClose;
import io.github.bitterfox.json.string.template.core.JsonToken.JTObjectOpen;
import io.github.bitterfox.json.string.template.core.JsonToken.JTString;
import io.github.bitterfox.json.string.template.core.JsonToken.JTTrue;

public class JsonTokenizer implements Iterator<JsonToken> {
    private JsonCharacterIterator iterator;
    private JsonToken next;

    private JsonTokenizer(JsonCharacterIterator iterator) {
        this.iterator = iterator;
        readNext();
    }


    public JsonTokenizer(StringTemplate template) {
        this(new JsonCharacterIterator(template));
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
        skipWhitespace();

        if (!iterator.hasNext()) {
            next = null;
            return;
        }

        next = switch (iterator.peek()) {
            case JCCh(char ch, _) -> switch (ch) {
                case '{' -> read(JsonToken.OBJECT_OPEN);
                case '}' -> read(JsonToken.OBJECT_CLOSE);
                case '[' -> read(JsonToken.ARRAY_OPEN);
                case ']' -> read(JsonToken.ARRAY_CLOSE);
                case ',' -> read(JsonToken.COMMA);
                case ':' -> read(JsonToken.COLON);
                case '"' -> readString();
                // can be true, false or null
                case 't', 'f', 'n' -> readLiteral();
                case '-' -> readNumber();
                default -> {
                    if (Character.isDigit(ch)) {
                        yield readNumber();
                    } else {
                        throw new IllegalStateException(STR."unexpected token json char \{ch}");
                    }
                }
            };
            case JCObj(Object o, _) -> read(new JTJavaObject(o));
            case JCWhitespace _ -> throw new IllegalStateException("Unexpected branch");
        };
    }

    private JTString readString() {
        StringBuilder sb = new StringBuilder();

        accept('"');
        while (iterator.hasNext()) {
            JsonCharacter jch = iterator.next();
            switch (jch) {
                case JCCh(char ch, _) -> {
                    if (ch == '\\') {
                        sb.append(readEscape());
                    } else if (ch == '"') {
                        return new JTString(sb.toString());
                    } else {
                        sb.append(ch);
                    }
                }
                case JCObj(Object obj, _) -> sb.append(obj);
                case JCWhitespace(char ch, _) -> sb.append(ch);
            }
        }
        throw new IllegalStateException(STR."String is not closed \{sb}");
    }

    private char readEscape() {
        return switch (iterator.next()) {
            case JCCh(char ch, _) ->
                switch (ch) {
                    case '"', '\\', '/' -> ch;
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

    private void accept(char expected) {
        JsonCharacter jch;
        if ((jch = iterator.next()) instanceof JCCh(char actual, _) && expected == actual) {
            return;
        }

        throw new IllegalStateException(
                        STR."Unexpected char: expected \{expected}, but \{jch} found");
    }

    private JsonToken readNumber() {
        StringBuilder number = new StringBuilder();

        // -
        switch (iterator.peek()) {
            case JCCh(char ch, _) when ch == '-' -> {
                iterator.next();
                number.append(ch);
            }
            case JCCh _ -> {}
            case JsonCharacter it -> throw new IllegalStateException(STR."Unexpected token json char \{it}");
        }

        // 0
        // or digit1-9 {digit}
        if (iterator.peek() instanceof JCCh(char ch, _)) {
            if (ch == '0') {
                iterator.next();
                number.append(ch);
            } else if (Character.isDigit(ch)) { // digit 1-9
                iterator.next();
                number.append(ch);
                while (iterator.peek() instanceof JCCh(char ch1, _) && Character.isDigit(ch1)) {
                    iterator.next();
                    number.append(ch1);
                }
            }
        }

        // fraction
        // [. digit {digit}]
        if (iterator.peek() instanceof JCCh(char ch, _) && ch == '.') {
            iterator.next();
            number.append(ch);

            if (iterator.peek() instanceof JCCh(char ch1, _) && Character.isDigit(ch1)) {
                iterator.next();
                number.append(ch1);
            } else {
                throw new IllegalStateException(STR."Expected digit after . for fraction, but \{iterator.peek()} found");
            }

            while (iterator.peek() instanceof JCCh(char ch2, _) && Character.isDigit(ch2)) {
                iterator.next();
                number.append(ch2);
            }
        }

        // exponent
        // [(E|e) [(-|+)] digit {digit}]
        if (iterator.peek() instanceof JCCh(char ch, _) && (ch == 'E' || ch == 'e')) {
            iterator.next();
            number.append(ch);

            if (iterator.peek() instanceof JCCh(char ch1, _) && (ch1 == '+' || ch1 == '-')) {
                iterator.next();
                number.append(ch1);
            }

            if (iterator.peek() instanceof JCCh(char ch1, _) && Character.isDigit(ch1)) {
                iterator.next();
                number.append(ch1);
            } else {
                throw new IllegalStateException(STR."Expected digit after E/e for exponent, but \{iterator.peek()} found");
            }

            while (iterator.peek() instanceof JCCh(char ch2, _) && Character.isDigit(ch2)) {
                iterator.next();
                number.append(ch2);
            }
        }

        return new JsonToken.JTNumber(number.toString());
    }

    private JsonToken read(JsonToken token) {
        iterator.next();
        return token;
    }

    private void skipWhitespace() {
        while (iterator.peek() instanceof JCWhitespace _) {
            iterator.next();
        }
    }
}
