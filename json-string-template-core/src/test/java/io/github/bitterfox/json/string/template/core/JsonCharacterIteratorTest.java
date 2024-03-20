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
                        ch('{'), nl,
                        w, w, w, w, ch('"'), obj(o1), ch('"'), ch(':'),
                            w, ch('['), w, obj(o2), ch(','), w, obj(o3), w, ch(']'), ch(','), nl,
                        w, w, w, w, ch('"'), ch('t'), ch('e'), ch('s'), ch('t'), ch('"'), ch(':'),
                            w, ch('1'), ch('2'), ch('3'), ch('4'), ch(','), nl,
                        w, w, w, w, ch('"'), ch('n'), ch('u'), ch('l'), ch('l'), ch('"'), ch(':'),
                            w, obj(o4), nl,
                        ch('}'), nl
                ),
                chs);
    }

    private JCWhitespace w = new JCWhitespace(' ');
    private JCWhitespace nl = new JCWhitespace('\n');

    private JCCh ch(char ch) {
        return new JCCh(ch);
    }

    private JCObj obj(Object obj) {
        return new JCObj(obj);
    }
}