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

package io.github.bitterfox.json.string.template.base;

import io.github.bitterfox.json.string.template.base.JsonToken.JTArrayClose;
import io.github.bitterfox.json.string.template.base.JsonToken.JTArrayOpen;
import io.github.bitterfox.json.string.template.base.JsonToken.JTColon;
import io.github.bitterfox.json.string.template.base.JsonToken.JTComma;
import io.github.bitterfox.json.string.template.base.JsonToken.JTFalse;
import io.github.bitterfox.json.string.template.base.JsonToken.JTJavaObject;
import io.github.bitterfox.json.string.template.base.JsonToken.JTNull;
import io.github.bitterfox.json.string.template.base.JsonToken.JTNumber;
import io.github.bitterfox.json.string.template.base.JsonToken.JTObjectClose;
import io.github.bitterfox.json.string.template.base.JsonToken.JTObjectOpen;
import io.github.bitterfox.json.string.template.base.JsonToken.JTString;
import io.github.bitterfox.json.string.template.base.JsonToken.JTTrue;

public sealed interface JsonToken
        permits JTObjectOpen,
                JTObjectClose,
                JTArrayOpen,
                JTArrayClose,
                JTComma,
                JTColon,
                JTString,
                JTNumber,
                JTTrue,
                JTFalse,
                JTNull,
                JTJavaObject {

    // {
    record JTObjectOpen() implements JsonToken {}
    // }
    record JTObjectClose() implements JsonToken {}
    // [
    record JTArrayOpen() implements JsonToken {}
    // ]
    record JTArrayClose() implements JsonToken {}
    // ,
    record JTComma() implements JsonToken {}
    // :
    record JTColon() implements JsonToken {}
    // "..."
    record JTString(String str) implements JsonToken {}
    // number
    record JTNumber(String number) implements JsonToken {}
    // true
    record JTTrue() implements JsonToken {}
    // false
    record JTFalse() implements JsonToken {}
    // null
    record JTNull() implements JsonToken {}

    // String template value
    record JTJavaObject(Object o) implements JsonToken {}
}
