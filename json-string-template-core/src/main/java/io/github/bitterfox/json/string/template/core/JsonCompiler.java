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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import io.github.bitterfox.json.string.template.core.JsonAST.JASTArray;
import io.github.bitterfox.json.string.template.core.JsonAST.JASTFalse;
import io.github.bitterfox.json.string.template.core.JsonAST.JASTJavaObject;
import io.github.bitterfox.json.string.template.core.JsonAST.JASTNull;
import io.github.bitterfox.json.string.template.core.JsonAST.JASTNumberNumber;
import io.github.bitterfox.json.string.template.core.JsonAST.JASTNumberString;
import io.github.bitterfox.json.string.template.core.JsonAST.JASTObject;
import io.github.bitterfox.json.string.template.core.JsonAST.JASTString;
import io.github.bitterfox.json.string.template.core.JsonAST.JASTTrue;
import io.github.bitterfox.json.string.template.core.JsonAST.JsonVisitor;
import io.github.bitterfox.json.string.template.core.JsonPosition.FragmentPosition;
import io.github.bitterfox.json.string.template.core.JsonPosition.ValuePosition;

public class JsonCompiler<JSON> {
    private JsonBridge<JSON> jsonBridge;
    private final JSON TRUE;
    private final JSON FALSE;
    private final JSON NULL;
    private final Visitor visitor;

    public JsonCompiler(JsonBridge<JSON> jsonBridge) {
        this.jsonBridge = jsonBridge;
        TRUE = jsonBridge.createTrue();
        FALSE = jsonBridge.createFalse();
        NULL = jsonBridge.createNull();
        visitor = new Visitor();
    }

    public CompiledJsonStringTemplate<JSON> compile(JsonAST root) {
        return root.visit(visitor);
    }

    private class Visitor implements JsonVisitor<CompiledJsonStringTemplate<JSON>> {

        @Override
        public CompiledJsonStringTemplate<JSON> visitObject(JASTObject that) {
            if (that.fields().isEmpty()) {
                return _ -> jsonBridge.createObject(Map.of());
            }

            // Can we optimize more?
            // If we have `"key": 1234`, we can make pair of String and JSON and reuse that
            // We cannot reuse `"key": {"k": 1234}` because `{"k": 1234}` is a object and can be mutable

            Entry<CompiledJsonStringTemplate<String>,
                    CompiledJsonStringTemplate<JSON>>[] compiledFields =
                    that.fields().stream()
                        .map(e -> Map.entry(compileKey(e.getKey()), e.getValue().visit(this)))
                        .<Entry<CompiledJsonStringTemplate<String>,
                                CompiledJsonStringTemplate<JSON>>>toArray(Entry[]::new);

            return values -> {
                Map<String, JSON> fields = new HashMap<>();
                for (var compiledField : compiledFields) {
                    String key = compiledField.getKey().apply(values);
                    JSON value = compiledField.getValue().apply(values);

                    if (fields.containsKey(key)) {
                        throw new IllegalStateException(STR."Duplicated \{key}, \{fields}");
                    }

                    fields.put(key, value);
                }
                return jsonBridge.createObject(fields);
            };
        }

        private CompiledJsonStringTemplate<String> compileKey(JsonAST key) {
            return switch (key) {
                case JASTString(List<String> fragment, JsonPositionRange(
                        FragmentPosition(int start, _),
                        FragmentPosition(int end, _))) -> compileString(fragment, start, end);
                case JASTString(_, var pos) ->
                        throw new RuntimeException(
                                STR."Pos in JASTString must be FragmentPosition for both of start and end: \{pos}");
                case JASTJavaObject(ValuePosition(int index)) -> compileJavaObject(index);
                // Should not reach here, such case will be handled at parser
                case JsonAST it -> throw new RuntimeException(STR."Key in JsonObject must be a String, but \{it} found");
            };
        }

        private CompiledJsonStringTemplate<String> compileString(List<String> fragments, int start, int end) {
            return values -> {
                List<Object> subvalues = values.subList(start, end);
                return STR.process(StringTemplate.of(fragments, subvalues));
            };
        }
        private CompiledJsonStringTemplate<String> compileJavaObject(int index) {
            return values -> {
                Object object = values.get(index);

                if (object instanceof CharSequence it) {
                    return it.toString();
                }

                if (object == null) {
                    throw new RuntimeException(STR."Java object for key at \{index} must be subtype of CharSequence, but null object");
                }
                throw new RuntimeException(STR."Java object for key at \{index} must be subtype of CharSequence, but \{object.getClass()}: \{object}");
            };
        }

        @Override
        public CompiledJsonStringTemplate<JSON> visitArray(JASTArray that) {
            CompiledJsonStringTemplate<JSON>[] compiledArray = that.values().stream()
                                 .map(v -> v.visit(this))
                                 .<CompiledJsonStringTemplate<JSON>>toArray(CompiledJsonStringTemplate[]::new);
            return values -> {
                List<JSON> array = new ArrayList<>(compiledArray.length);
                for (var v : compiledArray) {
                    array.add(v.apply(values));
                }
                return jsonBridge.createArray(array);
            };
        }

        @Override
        public CompiledJsonStringTemplate<JSON> visitString(JASTString that) {
            if (that.fragments().size() == 1) {
                // This string doesn't contain values
                // We can assume Json String is immutable
                JSON first = jsonBridge.createString(that.fragments().getFirst());
                return _ -> first;
            }

            return switch (that) {
                case JASTString(List<String> fragment, JsonPositionRange(
                        FragmentPosition(int start, _),
                        FragmentPosition(int end, _))) -> compileString(fragment, start, end).andThen(jsonBridge::createString)::apply;
                case JASTString(_, var pos) -> throw new RuntimeException(
                        STR."Pos in JASTString must be FragmentPosition for both of start and end: \{pos}");
            };
        }

        @Override
        public CompiledJsonStringTemplate<JSON> visitNumber(JASTNumberString that) {
            JSON number = jsonBridge.createNumber(that.number());
            return _ -> number;
        }

        @Override
        public CompiledJsonStringTemplate<JSON> visitNumber(JASTNumberNumber that) {
            JSON number = jsonBridge.createNumber(that.number());
            return _ -> number;
        }

        @Override
        public CompiledJsonStringTemplate<JSON> visitTrue(JASTTrue that) {
            return _ -> TRUE;
        }

        @Override
        public CompiledJsonStringTemplate<JSON> visitFalse(JASTFalse that) {
            return _ -> FALSE;
        }

        @Override
        public CompiledJsonStringTemplate<JSON> visitNull(JASTNull that) {
            return _ -> NULL;
        }

        @Override
        public CompiledJsonStringTemplate<JSON> visitJavaObject(JASTJavaObject that) {
            int i = that.pos().index();
            return value -> convertJavaObjectToJSON(value.get(i));
        }

        private JSON convertJavaObjectToJSON(Object o) {
            if (jsonBridge.isJsonObject(o)) {
                return jsonBridge.convertToJsonObject(o);
            }

            // Default binding
            if (o == null) {
                return jsonBridge.createNull();
            }

            return switch (o) {
                case Number n -> jsonBridge.createNumber(n);
                case Boolean b -> b ? jsonBridge.createTrue() : jsonBridge.createFalse();
                case Object[] a -> {
                    JSON array = jsonBridge.createArray(
                            Arrays.stream(a)
                                  .map(this::convertJavaObjectToJSON)
                                  .collect(Collectors.toList()));
                    yield array;
                }
                case Collection<?> c -> {
                    JSON array = jsonBridge.createArray(
                            c.stream()
                             .map(this::convertJavaObjectToJSON)
                             .collect(Collectors.toList()));
                    yield array;
                }
                case Object _ -> jsonBridge.createString(o.toString());
            };
        }
    }

}
