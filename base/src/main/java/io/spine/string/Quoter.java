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

package io.spine.string;

import com.google.common.base.Converter;

import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static java.util.regex.Pattern.compile;

/**
 * Encloses and discloses the {@code String} objects with double quotes.
 */
abstract class Quoter extends Converter<String, String> {

    private static final String BACKSLASH_QUOTE = "\\\"";
    static final String BACKSLASH = "\\\\";
    static final char QUOTE_CHAR = '"';
    private static final String DELIMITER_PATTERN_PREFIX = "(?<!" + BACKSLASH + ')' + BACKSLASH;

    @Override
    protected String doForward(String s) {
        return quote(s);
    }

    @Override
    protected String doBackward(String s) {
        checkNotNull(s);
        return unquote(s);
    }

    /**
     * Prepends quote characters in the given string with two leading backslashes,
     * and then wraps the string into quotes.
     */
    abstract String quote(String stringToQuote);

    /**
     * Unquotes the passed string and removes double backslash prefixes for quote symbols
     * found inside the passed value.
     */
    abstract String unquote(String value);

    /**
     * Creates the pattern to match the escaped delimiters.
     *
     * @param delimiter the character to match
     * @return the created pattern
     */
    static String createDelimiterPattern(char delimiter) {
        var quotedDelimiter = Pattern.quote(String.valueOf(delimiter));
        var result = compile(DELIMITER_PATTERN_PREFIX + quotedDelimiter).pattern();
        return result;
    }

    /**
     * Returns the {@code MapQuoter} instance.
     */
    static Quoter forMaps() {
        return MapQuoter.INSTANCE;
    }

    /**
     * Returns the {@code ListQuoter} instance.
     */
    static Quoter forLists() {
        return ListQuoter.INSTANCE;
    }

    static String unquoteValue(String value, Pattern pattern) {
        checkQuoted(value);
        var unquoted = value.substring(2, value.length() - 2);
        var unescaped = pattern.matcher(unquoted)
                               .replaceAll("");
        return unescaped;
    }

    /**
     * Throws IllegalArgumentException if the passed char sequence is not wrapped into {@code \"}.
     */
    private static void checkQuoted(String str) {
        if (!(str.startsWith(BACKSLASH_QUOTE)
                && str.endsWith(BACKSLASH_QUOTE))) {
            throw newIllegalArgumentException("The passed string is not quoted: `%s`.", str);
        }
    }
}
