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

import static com.github.bitterfox.json.string.template.org.json.JsonStringTemplate.JSON_O;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Map;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

class JsonStringTemplateTest {
    @Test
    void test() {
        String value = "te\nst";
        JSONObject json = JSON_O."""
            {
                "test": \{value}
            }
            """;

        System.out.println(json);

        assertTrue(json.similar(
                new JSONObject(
                        Map.of("test", value)
                )));
    }

    @Test
    void testCommon() {
        int i = 1;
        long l = 2;
        double d = 3.4;
        JSONObject json = JSON_O."""
            {
                "true": true,
                "false": false,
                "null": null,
                "true-expr": \{true},
                "false-expr": \{false},
                "null-expr": \{null},
                "number": 100,
                "number-int": \{i},
                "number-long": \{l},
                "number-double": \{d},
            }
            """;

        System.out.println(json);

        assertTrue(json.similar(
                new JSONObject(
                        Map.of(
                                "true", true,
                                "false", false,
                                "null", JSONObject.NULL,
                                "true-expr", true,
                                "false-expr", false,
                                "null-expr", JSONObject.NULL,
                                "number", new BigDecimal("100"),
                                "number-int", i,
                                "number-long", l,
                                "number-double", d)
                )));
    }
}
