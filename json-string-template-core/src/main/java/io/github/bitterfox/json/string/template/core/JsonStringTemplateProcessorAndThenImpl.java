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

import java.util.function.Function;

public record JsonStringTemplateProcessorAndThenImpl<JSON, U>(
        JsonStringTemplateProcessor<JSON> processor,
        Function<? super JSON, ? extends U> function
) implements JsonStringTemplateProcessor<U> {
    @Override
    public U process(StringTemplate stringTemplate) {
        return function.apply(processor.process(stringTemplate));
    }

    @Override
    public JsonStringTemplateConfiguration configuration() {
        return processor.configuration();
    }

    @Override
    public boolean cacheEnabled() {
        return processor.cacheEnabled();
    }

    @Override
    public JsonStringTemplateProcessor<U> withCacheEnabled(boolean cacheEnabled) {
        return new JsonStringTemplateProcessorAndThenImpl<>(processor.withCacheEnabled(cacheEnabled), function);
    }

    @Override
    public boolean tailingCommaAllowed() {
        return processor.tailingCommaAllowed();
    }

    @Override
    public JsonStringTemplateProcessor<U> withTailingCommaAllowed(boolean tailingCommaAllowed) {
        return new JsonStringTemplateProcessorAndThenImpl<>(processor.withTailingCommaAllowed(tailingCommaAllowed), function);
    }

    @Override
    public boolean extraCommaAllowed() {
        return processor.extraCommaAllowed();
    }

    @Override
    public JsonStringTemplateProcessor<U> withExtraCommaAllowed(boolean extraCommaAllowed) {
        return new JsonStringTemplateProcessorAndThenImpl<>(processor.withExtraCommaAllowed(extraCommaAllowed), function);
    }
}
