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

public record JsonStringTemplateConfiguration(
        boolean cacheEnabled,
        boolean tailingCommaAllowed,
        boolean extraCommaAllowed,
        boolean commentAllowed
) implements JsonStringTemplateConfigureable<JsonStringTemplateConfiguration> {
    public static final JsonStringTemplateConfiguration DEFAULT = new JsonStringTemplateConfiguration(
            true,
            true,
            true,
            true
    );
    public static final JsonStringTemplateConfiguration JSON_SPEC =
            DEFAULT.disallowTailingComma()
                   .disallowExtraComma()
                   .disallowComment();

    public static JsonStringTemplateConfiguration ofDefault() {
        return DEFAULT;
    }

    public static JsonStringTemplateConfiguration ofJsonSpec() {
        return JSON_SPEC;
    }

    @Override
    public JsonStringTemplateConfiguration withCacheEnabled(boolean cacheEnabled) {
        return new JsonStringTemplateConfiguration(
                cacheEnabled,
                tailingCommaAllowed,
                extraCommaAllowed,
                commentAllowed
        );
    }

    @Override
    public JsonStringTemplateConfiguration withTailingCommaAllowed(boolean tailingCommaAllowed) {
        return new JsonStringTemplateConfiguration(
                cacheEnabled,
                tailingCommaAllowed,
                extraCommaAllowed,
                commentAllowed
        );
    }

    @Override
    public JsonStringTemplateConfiguration withExtraCommaAllowed(boolean extraCommaAllowed) {
        return new JsonStringTemplateConfiguration(
                cacheEnabled,
                tailingCommaAllowed,
                extraCommaAllowed,
                commentAllowed
        );
    }

    @Override
    public JsonStringTemplateConfiguration withCommentAllowed(boolean commentAllowed) {
        return new JsonStringTemplateConfiguration(
                cacheEnabled,
                tailingCommaAllowed,
                extraCommaAllowed,
                commentAllowed
        );
    }
}
