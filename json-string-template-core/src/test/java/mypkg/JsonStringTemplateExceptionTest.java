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

package mypkg;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.bitterfox.json.string.template.core.AbstractJsonStringTemplateProcessorTest.JavaObjectJsonBridge;
import io.github.bitterfox.json.string.template.core.JsonStringTemplateConfiguration;
import io.github.bitterfox.json.string.template.core.JsonStringTemplateException;
import io.github.bitterfox.json.string.template.core.JsonStringTemplateProcessor;

class JsonStringTemplateExceptionTest {

    @Test
    void test() {
        try {
            JsonStringTemplateProcessor<Object> JSON =
                    JsonStringTemplateProcessor.of(new JavaObjectJsonBridge(),
                                                   JsonStringTemplateConfiguration.DEFAULT);

            Object json = JSON."_";
            fail();
        } catch (JsonStringTemplateException e) {
            assertEquals("mypkg.JsonStringTemplateExceptionTest.test(JsonStringTemplateExceptionTest.java:42)",
                         e.getStackTrace()[0].toString());
            assertEquals("io.github.bitterfox.json.string.template.core.JsonTokenizer.readNext(JsonTokenizer.java:96)",
                    e.getCause().getStackTrace()[0].toString());
        }
    }
}
