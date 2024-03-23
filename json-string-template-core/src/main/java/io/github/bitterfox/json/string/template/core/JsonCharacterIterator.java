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

import java.util.Iterator;

import io.github.bitterfox.json.string.template.core.JsonCharacter.JCCh;
import io.github.bitterfox.json.string.template.core.JsonCharacter.JCObj;
import io.github.bitterfox.json.string.template.core.JsonCharacter.JCWhitespace;
import io.github.bitterfox.json.string.template.core.JsonPosition.EndOfStringTemplate;
import io.github.bitterfox.json.string.template.core.JsonPosition.FragmentPosition;
import io.github.bitterfox.json.string.template.core.JsonPosition.ValuePosition;

public class JsonCharacterIterator implements Iterator<JsonCharacter> {
    private final StringTemplate template;

    private final Iterator<String> fragments;
    private final Iterator<Object> values;

    private String current;
    private int cursor;
    private JsonCharacter next;

    private JsonPosition pos;

    private int index = 0;

    public JsonCharacterIterator(StringTemplate template, JsonStringTemplateConfiguration config) {
        this.template = template;

        require(!template.fragments().isEmpty());
        require(template.fragments().size() == template.values().size() + 1);

        fragments = template.fragments().iterator();
        values = template.values().iterator();

        readNext();
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

    public JsonCharacter peek() {
        return next;
    }

    public JsonPosition pos() {
        return pos;
    }

    private void readNext() {
        if (current == null) {
            readNextString();
        }

        if (cursor < current.length()) {
            if (isWhitespace()) {
                next = new JCWhitespace(current.charAt(cursor), new FragmentPosition(index, cursor));
                pos = next.pos();
                cursor++;
            } else {
                next = new JCCh(current.charAt(cursor), new FragmentPosition(index, cursor));
                pos = next.pos();
                cursor++;
            }
        } else {
            if (values.hasNext()) {
                next = new JCObj(values.next(), new ValuePosition(index));
            }
            if (fragments.hasNext()) {
                readNextString();
            } else {
                pos = new EndOfStringTemplate();
                next = null;
            }
        }
    }

    private void readNextString() {
        if (current != null) {
            index++;
        }
        current = fragments.next();
        cursor = 0;
    }

    private boolean isWhitespace() {
        return switch (current.charAt(cursor)) {
            case ' ', '\t', '\n', '\r' -> true;
            default -> false;
        };
    }

    public StringTemplate getTemplate() {
        return template;
    }
}
