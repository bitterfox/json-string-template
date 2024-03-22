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

package io.github.bitterfox.json.string.template.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.bitterfox.json.string.template.core.JsonStringTemplateConfiguration;
import io.github.bitterfox.json.string.template.core.JsonStringTemplateProcessor;

public class JsonStringTemplate {
    private static final ObjectMapper DEFAULT_MAPPER = new ObjectMapper();

    public static final JsonStringTemplateProcessor<JsonNode> JSON =
            JsonStringTemplateProcessor.of(new JacksonJsonBridge(DEFAULT_MAPPER));

    public static final JsonStringTemplateProcessor<JsonNode> JSON_SPEC =
            JsonStringTemplateProcessor.of(
                    new JacksonJsonBridge(DEFAULT_MAPPER),
                    JsonStringTemplateConfiguration.JSON_SPEC);
}
