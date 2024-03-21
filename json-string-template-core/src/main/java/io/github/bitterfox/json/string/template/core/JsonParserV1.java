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
import java.util.stream.Collectors;

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

public class JsonParserV1<JSON> {
    private final JsonTokenizer tokenizer;
    private final JsonBridge<JSON> jsonBridge;

    public JsonParserV1(JsonTokenizer tokenizer, JsonBridge<JSON> jsonBridge) {
        this.tokenizer = tokenizer;
        this.jsonBridge = jsonBridge;
    }

    public JSON parseJson() {
        return parseValue();
    }

    private JSON parseValue() {
        return switch (tokenizer.peek()) {
            case JTObjectOpen _ -> parseObject();
            case JTArrayOpen _ -> parseArray();
            case JTString _, JTNumber _, JTTrue _, JTFalse _, JTNull _, JTJavaObject _ -> parseLiteral();
            case JsonToken it -> throw new IllegalStateException(STR."Unexpected token \{it}");
        };
    }

    private JSON parseObject() {
        accept(JsonToken.OBJECT_OPEN);

        Map<String, JSON> object = new HashMap<>();
        while (!(tokenizer.peek() instanceof JTObjectClose)) {
            String key = parseString();
            accept(JsonToken.COLON);
            JSON value = parseValue();
            if (object.containsKey(key)) {
                // TODO Make behavior can be defined in JsonBridge
                throw new IllegalStateException(STR."Duplicated \{key}, \{object}");
            }
            object.put(key, value);

            if (tokenizer.peek() instanceof JTObjectClose) {
                // do nothing
            } else {
                accept(JsonToken.COMMA);
            }
        }

        accept(JsonToken.OBJECT_CLOSE);
        return jsonBridge.createObject(object);
    }

    private JSON parseArray() {
        accept(JsonToken.ARRAY_OPEN);

        List<JSON> array = new ArrayList<>();
        while (!(tokenizer.peek() instanceof JTArrayClose)) {
            JSON value = parseValue();
            array.add(value);

            if (tokenizer.peek() instanceof JTArrayClose) {
                // do nothing
            } else {
                accept(JsonToken.COMMA);
            }
        }

        accept(JsonToken.ARRAY_CLOSE);
        return jsonBridge.createArray(array);
    }

    private String parseString() {
        JsonToken token = tokenizer.next();
        return switch (token) {
            case JTString(var fragments, var values, _) -> STR.process(StringTemplate.of(fragments, values));
            case JTJavaObject(Object o, _) -> o.toString(); // or NPE
            default -> throw new IllegalStateException(STR."Unexpected token \{token}");
        };
    }

    private JSON parseLiteral() {
        JsonToken token = tokenizer.next();
        return switch (token) {
            case JTString(var fragments, var values, _) -> jsonBridge.createString(STR.process(StringTemplate.of(fragments, values)));
            case JTNumber(String number, _) -> jsonBridge.createNumber(number);
            case JTTrue _ -> jsonBridge.createTrue();
            case JTFalse _ -> jsonBridge.createFalse();
            case JTNull _ -> jsonBridge.createNull();
            case JTJavaObject(Object o, _) -> convertJavaObject(o);
            default -> throw new IllegalStateException(STR."Unexpected token \{token}");
        };
    }

    private JSON convertJavaObject(Object o) {
        if (jsonBridge.isJsonObject(o)) {
            return jsonBridge.convertToJsonObject(o);
        }

        // Default binding
        if (o == null) {
            return jsonBridge.createNull();
        }

        return switch (o) {
            case Number n -> jsonBridge.createNumber(n);
            case Boolean b -> b ? jsonBridge.createTrue() : jsonBridge.createFalse();
            case Object[] a -> {
                JSON array = jsonBridge.createArray(
                        Arrays.stream(a)
                              .map(this::convertJavaObject)
                              .collect(Collectors.toList()));
                yield array;
            }
            case Collection<?> c -> {
                JSON array = jsonBridge.createArray(
                        c.stream()
                                .map(this::convertJavaObject)
                                .collect(Collectors.toList()));
                yield array;
            }
            case Object _ -> jsonBridge.createString(o.toString());
        };
    }

    private void accept(JsonToken expect) {
        JsonToken actual = tokenizer.next();
        if (!expect.equals(actual)) {
            throw new IllegalStateException(STR."Unexpected token \{actual}, expected \{expect}");
        }
    }
}
