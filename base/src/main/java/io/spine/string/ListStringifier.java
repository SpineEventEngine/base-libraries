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

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * The stringifier for the {@code List} classes.
 *
 * <p> The stringifier for the type of the elements in the list
 * should be registered in the {@code StringifierRegistry} class
 * for the correct usage of the {@code ListStringifier}.
 *
 * <h1>Example</h1>
 * <pre>    {@code
 *   // Stringifier creation.
 *   final Stringifier<List<Integer>> listStringifier = Stringifiers.listStringifier();
 *
 *   // The registration of the stringifier.
 *   final Stringifier<List<Integer>> listStringifer = Stringifiers.listStringifier();
 *   final Type type = Types.listTypeOf(Integer.class);
 *   StringifierRegistry.getInstance().register(listStringifier, type);
 *
 *   // Obtain already registered stringifier.
 *   final Stringifier<List<Integer>> listStringifier = StringifierRegistry.getInstance()
 *                                                                         .getStringifier(type);
 *
 *   // Convert to string.
 *   final List<Integer> listToConvert = newArrayList(1, 2, 3);
 *
 *   // The result is: \"1\",\"2\",\"3\".
 *   final String convertedString = listStringifer.toString(listToConvert);
 *
 *   // Convert from string.
 *   final String stringToConvert = ...
 *   final List<Integer> convertedList = listStringifier.fromString(stringToConvert);}
 * </pre>
 *
 * @param <T> the type of the elements in the list.
 */
final class ListStringifier<T> extends Stringifier<List<T>> {

    private static final char DEFAULT_ELEMENT_DELIMITER = ',';

    /**
     * The delimiter for the passed elements in the {@code String} representation,
     * {@code DEFAULT_ELEMENT_DELIMITER} by default.
     */
    private final char delimiter;
    private final Escaper escaper;
    private final Splitter splitter;
    private final Stringifier<T> elementStringifier;

    /**
     * Creates a {@code ListStringifier}.
     *
     * <p>The specified delimiter is used for element separation
     * in the {@code String} representation of the {@code List}.
     *
     * @param listGenericClass the class of the list elements
     * @param delimiter        the delimiter for the passed elements via string
     */
    ListStringifier(Class<T> listGenericClass, char delimiter) {
        super();
        this.elementStringifier = StringifierRegistry.getFor(listGenericClass);
        this.delimiter = delimiter;
        this.escaper = Stringifiers.createEscaper(delimiter);
        this.splitter = Splitter.onPattern(Quoter.createDelimiterPattern(delimiter));
    }

    /**
     * Creates a {@code ListStringifier}.
     *
     * <p>The {@code DEFAULT_ELEMENT_DELIMITER} is used for element
     * separation in the {@code String} representation of the {@code List}.
     *
     * @param listGenericClass the class of the list elements
     */
    ListStringifier(Class<T> listGenericClass) {
        this(listGenericClass, DEFAULT_ELEMENT_DELIMITER);
    }

    @Override
    protected String toString(List<T> list) {
        Converter<String, String> quoter = Quoter.forLists();
        List<String> convertedItems = new ArrayList<>();
        for (var item : list) {
            var convertedItem = elementStringifier.andThen(quoter)
                                                  .convert(item);
            convertedItems.add(convertedItem);
        }
        var result = Joiner.on(delimiter)
                           .join(convertedItems);
        return result;
    }

    @Override
    protected List<T> fromString(String s) {
        var escapedString = escaper.escape(s);
        List<String> items = newArrayList(splitter.split(escapedString));
        Converter<String, String> quoter = Quoter.forLists();
        var converter = quoter.reverse()
                              .andThen(elementStringifier.reverse());
        List<T> result = newArrayList();
        for (var item : items) {
            var convertedItem = converter.convert(item);
            result.add(convertedItem);
        }
        return result;
    }
}
