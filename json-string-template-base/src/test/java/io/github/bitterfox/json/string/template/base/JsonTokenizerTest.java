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

package io.github.bitterfox.json.string.template.base;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.bitterfox.json.string.template.base.JsonToken.JTArrayClose;
import io.github.bitterfox.json.string.template.base.JsonToken.JTArrayOpen;
import io.github.bitterfox.json.string.template.base.JsonToken.JTColon;
import io.github.bitterfox.json.string.template.base.JsonToken.JTComma;
import io.github.bitterfox.json.string.template.base.JsonToken.JTFalse;
import io.github.bitterfox.json.string.template.base.JsonToken.JTJavaObject;
import io.github.bitterfox.json.string.template.base.JsonToken.JTNull;
import io.github.bitterfox.json.string.template.base.JsonToken.JTNumber;
import io.github.bitterfox.json.string.template.base.JsonToken.JTObjectClose;
import io.github.bitterfox.json.string.template.base.JsonToken.JTObjectOpen;
import io.github.bitterfox.json.string.template.base.JsonToken.JTString;
import io.github.bitterfox.json.string.template.base.JsonToken.JTTrue;

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
                        new JTObjectOpen(),
                        // L2
                        new JTString("o1"), new JTColon(),
                        new JTArrayOpen(), new JTJavaObject(o2), new JTComma(), new JTJavaObject(o3), new JTArrayClose(), new JTComma(),
                        // L3
                        new JTString("test"), new JTColon(), new JTNumber("1234"), new JTComma(),
                        // L4
                        new JTString("complex number"), new JTColon(), new JTNumber("-1234.84E+5"), new JTComma(),
                        // L5
                        new JTString("null"), new JTColon(), new JTJavaObject(o4), new JTComma(),
                        // L6
                        new JTString("true"), new JTColon(), new JTTrue(), new JTComma(),
                        // L7
                        new JTString("false"), new JTColon(), new JTFalse(), new JTComma(),
                        // L8
                        new JTString("null"), new JTColon(), new JTNull(),
                        // L9
                        new JTObjectClose()
                ),
                tokens
        );
    }

    // TODO more test cases including failure pattern
}
