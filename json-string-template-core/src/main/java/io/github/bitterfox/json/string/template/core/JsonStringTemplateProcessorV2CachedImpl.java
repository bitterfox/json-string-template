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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class JsonStringTemplateProcessorV2CachedImpl<JSON> extends AbstractJsonStringTemplateProcessor<JSON> {
    // TODO Make this configurable
    private static final int CACHE_SIZE_LIMIT = 1000;

    private final JsonStringTemplateProcessorV2Impl<JSON> compileEngine;
    private final Map<CacheKey, CompiledJsonStringTemplate<JSON>> cache;

    private final AtomicInteger cacheMiss = new AtomicInteger();
    private final AtomicInteger cacheHit = new AtomicInteger();
    private final AtomicInteger cacheEvicted = new AtomicInteger();

    // Java uses the same instance of fragments of String Template expr?
    private static class CacheKey {
        private final List<String> fragments;

        public CacheKey(List<String> fragments) {
            this.fragments = fragments;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(fragments);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {return true;}
            if (o == null || getClass() != o.getClass()) {return false;}
            CacheKey cacheKey = (CacheKey) o;
            return fragments == cacheKey.fragments;
        }
    }

    public JsonStringTemplateProcessorV2CachedImpl(JsonBridge<JSON> compileEngine, JsonStringTemplateConfiguration config) {
        super(config);
        this.compileEngine = new JsonStringTemplateProcessorV2Impl<>(compileEngine, config);
        // synchronizedMap uses synchronized
        // This is not good for virtual thread, Java 21+
        // Let's consider read/write lock or implement concurrent LRU cache
        cache = Collections.synchronizedMap(
                new LinkedHashMap<>(16, 0.75f, false) {
                    @Override
                    protected boolean removeEldestEntry(
                            Entry<CacheKey, CompiledJsonStringTemplate<JSON>> eldest) {
                        if (size() > CACHE_SIZE_LIMIT) {
                            cacheEvicted.incrementAndGet();
                            return true;
                        }
                        return false;
                    }
                });
    }

    @Override
    public JSON process(StringTemplate stringTemplate) {
        return cache.compute(new CacheKey(stringTemplate.fragments()), (_, cached) -> {
            if (cached == null) {
                cacheMiss.incrementAndGet();
                return compileEngine.compile(stringTemplate);
            }

            cacheHit.incrementAndGet();
            return cached;
        }).apply(stringTemplate.values());
    }

    public int cacheMiss() {
        return cacheMiss.get();
    }
    public int cacheHit() {
        return cacheHit.get();
    }
    public int cacheEvicted() {
        return cacheEvicted.get();
    }
}
