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

    private JsonStringTemplateProcessor<U> withProcessor(JsonStringTemplateProcessor<JSON> processor) {
        return new JsonStringTemplateProcessorAndThenImpl<>(processor, function);
    }

    @Override
    public boolean cacheEnabled() {
        return processor.cacheEnabled();
    }

    @Override
    public JsonStringTemplateProcessor<U> withCacheEnabled(boolean cacheEnabled) {
        return withProcessor(processor.withCacheEnabled(cacheEnabled));
    }

    @Override
    public boolean tailingCommaAllowed() {
        return processor.tailingCommaAllowed();
    }

    @Override
    public JsonStringTemplateProcessor<U> withTailingCommaAllowed(boolean tailingCommaAllowed) {
        return withProcessor(processor.withTailingCommaAllowed(tailingCommaAllowed));
    }

    @Override
    public boolean extraCommaAllowed() {
        return processor.extraCommaAllowed();
    }

    @Override
    public JsonStringTemplateProcessor<U> withExtraCommaAllowed(boolean extraCommaAllowed) {
        return withProcessor(processor.withExtraCommaAllowed(extraCommaAllowed));
    }

    @Override
    public boolean commentAllowed() {
        return processor.commentAllowed();
    }

    @Override
    public JsonStringTemplateProcessor<U> withCommentAllowed(boolean commentAllowed) {
        return withProcessor(processor.withCommentAllowed(commentAllowed));
    }

    @Override
    public boolean singleQuoteForStringSeparatorAllowed() {
        return processor.singleQuoteForStringSeparatorAllowed();
    }

    @Override
    public JsonStringTemplateProcessor<U> withSingleQuoteForStringSeparatorAllowed(
            boolean singleQuoteForStringSeparatorAllowed) {
        return withProcessor(processor.withSingleQuoteForStringSeparatorAllowed(singleQuoteForStringSeparatorAllowed));
    }
}
