/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.github.bitterfox.json.string.template.base;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.bitterfox.json.string.template.base.JsonCharacter.JsonCharacterCh;
import com.github.bitterfox.json.string.template.base.JsonCharacter.JsonCharacterObj;

class JsonCharacterIteratorTest {
    public StringTemplate.Processor<List<JsonCharacter>, RuntimeException> JC =
            StringTemplate.Processor.of(t -> {
                var i = new JsonCharacterIterator(t);
                var l = new ArrayList<JsonCharacter>();
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

        List<JsonCharacter> chs =
                JC."""
                        {
                            "\{o1}": [ \{o2}, \{o3} ],
                            "test": 1234,
                            "null": \{o4}
                        }
                        """;

        assertEquals(
                List.of(
                        ch('{'),
                        ch('"'), obj(o1), ch('"'), ch(':'), ch('['), obj(o2), ch(','), obj(o3), ch(']'), ch(','),
                        ch('"'), ch('t'), ch('e'), ch('s'), ch('t'), ch('"'), ch(':'),
                        ch('1'), ch('2'), ch('3'), ch('4'), ch(','),
                        ch('"'), ch('n'), ch('u'), ch('l'), ch('l'), ch('"'), ch(':'), obj(o4),
                        ch('}')
                ),
                chs);
    }

    private JsonCharacterCh ch(char ch) {
        return new JsonCharacterCh(ch);
    }

    private JsonCharacterObj obj(Object obj) {
        return new JsonCharacterObj(obj);
    }
}
