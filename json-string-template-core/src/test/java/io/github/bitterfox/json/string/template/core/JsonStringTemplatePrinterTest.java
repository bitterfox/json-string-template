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

import static java.lang.StringTemplate.RAW;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.github.bitterfox.json.string.template.core.JsonPosition.EndOfStringTemplate;
import io.github.bitterfox.json.string.template.core.JsonPosition.FragmentPosition;
import io.github.bitterfox.json.string.template.core.JsonPosition.ValuePosition;

class JsonStringTemplatePrinterTest {

    @Test
    void test() {
        var template = RAW."""
                {
                    "test": \{0},
                    \{1}: \{2}
                }
                """;
        JsonStringTemplatePrinter printer = new JsonStringTemplatePrinter();
        printer.expressionEscapeChar = '$';

        assertEquals(
                """
                        {
                            "test": ${0},
                            ${1}: ${2}
                        }
                        """,
                printer.print(template, new EndOfStringTemplate(), true)
        );

        assertEquals(
                """
                        {
                            "test": ${0},
                            ^
                            ${1}: ${2}
                        }
                        """,
                printer.print(template, new FragmentPosition(0, 6), false)
        );
        assertEquals(
                """
                        {
                            "test": ${0},
                            ^
                        """,
                printer.print(template, new FragmentPosition(0, 6), true)
        );

        assertEquals(
                """
                        {
                            "test": ${0},
                            ${1}: ${2}
                                ^
                        }
                        """,
                printer.print(template, new FragmentPosition(2, 0), false)
        );
        assertEquals(
                """
                        {
                            "test": ${0},
                            ${1}: ${2}
                                ^
                        """,
                printer.print(template, new FragmentPosition(2, 0), true)
        );

        assertEquals(
                """
                        {
                            "test": ${0},
                                    ^^^^
                            ${1}: ${2}
                        }
                        """,
                printer.print(template, new ValuePosition(0), false)
        );
        assertEquals(
                """
                        {
                            "test": ${0},
                                    ^^^^
                        """,
                printer.print(template, new ValuePosition(0), true)
        );

        assertEquals(
                """
                        {
                            "test": ${0},
                            ${1}: ${2}
                                  ^^^^
                        }
                        """,
                printer.print(template, new ValuePosition(2), false)
        );
        assertEquals(
                """
                        {
                            "test": ${0},
                            ${1}: ${2}
                                  ^^^^
                        """,
                printer.print(template, new ValuePosition(2), true)
        );

        // line end
        assertEquals(
                """
                        {
                            "test": ${0},
                            ${1}: ${2}
                                      ^
                        }
                        """,
                printer.print(template, new FragmentPosition(3, 0), false)
        );
        assertEquals(
                """
                        {
                            "test": ${0},
                            ${1}: ${2}
                                      ^
                        """,
                printer.print(template, new FragmentPosition(3, 0), true)
        );
    }

    @Test
    void testRange() {
        var template = RAW."""
                {
                    "test": \{0},
                    \{1}: \{2}
                }
                """;
        JsonStringTemplatePrinter printer = new JsonStringTemplatePrinter();
        printer.expressionEscapeChar = '$';

        assertEquals(
                """
                        {
                        ^^
                            "test": ${0},
                        ^^^^^^^^^^^^^^^^^^
                            ${1}: ${2}
                        ^^^^^^^^^^^^^^^
                        }
                        ^^
                        """,
                printer.print(template, new JsonPositionRange(new FragmentPosition(0, 0), new FragmentPosition(100, 0)), false)
        );
        assertEquals(
                """
                        {
                        ^^
                            "test": ${0},
                        ^^^^^^^^^^^^^^^^^^
                            ${1}: ${2}
                        ^^^^^^^^^^^^^^^
                        }
                        ^^
                        """,
                printer.print(template, new JsonPositionRange(new FragmentPosition(0, 0), new FragmentPosition(100, 0)), true)
        );

        assertEquals(
                """
                        {
                            "test": ${0},
                            ^^^^^^
                            ${1}: ${2}
                        }
                        """,
                printer.print(template, new JsonPositionRange(new FragmentPosition(0, 6), new FragmentPosition(0, 11)), false)
        );
        assertEquals(
                """
                        {
                            "test": ${0},
                            ^^^^^^
                        """,
                printer.print(template, new JsonPositionRange(new FragmentPosition(0, 6), new FragmentPosition(0, 11)), true)
        );

        assertEquals(
                """
                        {
                            "test": ${0},
                            ^^^^^^^^^^^^
                            ${1}: ${2}
                        }
                        """,
                printer.print(template, new JsonPositionRange(new FragmentPosition(0, 6), new ValuePosition(0)), false)
        );
        assertEquals(
                """
                        {
                            "test": ${0},
                            ^^^^^^^^^^^^
                        """,
                printer.print(template, new JsonPositionRange(new FragmentPosition(0, 6), new ValuePosition(0)), true)
        );


        assertEquals(
                """
                        {
                            "test": ${0},
                            ^^^^^^^^^^^^^^
                            ${1}: ${2}
                        ^^^^^^^^^
                        }
                        """,
                printer.print(template, new JsonPositionRange(new FragmentPosition(0, 6), new FragmentPosition(2, 0)), false)
        );
        assertEquals(
                """
                        {
                            "test": ${0},
                            ^^^^^^^^^^^^^^
                            ${1}: ${2}
                        ^^^^^^^^^
                        """,
                printer.print(template, new JsonPositionRange(new FragmentPosition(0, 6), new FragmentPosition(2, 0)), true)
        );
    }

    @Test
    void testEscapedChar() {
        var template = RAW."""
                {
                    "te\tst": \{0},
                    \{1}: \{2}
                }
                """;
        JsonStringTemplatePrinter printer = new JsonStringTemplatePrinter();
        printer.expressionEscapeChar = '$';

        assertEquals(
                """
                        {
                            "te\\tst": ${0},
                               ^^
                            ${1}: ${2}
                        }
                        """,
                printer.print(template, new FragmentPosition(0, 9), false)
        );
        assertEquals(
                """
                        {
                            "te\\tst": ${0},
                               ^^
                        """,
                printer.print(template, new FragmentPosition(0, 9), true)
        );
    }
}
