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

package com.github.bitterfox.json.string.template.org.json;

import org.json.JSONArray;
import org.json.JSONObject;

import io.github.bitterfox.json.string.template.core.JsonStringTemplateConfiguration;
import io.github.bitterfox.json.string.template.core.JsonStringTemplateProcessor;

public class JsonStringTemplate {
    private static final JsonStringTemplateProcessor<Object> JSON =
            JsonStringTemplateProcessor.of(new OrgJsonJsonBridge());
    public static final JsonStringTemplateProcessor<JSONObject> JSON_O = JSON.andThen(JSONObject.class::cast);
    public static final JsonStringTemplateProcessor<JSONArray> JSON_A = JSON.andThen(JSONArray.class::cast);

    private static final JsonStringTemplateProcessor<Object> JSON_SPEC =
            JsonStringTemplateProcessor.of(new OrgJsonJsonBridge(), JsonStringTemplateConfiguration.JSON_SPEC);
    public static final JsonStringTemplateProcessor<JSONObject> JSON_SPEC_O = JSON_SPEC.andThen(JSONObject.class::cast);
    public static final JsonStringTemplateProcessor<JSONArray> JSON_SPEC_A = JSON_SPEC.andThen(JSONArray.class::cast);
}
