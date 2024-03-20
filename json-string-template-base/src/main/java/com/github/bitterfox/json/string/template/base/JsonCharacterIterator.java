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

import java.util.Iterator;
import java.util.List;

import com.github.bitterfox.json.string.template.base.JsonCharacter.JsonCharacterCh;
import com.github.bitterfox.json.string.template.base.JsonCharacter.JsonCharacterObj;

public class JsonCharacterIterator implements Iterator<JsonCharacter> {

    private final Iterator<String> fragments;
    private final Iterator<Object> values;

    private String current;
    private int cursor;
    private JsonCharacter next;

    public JsonCharacterIterator(List<String> fragments, List<Object> values) {
        require(!fragments.isEmpty());
        require(fragments.size() == values.size() + 1);

        this.fragments = fragments.iterator();
        this.values = values.iterator();

        readNext();
    }

    public JsonCharacterIterator(StringTemplate template) {
        this(template.fragments(), template.values());
    }

    private void require(boolean b) {
        if (!b) {
            throw new RuntimeException("");
        }
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public JsonCharacter next() {
        var n = next;
        readNext();
        return n;
    }

    private void readNext() {
        if (current == null) {
            readNextString();
        }

        skipWhitespace();
        if (cursor < current.length()) {
            next = new JsonCharacterCh(current.charAt(cursor++));
        } else {
            if (values.hasNext()) {
                next = new JsonCharacterObj(values.next());
            }
            if (fragments.hasNext()) {
                readNextString();
            } else {
                next = null;
            }
        }
    }

    private void readNextString() {
        current = fragments.next();
        cursor = 0;
    }

    private void skipWhitespace() {
        while (cursor < current.length()) {
            switch (current.charAt(cursor)) {
                case ' ', '\t', '\n', '\r':
                    cursor++;
                    break;
                default: return;
            }
        }
    }
}
