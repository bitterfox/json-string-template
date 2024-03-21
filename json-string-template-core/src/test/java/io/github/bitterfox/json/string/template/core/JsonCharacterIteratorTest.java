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

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.bitterfox.json.string.template.core.JsonCharacter.JCCh;
import io.github.bitterfox.json.string.template.core.JsonCharacter.JCObj;
import io.github.bitterfox.json.string.template.core.JsonCharacter.JCWhitespace;
import io.github.bitterfox.json.string.template.core.JsonPosition.FragmentPosition;
import io.github.bitterfox.json.string.template.core.JsonPosition.ValuePosition;

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

        Factory f = new Factory();
        assertEquals(
                List.of(
                        f.ch('{'), f.nl(),
                        f.w(), f.w(), f.w(), f.w(), f.ch('"'), f.obj(o1), f.ch('"'), f.ch(':'),
                            f.w(), f.ch('['), f.w(), f.obj(o2), f.ch(','), f.w(), f.obj(o3), f.w(), f.ch(']'), f.ch(','), f.nl(),
                        f.w(), f.w(), f.w(), f.w(), f.ch('"'), f.ch('t'), f.ch('e'), f.ch('s'), f.ch('t'), f.ch('"'), f.ch(':'),
                            f.w(), f.ch('1'), f.ch('2'), f.ch('3'), f.ch('4'), f.ch(','), f.nl(),
                        f.w(), f.w(), f.w(), f.w(), f.ch('"'), f.ch('n'), f.ch('u'), f.ch('l'), f.ch('l'), f.ch('"'), f.ch(':'),
                            f.w(), f.obj(o4), f.nl(),
                        f.ch('}'), f.nl()
                ),
                chs);
    }

    private static class Factory {
        private int fragmentIndex;
        private int cursor;

        private JCCh ch(char ch) {
            return new JCCh(ch, new FragmentPosition(fragmentIndex, cursor++));
        }

        private JCObj obj(Object obj) {
            cursor = 0;
            return new JCObj(obj, new ValuePosition(fragmentIndex++));
        }

        private JCWhitespace w() {
            return new JCWhitespace(' ', new FragmentPosition(fragmentIndex, cursor++));
        }
        private JCWhitespace nl() {
            return new JCWhitespace('\n', new FragmentPosition(fragmentIndex, cursor++));
        }
    }
}
