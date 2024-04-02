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

import java.lang.invoke.MethodHandle;
import java.util.List;

public class JsonStringTemplateProcessorV3Impl<JSON> extends AbstractJsonStringTemplateProcessor<JSON> {
    private final JsonCompilerBridge<JSON> jsonBridge;
    private final JsonCompilerV2<JSON> compiler;

    public JsonStringTemplateProcessorV3Impl(
            JsonCompilerBridge<JSON> jsonBridge, JsonStringTemplateConfiguration config) {
        super(config);
        this.jsonBridge = jsonBridge;
        this.compiler = new JsonCompilerV2<>(jsonBridge);
    }

    @Override
    public JSON process(StringTemplate stringTemplate) {
        return process(compile(stringTemplate), stringTemplate.values());
    }

    public MethodHandle compile(StringTemplate stringTemplate) {
        var parser = new JsonParserV2(new JsonTokenizer(stringTemplate, config), config);
        return compiler.compile(stringTemplate.values().size(), parser.parseJson());
    }

    public JSON process(MethodHandle handle, List<Object> values) {
        // jshell> for (int i = 0; i <= 21; i++) {
        //   ...>     System.out.print("case " + i + " -> (JSON) handle.invokeExact(");
        //   ...>     for (int j = 0; j < i; j++) {
        //   ...>         System.out.print("values.get(" + j + "),");
        //   ...>     }
        //   ...>     System.out.println(");");
        //   ...> }
        try {
            return switch (values.size()) {
                case 0 ->  (JSON) handle.invokeExact();
                case 1 ->  (JSON) handle.invokeExact(values.get(0));
                case 2 ->  (JSON) handle.invokeExact(values.get(0),values.get(1));
                case 3 ->  (JSON) handle.invokeExact(values.get(0),values.get(1),values.get(2));
                case 4 ->  (JSON) handle.invokeExact(values.get(0),values.get(1),values.get(2),values.get(3));
                case 5 ->  (JSON) handle.invokeExact(values.get(0),values.get(1),values.get(2),values.get(3),values.get(4));
                case 6 ->  (JSON) handle.invokeExact(values.get(0),values.get(1),values.get(2),values.get(3),values.get(4),values.get(5));
                case 7 ->  (JSON) handle.invokeExact(values.get(0),values.get(1),values.get(2),values.get(3),values.get(4),values.get(5),values.get(6));
                case 8 ->  (JSON) handle.invokeExact(values.get(0),values.get(1),values.get(2),values.get(3),values.get(4),values.get(5),values.get(6),values.get(7));
                case 9 ->  (JSON) handle.invokeExact(values.get(0),values.get(1),values.get(2),values.get(3),values.get(4),values.get(5),values.get(6),values.get(7),values.get(8));
                case 10 -> (JSON) handle.invokeExact(values.get(0),values.get(1),values.get(2),values.get(3),values.get(4),values.get(5),values.get(6),values.get(7),values.get(8),values.get(9));
                case 11 -> (JSON) handle.invokeExact(values.get(0),values.get(1),values.get(2),values.get(3),values.get(4),values.get(5),values.get(6),values.get(7),values.get(8),values.get(9),values.get(10));
                case 12 -> (JSON) handle.invokeExact(values.get(0),values.get(1),values.get(2),values.get(3),values.get(4),values.get(5),values.get(6),values.get(7),values.get(8),values.get(9),values.get(10),values.get(11));
                case 13 -> (JSON) handle.invokeExact(values.get(0),values.get(1),values.get(2),values.get(3),values.get(4),values.get(5),values.get(6),values.get(7),values.get(8),values.get(9),values.get(10),values.get(11),values.get(12));
                case 14 -> (JSON) handle.invokeExact(values.get(0),values.get(1),values.get(2),values.get(3),values.get(4),values.get(5),values.get(6),values.get(7),values.get(8),values.get(9),values.get(10),values.get(11),values.get(12),values.get(13));
                case 15 -> (JSON) handle.invokeExact(values.get(0),values.get(1),values.get(2),values.get(3),values.get(4),values.get(5),values.get(6),values.get(7),values.get(8),values.get(9),values.get(10),values.get(11),values.get(12),values.get(13),values.get(14));
                case 16 -> (JSON) handle.invokeExact(values.get(0),values.get(1),values.get(2),values.get(3),values.get(4),values.get(5),values.get(6),values.get(7),values.get(8),values.get(9),values.get(10),values.get(11),values.get(12),values.get(13),values.get(14),values.get(15));
                case 17 -> (JSON) handle.invokeExact(values.get(0),values.get(1),values.get(2),values.get(3),values.get(4),values.get(5),values.get(6),values.get(7),values.get(8),values.get(9),values.get(10),values.get(11),values.get(12),values.get(13),values.get(14),values.get(15),values.get(16));
                case 18 -> (JSON) handle.invokeExact(values.get(0),values.get(1),values.get(2),values.get(3),values.get(4),values.get(5),values.get(6),values.get(7),values.get(8),values.get(9),values.get(10),values.get(11),values.get(12),values.get(13),values.get(14),values.get(15),values.get(16),values.get(17));
                case 19 -> (JSON) handle.invokeExact(values.get(0),values.get(1),values.get(2),values.get(3),values.get(4),values.get(5),values.get(6),values.get(7),values.get(8),values.get(9),values.get(10),values.get(11),values.get(12),values.get(13),values.get(14),values.get(15),values.get(16),values.get(17),values.get(18));
                case 20 -> (JSON) handle.invokeExact(values.get(0),values.get(1),values.get(2),values.get(3),values.get(4),values.get(5),values.get(6),values.get(7),values.get(8),values.get(9),values.get(10),values.get(11),values.get(12),values.get(13),values.get(14),values.get(15),values.get(16),values.get(17),values.get(18),values.get(19));
                case 21 -> (JSON) handle.invokeExact(values.get(0),values.get(1),values.get(2),values.get(3),values.get(4),values.get(5),values.get(6),values.get(7),values.get(8),values.get(9),values.get(10),values.get(11),values.get(12),values.get(13),values.get(14),values.get(15),values.get(16),values.get(17),values.get(18),values.get(19),values.get(20));

                default -> (JSON) handle.invokeWithArguments(values);
            };
        } catch (Throwable e) {
            throw new RuntimeException(handle.toString(), e);
        }
    }

    @Override
    protected JsonStringTemplateProcessor<JSON> withConfiguration(JsonStringTemplateConfiguration config) {
        return new JsonStringTemplateProcessorV3Impl<>(jsonBridge, config);
    }
}
