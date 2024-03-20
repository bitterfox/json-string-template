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

import java.util.function.Function;

public class JsonStringTemplateProcessor<JSON> implements StringTemplate.Processor<JSON, RuntimeException> {
    private final StringTemplate.Processor<JSON, RuntimeException> delegate;

    private JsonStringTemplateProcessor(StringTemplate.Processor<JSON, RuntimeException> delegate) {
        this.delegate = delegate;
    }

    public static <JSON> JsonStringTemplateProcessor<JSON> of(JsonBridge<JSON> jsonBridge) {
        return new JsonStringTemplateProcessor<>(
                StringTemplate.Processor.of(stringTemplate -> {
                    var parser = new JsonParser<JSON>(new JsonTokenizer(stringTemplate), jsonBridge);
                    return parser.parseJson();
                }));
    }

    @Override
    public JSON process(StringTemplate stringTemplate) throws RuntimeException {
        return delegate.process(stringTemplate);
    }

    public <U> JsonStringTemplateProcessor<U> andThen(Function<? super JSON, ? extends U> f) {
        return new JsonStringTemplateProcessor<U>(StringTemplate.Processor.of(t -> f.apply(this.process(t))));
    }
}
