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
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.escape.Escaper;

import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newLinkedHashMap;

/**
 * The stringifier for the {@code Map} classes.
 *
 * <p>The stringifier for the type of the elements in the map
 * should be registered in the {@code StringifierRegistry} class
 * for the correct usage of {@code MapStringifier}.
 *
 * <h1>Example</h1>
 * <pre>    {@code
 *   StringifierRegistry registry = StringifierRegistry.getInstance();
 *
 *   // The registration of the stringifier.
 *   Type type = Types.mapTypeOf(String.class, Long.class);
 *   registry.register(stringifier, type);
 *
 *   // Obtain already registered `MapStringifier`.
 *   Stringifier<Map<String, Long>> mapStringifier = registry.getStringifier(type);
 *
 *   // Convert to string.
 *   Map<String, Long> mapToConvert = newHashMap();
 *   mapToConvert.put("first", 1);
 *   mapToConvert.put("second", 2);
 *
 *   // The result is: \"first\":\"1\",\"second\":\"2\".
 *   String convertedString = mapStringifier.toString(mapToConvert);
 *
 *   // Convert from string.
 *   String stringToConvert = ...
 *   Map<String, Long> convertedMap = mapStringifier.fromString(stringToConvert); }
 * </pre>
 *
 * @param <K> the type of the keys in the map
 * @param <V> the type of the values in the map
 */
final class MapStringifier<K, V> extends Stringifier<Map<K, V>> {

    private static final char DEFAULT_ELEMENT_DELIMITER = ',';
    private static final char KEY_VALUE_DELIMITER = ':';

    /**
     * The delimiter for the passed elements in the {@code String} representation,
     * {@code DEFAULT_ELEMENT_DELIMITER} by default.
     */
    private final char delimiter;
    private final Escaper escaper;
    private final Splitter.MapSplitter splitter;
    private final Stringifier<K> keyStringifier;
    private final Stringifier<V> valueStringifier;

    /**
     * Creates a {@code MapStringifier}.
     *
     * <p>The specified delimiter is used for key-value separation
     * in {@code String} representation of the {@code Map}.
     *
     * @param keyClass   the class of the key elements
     * @param valueClass the class of the value elements
     * @param delimiter  the delimiter for the passed elements via string
     */
    MapStringifier(Class<K> keyClass, Class<V> valueClass, char delimiter) {
        super();
        this.keyStringifier = StringifierRegistry.getFor(keyClass);
        this.valueStringifier = StringifierRegistry.getFor(valueClass);
        this.delimiter = delimiter;
        this.escaper = Stringifiers.createEscaper(delimiter);
        this.splitter = createMapSplitter(Quoter.createDelimiterPattern(delimiter),
                                          createKeyValuePattern());
    }

    /**
     * Creates a {@code MapStringifier}.
     *
     * <p>The {@code DEFAULT_ELEMENT_DELIMITER} is used for key-value
     * separation in {@code String} representation of the {@code Map}.
     *
     * @param keyClass   the class of the key elements
     * @param valueClass the class of the value elements
     */
    MapStringifier(Class<K> keyClass, Class<V> valueClass) {
        this(keyClass, valueClass, DEFAULT_ELEMENT_DELIMITER);
    }

    private static Splitter.MapSplitter createMapSplitter(String bucketPattern,
                                                          String keyValuePattern) {
        var result = Splitter.onPattern(bucketPattern)
                             .withKeyValueSeparator(Splitter.onPattern(keyValuePattern));
        return result;
    }

    private static String createKeyValuePattern() {
        return Pattern.compile("(?<!\\\\)" + KEY_VALUE_DELIMITER)
                      .pattern();
    }

    @Override
    protected String toString(Map<K, V> obj) {
        Converter<String, String> quoter = Quoter.forMaps();
        var keyConverter = keyStringifier.andThen(quoter);
        var valueConverter = valueStringifier.andThen(quoter);

        Map<String, String> resultMap = newLinkedHashMap();
        for (var entry : obj.entrySet()) {
            var convertedKey = keyConverter.convert(entry.getKey());
            var convertedValue = valueConverter.convert(entry.getValue());
            resultMap.put(convertedKey, convertedValue);
        }
        var result = Joiner.on(delimiter)
                           .withKeyValueSeparator(KEY_VALUE_DELIMITER)
                           .join(resultMap);
        return result;
    }

    @Override
    protected Map<K, V> fromString(String s) {
        var escapedString = escaper.escape(s);
        var buckets = splitter.split(escapedString);
        var resultMap = convert(buckets);
        return resultMap;
    }

    private Map<K, V> convert(Map<String, String> buckets) {
        Converter<String, String> quoter = Quoter.forMaps();
        var keyConverter = quoter.reverse()
                                 .andThen(keyStringifier.reverse());
        var valueConverter = quoter.reverse()
                                   .andThen(valueStringifier.reverse());
        Map<K, V> resultMap = newHashMap();
        try {
            for (var bucket : buckets.entrySet()) {
                var convertedKey = keyConverter.convert(bucket.getKey());
                var convertedValue = valueConverter.convert(bucket.getValue());
                resultMap.put(convertedKey, convertedValue);
            }
            return resultMap;
        } catch (Throwable e) {
            throw new IllegalArgumentException("The exception occurred during the conversion", e);
        }
    }
}
