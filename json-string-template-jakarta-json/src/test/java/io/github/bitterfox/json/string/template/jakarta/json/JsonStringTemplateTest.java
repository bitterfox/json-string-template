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

package io.github.bitterfox.json.string.template.jakarta.json;

import static io.github.bitterfox.json.string.template.jakarta.json.JsonStringTemplate.JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import jakarta.json.Json;
import jakarta.json.JsonValue;

import org.junit.jupiter.api.Test;

class JsonStringTemplateTest {
    @Test
    void test() {
        String value = "te\nst";
        JsonValue json = JSON."""
                {
                    "test": \{value}
                }
                """;

        assertEquals(
                Json.createObjectBuilder()
                        .add("test", value)
                        .build(),
                json);
    }

    @Test
    void test2() {
        String name = "Java";
        int age = 28;
        LocalDate dateOfBirth = LocalDate.of(1996, 1, 23);
        List<String> supportedVersions = List.of("22", "21", "17");

        JsonValue father = JSON."""
                {
                    "name": "James Arthur Gosling"
                }
                """;

        JsonValue json = JSON."""
                {
                    "name": \{name},
                    "age": \{age},
                    "dateOfBirth": \{dateOfBirth},
                    "supportedVersions": \{supportedVersions},
                    "father": \{father}
                }
                """;

        assertEquals(
                Json.createObjectBuilder()
                        .add("name", "Java")
                        .add("age", 28)
                        .add("dateOfBirth", "1996-01-23")
                        .add("supportedVersions", Json.createArrayBuilder().add("22").add("21").add("17"))
                        .add("father", Json.createObjectBuilder()
                                .add("name", "James Arthur Gosling"))
                        .build(),
                json);
    }
}
