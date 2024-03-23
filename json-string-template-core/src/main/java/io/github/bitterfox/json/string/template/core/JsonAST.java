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

import java.util.List;
import java.util.Map.Entry;

import io.github.bitterfox.json.string.template.core.JsonAST.JASTArray;
import io.github.bitterfox.json.string.template.core.JsonAST.JASTFalse;
import io.github.bitterfox.json.string.template.core.JsonAST.JASTJavaObject;
import io.github.bitterfox.json.string.template.core.JsonAST.JASTNull;
import io.github.bitterfox.json.string.template.core.JsonAST.JASTNumberNumber;
import io.github.bitterfox.json.string.template.core.JsonAST.JASTNumberString;
import io.github.bitterfox.json.string.template.core.JsonAST.JASTObject;
import io.github.bitterfox.json.string.template.core.JsonAST.JASTString;
import io.github.bitterfox.json.string.template.core.JsonAST.JASTTrue;
import io.github.bitterfox.json.string.template.core.JsonPosition.ValuePosition;

public sealed interface JsonAST
        permits JASTObject,
                JASTArray,
                JASTString,
                JASTNumberString,
                JASTNumberNumber,
                JASTJavaObject,
                JASTTrue,
                JASTFalse,
                JASTNull {
    JASTObject EMPTY_OBJECT = new JASTObject(List.of());
    JASTArray EMPTY_ARRAY = new JASTArray(List.of());
    JASTTrue TRUE = new JASTTrue();
    JASTFalse FALSE = new JASTFalse();
    JASTNull NULL = new JASTNull();

    record JASTObject(List<Entry<JsonAST, JsonAST>> fields) implements JsonAST {
        @Override
        public <T> T visit(JsonVisitor<T> visitor) {
            return visitor.visitObject(this);
        }
    }
    record JASTArray(List<JsonAST> values) implements JsonAST {
        @Override
        public <T> T visit(JsonVisitor<T> visitor) {
            return visitor.visitArray(this);
        }
    }
    record JASTString(List<String> fragments, JsonPositionRange pos) implements JsonAST {
        @Override
        public <T> T visit(JsonVisitor<T> visitor) {
            return visitor.visitString(this);
        }
    }
    record JASTNumberString(String number) implements JsonAST {
        @Override
        public <T> T visit(JsonVisitor<T> visitor) {
            return visitor.visitNumber(this);
        }
    }
    record JASTNumberNumber(Number number) implements JsonAST {
        @Override
        public <T> T visit(JsonVisitor<T> visitor) {
            return visitor.visitNumber(this);
        }
    }
    record JASTTrue() implements JsonAST {
        @Override
        public <T> T visit(JsonVisitor<T> visitor) {
            return visitor.visitTrue(this);
        }
    }
    record JASTFalse() implements JsonAST {
        @Override
        public <T> T visit(JsonVisitor<T> visitor) {
            return visitor.visitFalse(this);
        }
    }
    record JASTNull() implements JsonAST {
        @Override
        public <T> T visit(JsonVisitor<T> visitor) {
            return visitor.visitNull(this);
        }
    }
    record JASTJavaObject(ValuePosition pos) implements JsonAST {
        @Override
        public <T> T visit(JsonVisitor<T> visitor) {
            return visitor.visitJavaObject(this);
        }
    }

    <T> T visit(JsonVisitor<T> visitor);

    interface JsonVisitor<T> {
        T visitObject(JASTObject that);
        T visitArray(JASTArray that);
        T visitString(JASTString that);
        T visitNumber(JASTNumberString that);
        T visitNumber(JASTNumberNumber that);
        T visitTrue(JASTTrue that);
        T visitFalse(JASTFalse that);
        T visitNull(JASTNull that);
        T visitJavaObject(JASTJavaObject that);
    }
}
