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

import static io.github.bitterfox.json.string.template.jackson.JsonStringTemplate.JSON;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class JsonStringTemplateTest {
    private record MyData(String test1, int test2) {}

    @Test
    void test() {
        String test = "te\nst";
        MyData testData = new MyData("hello", 0xcafebabe);

        JsonNode json = JSON."""
                {
                    "test": \{test},
                    "request": \{testData}
                }
                """;

        assertEquals(test, json.get("test").asText());
        assertEquals("hello", json.get("request").get("test1").asText());
        assertEquals(0xcafebabe, json.get("request").get("test2").asInt());
    }

    @Test
    void test2() throws JsonProcessingException {
        record Profile(
                int id,
                String name,
                String status) {}

        Profile profile = new Profile(101, "Duke", "Hello world!");
        String token = "xxxx";

        JsonNode json = JSON."""
                {
                    "token": \{token},
                    "body": \{profile}
                }
                """;
        assertEquals(token, json.get("token").asText());
        assertEquals(101, json.get("body").get("id").asInt());
        assertEquals("Duke", json.get("body").get("name").asText());
        assertEquals("Hello world!", json.get("body").get("status").asText());

        System.out.println(new ObjectMapper().writeValueAsString(json));

    }
}
