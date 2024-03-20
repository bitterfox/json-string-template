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

package com.github.bitterfox.json.string.template.base;

import java.util.Collection;

import com.github.bitterfox.json.string.template.base.JsonToken.JTArrayClose;
import com.github.bitterfox.json.string.template.base.JsonToken.JTArrayOpen;
import com.github.bitterfox.json.string.template.base.JsonToken.JTColon;
import com.github.bitterfox.json.string.template.base.JsonToken.JTComma;
import com.github.bitterfox.json.string.template.base.JsonToken.JTFalse;
import com.github.bitterfox.json.string.template.base.JsonToken.JTJavaObject;
import com.github.bitterfox.json.string.template.base.JsonToken.JTNull;
import com.github.bitterfox.json.string.template.base.JsonToken.JTNumber;
import com.github.bitterfox.json.string.template.base.JsonToken.JTObjectClose;
import com.github.bitterfox.json.string.template.base.JsonToken.JTObjectOpen;
import com.github.bitterfox.json.string.template.base.JsonToken.JTString;
import com.github.bitterfox.json.string.template.base.JsonToken.JTTrue;

public class JsonParser<JSON> {
    private JsonTokenizer tokenizer;
    private JsonBridge<JSON> jsonBridge;

    public JsonParser(JsonTokenizer tokenizer, JsonBridge jsonBridge) {
        this.tokenizer = tokenizer;
        this.jsonBridge = jsonBridge;
    }

    public JSON parseJson() {
        return parseValue();
    }

    private JSON parseValue() {
        return switch (tokenizer.peek()) {
            case JTObjectOpen _ -> parseObject();
            case JTArrayClose _ -> parseArray();
            case JTString _, JTNumber _, JTTrue _, JTFalse _, JTNull _, JTJavaObject _ -> parseLiteral();
            case JsonToken it -> throw new IllegalStateException(STR."Unexpected token \{it}");
        };
    }

    private JSON parseObject() {
        accept(new JTObjectOpen());

        JSON json = jsonBridge.createObject();
        while (!(tokenizer.peek() instanceof JTObjectClose)) {
            JSON key = parseString();
            accept(new JTColon());
            JSON value = parseValue();
            jsonBridge.addToObject(json, key, value);

            if (tokenizer.peek() instanceof JTObjectClose) {
                // do nothing
            } else {
                accept(new JTComma());
            }
        }

        accept(new JTObjectClose());
        return json;
    }

    private JSON parseArray() {
        accept(new JTArrayOpen());

        JSON json = jsonBridge.createArray();
        while (!(tokenizer.peek() instanceof JTArrayClose)) {
            JSON value = parseValue();
            jsonBridge.addToArray(json, value);

            if (tokenizer.peek() instanceof JTArrayClose) {
                // do nothing
            } else {
                accept(new JTComma());
            }
        }

        accept(new JTArrayClose());
        return json;
    }


    private JSON parseString() {
        JsonToken token = tokenizer.next();
        return switch (token) {
            case JTString(String str) -> jsonBridge.createString(str);
            case JTJavaObject(Object o) -> jsonBridge.createString(o.toString()); // or NPE
            default -> throw new IllegalStateException(STR."Unexpected token \{token}");
        };
    }

    private JSON parseLiteral() {
        JsonToken token = tokenizer.next();
        return switch (token) {
            case JTString(String str) -> jsonBridge.createString(str);
            case JTNumber(String number) -> jsonBridge.createNumber(number);
            case JTTrue _ -> jsonBridge.createTrue();
            case JTFalse _ -> jsonBridge.createFalse();
            case JTNull _ -> jsonBridge.createNull();
            case JTJavaObject(Object o) -> convertJavaObject(o);
            default -> throw new IllegalStateException(STR."Unexpected token \{token}");
        };
    }

    private JSON convertJavaObject(Object o) {
        if (jsonBridge.isJsonObject(o)) {
            return jsonBridge.convertToJsonObject(o);
        }

        // Default binding
        if (o == null) {
            jsonBridge.createNull();
        }

        return switch (o) {
            case Number n -> jsonBridge.createNumber(n);
            case Boolean b -> b ? jsonBridge.createTrue() : jsonBridge.createFalse();
            case Collection<?> c -> {
                JSON array = jsonBridge.createArray();
                c.stream()
                 .map(this::convertJavaObject)
                 .forEach(e -> jsonBridge.addToArray(array, e));
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
