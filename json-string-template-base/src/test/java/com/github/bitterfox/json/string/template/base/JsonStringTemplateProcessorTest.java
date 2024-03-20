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

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class JsonStringTemplateProcessorTest {
    private StringTemplate.Processor<Object, RuntimeException> JSON =
            JsonStringTemplateProcessor.of(new JavaObjectJsonBridge());

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static class JavaObjectJsonBridge implements JsonBridge<Object> {
        @Override
        public Object createObject(Map<String, Object> object) {
            return object;
        }

        @Override
        public Object createArray(List<Object> array) {
            return array;
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
            return null;
        }

        @Override
        public void addToObject(Object object, Object key, Object value) {
            if (object instanceof Map m) {
                m.put(key, value);
            } else {
                throw new RuntimeException(STR."Wrong object type, \{object.getClass()}");
            }
        }

        @Override
        public void addToArray(Object array, Object value) {
            if (array instanceof List l) {
                l.add(value);
            } else {
                throw new RuntimeException(STR."Wrong object type, \{array.getClass()}");
            }
        }

        @Override
        public boolean isJsonObject(Object o) {
            // Test default convert
            return false;
        }

        @Override
        public Object convertToJsonObject(Object o) {
            return null;
        }
    }

    @Test
    void test() {
        String name = "name";
        String text = """
                hello
                world
                """;
        int number = 1234;

        Object json = JSON."""
                {
                    \{name}: \{text},
                    "number\{name}": \{number},
                    "boolean": true,
                    "array": \{List.of(1, 2, 3)}
                }
                """;

        assertEquals(
                Map.of("name", text,
                       "numbername", number,
                       "boolean", true,
                       "array", List.of(1, 2, 3)
                ),
                json);
    }
}
