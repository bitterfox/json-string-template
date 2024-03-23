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

public interface JsonStringTemplateConfigureable<THIS> {
    boolean cacheEnabled();
    default THIS enableCache() {
        return withCacheEnabled(true);
    }
    default THIS disableCache() {
        return withCacheEnabled(false);
    }
    THIS withCacheEnabled(boolean cacheEnabled);

    boolean tailingCommaAllowed();
    default THIS allowTailingComma() {
        return withTailingCommaAllowed(true);
    }
    default THIS disallowTailingComma() {
        return withTailingCommaAllowed(false);
    }
    THIS withTailingCommaAllowed(boolean tailingCommaAllowed);

    boolean extraCommaAllowed();
    default THIS allowExtraComma() {
        return withExtraCommaAllowed(true);
    }
    default THIS disallowExtraComma() {
        return withExtraCommaAllowed(false);
    }
    THIS withExtraCommaAllowed(boolean extraCommaAllowed);

    boolean commentAllowed();
    default THIS allowComment() {
        return withCommentAllowed(true);
    }
    default THIS disallowComment() {
        return withCommentAllowed(false);
    }
    THIS withCommentAllowed(boolean commentAllowed);
}
