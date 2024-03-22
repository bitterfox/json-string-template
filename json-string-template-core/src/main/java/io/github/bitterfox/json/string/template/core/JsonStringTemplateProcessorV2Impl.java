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

public class JsonStringTemplateProcessorV2Impl<JSON> extends AbstractJsonStringTemplateProcessor<JSON> {
    private final JsonCompiler<JSON> compiler;

    public JsonStringTemplateProcessorV2Impl(
            JsonBridge<JSON> jsonBridge, JsonStringTemplateConfiguration config) {
        super(config);
        compiler = new JsonCompiler<>(jsonBridge, config);
    }

    @Override
    public JSON process(StringTemplate stringTemplate) throws RuntimeException {
        return compile(stringTemplate).apply(stringTemplate.values());
    }

    public CompiledJsonStringTemplate<JSON> compile(StringTemplate stringTemplate) {
        var parser = new JsonParserV2(new JsonTokenizer(stringTemplate, config), config);
        return compiler.compile(parser.parseJson());
    }
}
