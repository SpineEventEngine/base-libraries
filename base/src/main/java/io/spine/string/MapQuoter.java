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
 * The {@code Quoter} for the {@code Map}.
 */
final class MapQuoter extends Quoter {

    static final MapQuoter INSTANCE = new MapQuoter();

    private static final String QUOTE_PATTERN = "((?=[^\\\\])[^\\w])";
    private static final Pattern DOUBLE_BACKSLASH_PATTERN = compile(BACKSLASH);

    @Override
    String quote(String stringToQuote) {
        checkNotNull(stringToQuote);
        var matcher = compile(QUOTE_PATTERN).matcher(stringToQuote);
        var unslashed = matcher.find()
                        ? matcher.replaceAll(BACKSLASH + matcher.group())
                        : stringToQuote;
        var result = QUOTE_CHAR + unslashed + QUOTE_CHAR;
        return result;
    }

    @Override
    String unquote(String value) {
        return unquoteValue(value, DOUBLE_BACKSLASH_PATTERN);
    }
}
