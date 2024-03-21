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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import io.github.bitterfox.json.string.template.core.JsonAST.JASTArray;
import io.github.bitterfox.json.string.template.core.JsonAST.JASTJavaObject;
import io.github.bitterfox.json.string.template.core.JsonAST.JASTNumberString;
import io.github.bitterfox.json.string.template.core.JsonAST.JASTObject;
import io.github.bitterfox.json.string.template.core.JsonAST.JASTString;
import io.github.bitterfox.json.string.template.core.JsonPosition.ValuePosition;
import io.github.bitterfox.json.string.template.core.JsonToken.JTArrayClose;
import io.github.bitterfox.json.string.template.core.JsonToken.JTArrayOpen;
import io.github.bitterfox.json.string.template.core.JsonToken.JTFalse;
import io.github.bitterfox.json.string.template.core.JsonToken.JTJavaObject;
import io.github.bitterfox.json.string.template.core.JsonToken.JTNull;
import io.github.bitterfox.json.string.template.core.JsonToken.JTNumber;
import io.github.bitterfox.json.string.template.core.JsonToken.JTObjectClose;
import io.github.bitterfox.json.string.template.core.JsonToken.JTObjectOpen;
import io.github.bitterfox.json.string.template.core.JsonToken.JTString;
import io.github.bitterfox.json.string.template.core.JsonToken.JTTrue;

public class JsonParserV2 {
    private final JsonTokenizer tokenizer;

    public JsonParserV2(JsonTokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public JsonAST parseJson() {
        return parseValue();
    }

    private JsonAST parseValue() {
        return switch (tokenizer.peek()) {
            case JTObjectOpen _ -> parseObject();
            case JTArrayOpen _ -> parseArray();
            case JTString _, JTNumber _, JTTrue _, JTFalse _, JTNull _, JTJavaObject _ -> parseLiteral();
            case JsonToken it -> throw new IllegalStateException(STR."Unexpected token \{it}");
        };
    }

    private JsonAST parseObject() {
        accept(JsonToken.OBJECT_OPEN);

        List<Entry<JsonAST, JsonAST>> fields = new ArrayList<>();
        while (!(tokenizer.peek() instanceof JTObjectClose)) {
            JsonAST key = parseString();
            accept(JsonToken.COLON);
            JsonAST value = parseValue();
            fields.add(Map.entry(key, value));

            if (tokenizer.peek() instanceof JTObjectClose) {
                // do nothing
            } else {
                accept(JsonToken.COMMA);
            }
        }

        accept(JsonToken.OBJECT_CLOSE);
        return new JASTObject(fields);
    }

    private JsonAST parseArray() {
        accept(JsonToken.ARRAY_OPEN);

        List<JsonAST> values = new ArrayList<>();
        while (!(tokenizer.peek() instanceof JTArrayClose)) {
            values.add(parseValue());

            if (tokenizer.peek() instanceof JTArrayClose) {
                // do nothing
            } else {
                accept(JsonToken.COMMA);
            }
        }

        accept(JsonToken.ARRAY_CLOSE);
        return new JASTArray(values);
    }

    private JsonAST parseString() {
        JsonToken token = tokenizer.next();
        return switch (token) {
            case JTString(var fragments, var values, JsonPositionRange pos) -> new JASTString(fragments, pos);
            case JTJavaObject(_, ValuePosition pos) -> new JASTJavaObject(pos);
            default -> throw new IllegalStateException(STR."Unexpected token \{token}");
        };
    }

    private JsonAST parseLiteral() {
        JsonToken token = tokenizer.next();
        return switch (token) {
            case JTString(var fragments, var values, JsonPositionRange pos) -> new JASTString(fragments, pos);
            case JTNumber(String number, _) -> new JASTNumberString(number);
            case JTTrue _ -> JsonAST.TRUE;
            case JTFalse _ -> JsonAST.FALSE;
            case JTNull _ -> JsonAST.NULL;
            case JTJavaObject(_, ValuePosition pos) -> new JASTJavaObject(pos);
            default -> throw new IllegalStateException(STR."Unexpected token \{token}");
        };
    }

    private void accept(JsonToken expect) {
        JsonToken actual = tokenizer.next();
        if (!expect.equals(actual)) {
            throw new IllegalStateException(STR."Unexpected token \{actual}, expected \{expect}");
        }
    }
}
