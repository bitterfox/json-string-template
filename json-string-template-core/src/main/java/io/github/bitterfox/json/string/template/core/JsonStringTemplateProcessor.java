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

import java.lang.StringTemplate.Processor;
import java.util.function.Function;

public interface JsonStringTemplateProcessor<JSON> extends Processor<JSON, RuntimeException> {
    static <JSON> JsonStringTemplateProcessor<JSON> of(JsonBridge<JSON> jsonBridge) {
        return ofV2Cached(jsonBridge);
    }

    /**
     * {@link #ofV1(JsonBridge)} is used only for backward compatibility and internal purpose like benchmarking. Use {@link #of(JsonBridge)}.
     * @param jsonBridge
     * @return
     * @param <JSON>
     */
    @Deprecated(forRemoval = true)
    static <JSON> JsonStringTemplateProcessor<JSON> ofV1(JsonBridge<JSON> jsonBridge) {
        return new JsonStringTemplateProcessorV1Impl<>(jsonBridge);
    }

    /**
     * {@link #ofV2(JsonBridge)} is used only for backward compatibility and internal purpose like benchmarking. Use {@link #of(JsonBridge)}.
     * @param jsonBridge
     * @return
     * @param <JSON>
     */
    @Deprecated(forRemoval = true)
    static <JSON> JsonStringTemplateProcessor<JSON> ofV2(JsonBridge<JSON> jsonBridge) {
        return new JsonStringTemplateProcessorV2Impl<>(jsonBridge);
    }

    /**
     * {@link #ofV2(JsonBridge)} is used only for backward compatibility and internal purpose like benchmarking. Use {@link #of(JsonBridge)}.
     * @param jsonBridge
     * @return
     * @param <JSON>
     */
    @Deprecated(forRemoval = true)
    static <JSON> JsonStringTemplateProcessor<JSON> ofV2Cached(JsonBridge<JSON> jsonBridge) {
        return new JsonStringTemplateProcessorV2CachedImpl<>(jsonBridge);
    }

    JSON process(StringTemplate stringTemplate);

    default <U> JsonStringTemplateProcessor<U> andThen(Function<? super JSON, ? extends U> f) {
        return new JsonStringTemplateProcessorAndThenImpl<>(this, f);
    }
}
