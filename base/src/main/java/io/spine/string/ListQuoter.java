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

import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.regex.Pattern.compile;

/**
 * The {@code Quoter} for the {@code List}.
 */
final class ListQuoter extends Quoter {

    static final Quoter INSTANCE = new ListQuoter();

    private static final String BACKSLASH_PATTERN_VALUE = "\\\\\\\\";
    private static final Pattern BACKSLASH_LIST_PATTERN = compile(BACKSLASH_PATTERN_VALUE);
    private static final String ESCAPED_QUOTE = BACKSLASH + QUOTE_CHAR;
    private static final String QUOTE = String.valueOf(QUOTE_CHAR);
    private static final Pattern QUOTE_PATTERN = compile(QUOTE);

    @Override
    String quote(String stringToQuote) {
        checkNotNull(stringToQuote);
        var escaped = QUOTE_PATTERN.matcher(stringToQuote)
                                   .replaceAll(ESCAPED_QUOTE);
        var result = QUOTE_CHAR + escaped + QUOTE_CHAR;
        return result;
    }

    @Override
    String unquote(String value) {
        return unquoteValue(value, BACKSLASH_LIST_PATTERN);
    }
}
