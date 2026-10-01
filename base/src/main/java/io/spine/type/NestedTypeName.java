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

package io.spine.type;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import io.spine.value.StringTypeValue;

import java.io.Serial;

import static io.spine.type.TypeName.NESTED_TYPE_SEPARATOR;

/**
 * A simple name of a nested Protobuf type.
 *
 * <p>Consists of names of containing types and the name of the type, all separated with dots.
 */
public final class NestedTypeName extends StringTypeValue {

    @Serial
    private static final long serialVersionUID = 0L;

    private static final Joiner simpleNameJoiner = Joiner.on(NESTED_TYPE_SEPARATOR);
    private static final Joiner underscoreNameJoiner = Joiner.on("_");

    private final ImmutableList<String> names;

    private NestedTypeName(ImmutableList<String> names) {
        super(simpleNameJoiner.join(names));
        this.names = names;
    }

    /**
     * Obtains the {@code NestedTypeName} of the given type.
     */
    static NestedTypeName of(Type<?, ?> type) {
        ImmutableList.Builder<String> names = ImmutableList.builder();
        var unqualified = type.descriptor()
                              .getName();
        names.add(unqualified);
        var parent = type.containingType();
        while (parent.isPresent()) {
            var containingType = parent.get();
            names.add(containingType.descriptor().getName());
            parent = containingType.containingType();
        }
        var fullSimpleName = names.build().reverse();
        return new NestedTypeName(fullSimpleName);
    }

    /**
     * Obtains the name joined with underscores ({@code _}), as used in generated code in some
     * languages.
     */
    public String joinWithUnderscore() {
        return underscoreNameJoiner.join(names);
    }
}
