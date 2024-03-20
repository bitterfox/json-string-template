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

package com.github.bitterfox.json.string.template.org.json;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import io.github.bitterfox.json.string.template.core.JsonBridge;

public class OrgJsonJsonBridge implements JsonBridge<Object> {
    @Override
    public Object createObject(Map<String, Object> object) {
        return new JSONObject(object);
    }

    @Override
    public Object createArray(List<Object> array) {
        return new JSONArray(array);
    }

    @Override
    public Object createString(String string) {
        return string;
    }

    @Override
    public Object createNumber(String number) {
        return new BigDecimal(number);
    }

    @Override
    public Object createNumber(Number number) {
        return number;
    }

    @Override
    public Object createTrue() {
        return true;
    }

    @Override
    public Object createFalse() {
        return false;
    }

    @Override
    public Object createNull() {
        return JSONObject.NULL;
    }

    @Override
    public boolean isJsonObject(Object o) {
        return o instanceof JSONObject || o instanceof JSONArray;
    }

    @Override
    public Object convertToJsonObject(Object o) {
        return o;
    }
}
