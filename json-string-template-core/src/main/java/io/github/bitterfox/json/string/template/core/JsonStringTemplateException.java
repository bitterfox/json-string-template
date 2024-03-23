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

import java.util.ArrayList;
import java.util.List;

public class JsonStringTemplateException extends RuntimeException {
    private static final String PACKAGE_ROOT = "io.github.bitterfox.json.string.template";

    private static final JsonStringTemplatePrinter PRINTER = new JsonStringTemplatePrinter();

    public enum Phase {
        LEXER_SCANNER,
        LEXER_TOKENIZER,
        PARSER,
        COMPILER,
        UNKNOWN
    }

    private final Phase phase;
    private final String code;
    private StackTraceElement[] stackTrace;

    public JsonStringTemplateException(String message) {
        super(message);
        phase = Phase.UNKNOWN;
        code = "";
        createInternalException();
    }

    public JsonStringTemplateException(String message, Phase phase, StringTemplate template, JsonPosition position) {
        super(message);
        this.phase = phase;
        this.code = PRINTER.print(template, position, true);
        createInternalException();

    }

    public JsonStringTemplateException(String message, Phase phase, StringTemplate template, JsonPositionRange position) {
        super(message);
        this.phase = phase;
        this.code = PRINTER.print(template, position, true);
        createInternalException();
    }

    private void createInternalException() {
        StackTraceElement[] stackTrace = super.getStackTrace();
        List<StackTraceElement> internal = new ArrayList<>(stackTrace.length);
        List<StackTraceElement> external = new ArrayList<>(stackTrace.length);
        for (StackTraceElement e : stackTrace) {
            external.add(e);
            if (e.getClassName().startsWith(PACKAGE_ROOT)) {
                internal.addAll(external);
                external.clear();
            }
        }
        this.setStackTrace(external.toArray(StackTraceElement[]::new));

        InternalException cause = new InternalException(super.getMessage());
        cause.setStackTrace(internal.toArray(StackTraceElement[]::new));
        this.initCause(cause);
    }

    @Override
    public String getMessage() {
        return STR."""
                [\{phase}]: \{super.getMessage()}
                \{code}""";
    }

    public static class InternalException extends RuntimeException {
        public InternalException(String message) {
            super(message);
        }
    }
}
