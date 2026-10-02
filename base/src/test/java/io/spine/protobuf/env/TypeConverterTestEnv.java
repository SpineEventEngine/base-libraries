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

package io.spine.protobuf.env;

import com.google.protobuf.StringValue;
import io.spine.base.ListOfAnys;
import io.spine.base.MapOfAnys;
import io.spine.protobuf.AnyPacker;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Test environment for {@link io.spine.protobuf.TypeConverterTest TypeConverterTest}.
 */
public class TypeConverterTestEnv {

    /**
     * Prevents instantiation of this utility class.
     */
    private TypeConverterTestEnv() {
    }

    /**
     * Converts the given list of {@link String} to {@link ListOfAnys}.
     */
    public static ListOfAnys toProtoList(List<String> list) {
        var anys = list.stream()
                .map(s -> AnyPacker.pack(StringValue.of(s)))
                .collect(toList());
        return ListOfAnys.newBuilder()
                .addAllValue(anys)
                .build();
    }

    /**
     * Converts the given map of {@link String} to {@link MapOfAnys}.
     */
    public static MapOfAnys toProtoMap(Map<String, String> map) {
        var entries = map.entrySet()
                .stream()
                .map(e -> {
                    var key = AnyPacker.pack(StringValue.of(e.getKey()));
                    var value = AnyPacker.pack(StringValue.of(e.getValue()));
                    return MapOfAnys.Entry.newBuilder()
                            .setKey(key)
                            .setValue(value)
                            .build();
                })
                .collect(toList());
        return MapOfAnys.newBuilder()
                .addAllEntry(entries)
                .build();
    }
}
