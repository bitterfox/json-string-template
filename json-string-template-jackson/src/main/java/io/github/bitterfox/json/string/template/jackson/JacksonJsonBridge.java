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

package io.github.bitterfox.json.string.template.jackson;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.bitterfox.json.string.template.core.JsonBridge;

public class JacksonJsonBridge implements JsonBridge<JsonNode> {
    private final ObjectMapper mapper;

    public JacksonJsonBridge(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public JsonNode createObject(Map<String, JsonNode> object) {
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        object.forEach(json::put);
        return json;
    }

    @Override
    public JsonNode createArray(List<JsonNode> array) {
        ArrayNode json = JsonNodeFactory.instance.arrayNode(array.size());
        json.addAll(array);
        return json;
    }

    @Override
    public JsonNode createString(String string) {
        return JsonNodeFactory.instance.textNode(string);
    }

    @Override
    public JsonNode createNumber(String number) {
        return JsonNodeFactory.instance.numberNode(new BigDecimal(number));
    }

    @Override
    public JsonNode createNumber(Number number) {
        return switch (number) {
            case Byte n -> JsonNodeFactory.instance.numberNode(n);
            case Short n -> JsonNodeFactory.instance.numberNode(n);
            case Integer n -> JsonNodeFactory.instance.numberNode(n);
            case Long n -> JsonNodeFactory.instance.numberNode(n);
            case Float n -> JsonNodeFactory.instance.numberNode(n);
            case Double n -> JsonNodeFactory.instance.numberNode(n);
            case BigInteger n -> JsonNodeFactory.instance.numberNode(n);
            case BigDecimal n -> JsonNodeFactory.instance.numberNode(n);
            default -> throw new IllegalArgumentException(STR."Unsupported number type \{number.getClass()}");
        };
    }

    @Override
    public JsonNode createTrue() {
        return BooleanNode.getTrue();
    }

    @Override
    public JsonNode createFalse() {
        return BooleanNode.getFalse();
    }

    @Override
    public JsonNode createNull() {
        return JsonNodeFactory.instance.nullNode();
    }

    @Override
    public boolean isJsonObject(Object o) {
        return true;
    }

    @Override
    public JsonNode convertToJsonObject(Object o) {
        if (o instanceof JsonNode n) {
            return n;
        }

        ObjectMapper mapper = new ObjectMapper();
        return mapper.valueToTree(o);
    }
}
