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

import io.github.bitterfox.json.string.template.core.JsonPosition.FragmnetPosition;
import io.github.bitterfox.json.string.template.core.JsonPosition.ValuePosition;
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
                        new JTString("o1", range(0, 6, 1, 0)), COLON,
                        ARRAY_OPEN, new JTJavaObject(o2, valueAt(1)), COMMA, new JTJavaObject(o3, valueAt(2)), ARRAY_CLOSE, COMMA,
                        // L3
                        new JTString("test", range(3, 8, 3, 13)), COLON, new JTNumber("1234", range(3, 16, 3, 19)), COMMA,
                        // L4
                        new JTString("complex number", range(3, 26, 3, 41)), COLON, new JTNumber("-1234.84E+5", range(3, 44, 3, 54)), COMMA,
                        // L5
                        new JTString("null", range(3, 61, 3, 66)), COLON, new JTJavaObject(o4, valueAt(3)), COMMA,
                        // L6
                        new JTString("true", range(4, 6, 4, 11)), COLON, TRUE, COMMA,
                        // L7
                        new JTString("false", range(4, 24, 4, 30)), COLON, FALSE, COMMA,
                        // L8
                        new JTString("null", range(4, 4, 4, 49)), COLON, NULL,
                        // L9
                        OBJECT_CLOSE
                ),
                tokens);
    }

    private JsonPositionRange range(int si, int sc, int ei, int ec) {
        return new JsonPositionRange(new FragmnetPosition(si, sc), new FragmnetPosition(ei, ec));
    }

    private ValuePosition valueAt(int i) {
        return new ValuePosition(i);
    }


    // TODO more test cases including failure pattern
}
