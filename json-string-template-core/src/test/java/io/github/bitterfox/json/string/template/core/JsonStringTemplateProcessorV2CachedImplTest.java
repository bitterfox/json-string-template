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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;

import org.junit.jupiter.api.Test;

class JsonStringTemplateProcessorV2CachedImplTest extends AbstractJsonStringTemplateProcessorTest {
    @SuppressWarnings("removal")
    public JsonStringTemplateProcessorV2CachedImplTest() {
        super(JsonStringTemplateProcessor.ofV2Cached(new JavaObjectJsonBridge()));
    }

    @Test
    void test() {
        JsonStringTemplateProcessorV2CachedImpl<Object> JSON =
                new JsonStringTemplateProcessorV2CachedImpl<>(new JavaObjectJsonBridge(), JsonStringTemplateConfiguration.ofDefault());

        for (int i = 0; i < 1000; i++) {
            Object o = JSON."""
                    {
                        "test": \{i}
                    }
                    """;
            assertEquals(Map.of("test", i), o);
        }

        assertEquals(1, JSON.cacheMiss());
        assertEquals(999, JSON.cacheHit());
        assertEquals(0, JSON.cacheEvicted());
    }

    @Test
    void testCacheDisabled() {
        JsonStringTemplateProcessorV2CachedImpl<Object> JSON =
                new JsonStringTemplateProcessorV2CachedImpl<>(
                        new JavaObjectJsonBridge(),
                        JsonStringTemplateConfiguration.ofDefault()
                                                       .disableCache());

        for (int i = 0; i < 1000; i++) {
            Object o = JSON."""
                    {
                        "test": \{i}
                    }
                    """;
            assertEquals(Map.of("test", i), o);
        }

        assertNull(JSON.cache);
        assertEquals(0, JSON.cacheMiss());
        assertEquals(0, JSON.cacheHit());
        assertEquals(0, JSON.cacheEvicted());
    }
}
