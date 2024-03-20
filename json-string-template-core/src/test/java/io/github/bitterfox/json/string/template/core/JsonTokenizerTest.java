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

import static io.github.bitterfox.json.string.template.core.JsonToken.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.bitterfox.json.string.template.core.JsonToken.JTArrayClose;
import io.github.bitterfox.json.string.template.core.JsonToken.JTArrayOpen;
import io.github.bitterfox.json.string.template.core.JsonToken.JTColon;
import io.github.bitterfox.json.string.template.core.JsonToken.JTComma;
import io.github.bitterfox.json.string.template.core.JsonToken.JTFalse;
import io.github.bitterfox.json.string.template.core.JsonToken.JTJavaObject;
import io.github.bitterfox.json.string.template.core.JsonToken.JTNull;
import io.github.bitterfox.json.string.template.core.JsonToken.JTNumber;
import io.github.bitterfox.json.string.template.core.JsonToken.JTObjectClose;
import io.github.bitterfox.json.string.template.core.JsonToken.JTObjectOpen;
import io.github.bitterfox.json.string.template.core.JsonToken.JTString;
import io.github.bitterfox.json.string.template.core.JsonToken.JTTrue;

class JsonTokenizerTest {
    public StringTemplate.Processor<List<JsonToken>, RuntimeException> JT =
            StringTemplate.Processor.of(t -> {
                var i = new JsonTokenizer(t);
                var l = new ArrayList<JsonToken>();
                while (i.hasNext()) {
                    l.add(i.next());
                }
                return l;
            });

    @Test
    void test() {
        Object o1 = "o1";
        Object o2 = 2;
        Object o3 = true;
        Object o4 = null;

        List<JsonToken> tokens =
                JT."""
                            {
                                "\{o1}": [ \{o2}, \{o3} ],
                                "test": 1234,
                                "complex number": -1234.84E+5,
                                "null": \{o4},
                                "true": true,
                                "false": false,
                                "null": null
                            }
                            """;

        assertEquals(
                List.of(
                        // L1
                        OBJECT_OPEN,
                        // L2
                        new JTString("o1"), COLON,
                        ARRAY_OPEN, new JTJavaObject(o2), COMMA, new JTJavaObject(o3), ARRAY_CLOSE, COMMA,
                        // L3
                        new JTString("test"), COLON, new JTNumber("1234"), COMMA,
                        // L4
                        new JTString("complex number"), COLON, new JTNumber("-1234.84E+5"), COMMA,
                        // L5
                        new JTString("null"), COLON, new JTJavaObject(o4), COMMA,
                        // L6
                        new JTString("true"), COLON, TRUE, COMMA,
                        // L7
                        new JTString("false"), COLON, FALSE, COMMA,
                        // L8
                        new JTString("null"), COLON, NULL,
                        // L9
                        OBJECT_CLOSE
                ),
                tokens);
    }

    // TODO more test cases including failure pattern
}
