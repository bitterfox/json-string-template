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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

abstract class AbstractJsonStringTemplateProcessorTest {
    protected JsonStringTemplateProcessor<Object> JSON;

    @SuppressWarnings({"rawtypes", "unchecked"})
    static class JavaObjectJsonBridge implements JsonBridge<Object> {
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
        public boolean isJsonObject(Object o) {
            // Test default convert
            return false;
        }

        @Override
        public Object convertToJsonObject(Object o) {
            return null;
        }
    }

    public AbstractJsonStringTemplateProcessorTest(JsonStringTemplateProcessor<Object> JSON) {
        this.JSON = JSON;
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

    @Test
    void testArray() {
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
                    "array": [1, 2, 3, 4]
                }
                """;

        assertEquals(
                Map.of("name", text,
                       "numbername", number,
                       "boolean", true,
                       "array", List.of(new BigDecimal(1), new BigDecimal(2), new BigDecimal(3), new BigDecimal(4))
                ),
                json);
    }

    @Test
    void testNumbers() {
        Object json = JSON."""
                {
                    "number1": 0.1234,
                    "number2": 1.2345E-10
                }
                """;

        assertEquals(
                Map.of("number1", new BigDecimal("0.1234"),
                       "number2", new BigDecimal("1.2345E-10")
                ),
                json);
    }

    @Test
    void testTailingCommaAndExtraCommaAllowed() {
        Object json;

        // Object
        json = JSON."""
                {
                    ,,,
                }
                """;
        assertEquals(Map.of(), json);
        json = JSON."""
                {
                    "test": "hoge",,,
                    "test2": "foo",,,
                }
                """;
        assertEquals(Map.of("test", "hoge", "test2", "foo"), json);

        // Array
        json = JSON."""
                [
                    ,,,
                ]
                """;
        assertEquals(List.of(), json);
        json = JSON."""
                [
                    "hoge",,,
                    "foo",,,
                ]
                """;
        assertEquals(List.of("hoge", "foo"), json);
    }

    @Test
    void testExtraCommaNotAllowed() {
        var JSON = this.JSON.disallowExtraComma();
        Object json;

        // Object
        try {
            json = JSON."""
                    {
                        ,,,
                    }
                    """;
            fail();
        } catch (Exception e) {
            // ok
        }
        try {

            json = JSON."""
                {
                    "test": "hoge",,
                    "test2": "foo"
                }
                """;
            fail();
        } catch (Exception e) {
            // ok
        }
        try {

            json = JSON."""
                {
                    "test": "hoge",
                    "test2": "foo",,
                }
                """;
            fail();
        } catch (Exception e) {
            // ok
        }

        // Array
        try {
            json = JSON."""
                    {
                        ,,,
                    }
                    """;
            fail();
        } catch (Exception e) {
            // ok
        }
        try {

            json = JSON."""
                {
                    "test": "hoge",,
                    "test2": "foo"
                }
                """;
            fail();
        } catch (Exception e) {
            // ok
        }
        try {

            json = JSON."""
                {
                    "test": "hoge",
                    "test2": "foo",,
                }
                """;
            fail();
        } catch (Exception e) {
            // ok
        }
    }

    @Test
    void testTailingCommaNotAllowed() {
        var JSON = this.JSON.disallowTailingComma();
        Object json;

        // Object
        try {
            json = JSON."""
                    {
                        ,
                    }
                    """;
            fail();
        } catch (Exception e) {
            // ok
        }
        try {

            json = JSON."""
                {
                    "test": "hoge",
                    "test2": "foo",
                }
                """;
            fail();
        } catch (Exception e) {
            // ok
        }

        // Array
        try {
            json = JSON."""
                    [
                        ,
                    ]
                    """;
            fail();
        } catch (Exception e) {
            // ok
        }
        try {
            json = JSON."""
                [
                    "hoge",
                    "foo",
                ]
                """;
            fail();
        } catch (Exception e) {
            // ok
        }
    }

    @Test
    void testCommentAllowed() {
        String name = "name";
        String text = """
                hello
                world
                """;
        int number = 1234;

        Object json = JSON."""
                {
                    /**
                     * This field is name
                     * *\\/ <- Note that this is not end of block comment (/ is escaped)
                     */ // combination with single line comment
                    \{name}: \{text}, // we can write comment here
                    "number\{name}": \{number},
                    // single line comment
                    "boolean": true,
                    // single line comment
                    // continuing multi line
                    "array": \{List.of(1, 2, 3)}
                }

                // comment at the end""";

        assertEquals(
                Map.of("name", text,
                       "numbername", number,
                       "boolean", true,
                       "array", List.of(1, 2, 3)
                ),
                json);
    }

    @Test
    void testCommentDisallowed() {
        var JSON = this.JSON.disallowComment();
        try {
            Object json = JSON."""
                {
                    /**
                     * This field is name
                     * *\\/ <- Note that this is not end of block comment (/ is escaped)
                     */ // combination with single line comment
                    "name": "text"
                }
                """;
            fail();
        } catch (Exception e) {
            // ok
        }

        try {
            Object json = JSON."""
                {
                    // line comment
                    "name": "text"
                }
                """;
            fail();
        } catch (Exception e) {
            // ok
        }
    }
}
