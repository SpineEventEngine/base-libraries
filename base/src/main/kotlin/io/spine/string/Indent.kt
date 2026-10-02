/*
 * Copyright 2026 CodeMatters, Lda.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package io.spine.string

/**
 * An increment of an indentation to be used for formatting texts.
 */
public data class Indent(

    /**
     * A positive number of space characters to be used for the indentation increment.
     */
    public val size: Int = DEFAULT_JAVA_INDENT_SIZE
) {

    init {
        require(size > 0) { "The `size` must be positive, but was $size."}
    }

    /**
     * The value of the indentation increment.
     */
    public val value: String = " ".repeat(size)

    /**
     * Obtains the value of this indentation.
     */
    override fun toString(): String = value

    /**
     * Repeats this indentation [n] times.
     *
     * @throws [IllegalArgumentException] when [n] < 0.
     */
    public fun repeat(n: Int): String {
        require(n >= 0) { "Count `n` must be non-negative, but was $n."}
        return value.repeat(n)
    }

    /**
     * Same as [repeat].
     */
    public fun atLevel(n: Int): String = repeat(n)

    public companion object {

        /**
         * The default size of indentation used in the Java code.
         */
        public const val DEFAULT_JAVA_INDENT_SIZE: Int = 4

        /**
         * The default size of indentation used in the Proto Text output.
         *
         * Two spaces are used for indentation in `TextFormat.TextGenerator.indent()`,
         * which is private in the Protobuf library.
         *
         * @see <a href="https://protobuf.dev/reference/protobuf/textformat-spec/">Protobuf
         * Text Format Language Specification</a>
         */
        public const val DEFAULT_PROTO_TEXT_INDENT_SIZE: Int = 2

        /**
         * Default indent for Java code.
         */
        public val defaultJavaIndent: Indent by lazy {
            Indent(DEFAULT_JAVA_INDENT_SIZE)
        }

        /**
         * Default indent for Proto Text output.
         *
         * @see DEFAULT_PROTO_TEXT_INDENT_SIZE
         * @see <a href="https://protobuf.dev/reference/protobuf/textformat-spec/">Protobuf
         * Text Format Language Specification</a>
         */
        public val defaultProtoTextIndent: Indent by lazy {
            Indent(DEFAULT_PROTO_TEXT_INDENT_SIZE)
        }
    }
}
