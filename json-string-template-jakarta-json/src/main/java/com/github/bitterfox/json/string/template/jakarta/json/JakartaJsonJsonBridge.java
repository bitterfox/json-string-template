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

package com.github.bitterfox.json.string.template.jakarta.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import com.github.bitterfox.json.string.template.base.JsonBridge;

import jakarta.json.Json;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class JakartaJsonJsonBridge implements JsonBridge<JsonValue> {
    @Override
    public JsonValue createObject(Map<String, JsonValue> object) {
        var builder = Json.createObjectBuilder();
        object.forEach(builder::add);
        return builder.build();
    }

    @Override
    public JsonValue createArray(List<JsonValue> array) {
        var builder = Json.createArrayBuilder();
        array.forEach(builder::add);
        return builder.build();
    }

    @Override
    public JsonValue createString(String string) {
        return Json.createValue(string);
    }

    @Override
    public JsonValue createNumber(String number) {
        return Json.createValue(new BigDecimal(number));
    }

    @Override
    public JsonValue createNumber(Number number) {
        return switch (number) {
            case Integer n -> Json.createValue(n);
            case Long n -> Json.createValue(n);
            case Double n -> Json.createValue(n);
            case BigInteger n -> Json.createValue(n);
            case BigDecimal n -> Json.createValue(n);
            default -> throw new IllegalArgumentException(STR."\{number.getClass()} is not supported in JsonPJsonBridge");
        };
    }

    @Override
    public JsonValue createTrue() {
        return JsonValue.TRUE;
    }

    @Override
    public JsonValue createFalse() {
        return JsonValue.FALSE;
    }

    @Override
    public JsonValue createNull() {
        return JsonValue.NULL;
    }

    @Override
    public boolean isJsonObject(Object o) {
        return o instanceof JsonValue;
    }

    @Override
    public JsonValue convertToJsonObject(Object o) {
        return (JsonValue) o;
    }
}
