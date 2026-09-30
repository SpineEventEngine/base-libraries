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

package io.spine.code.proto;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import io.spine.base.Field;
import io.spine.base.FieldPath;
import io.spine.code.AbstractFieldName;

import java.util.List;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A name of a message field.
 */
@Immutable
public final class FieldName extends AbstractFieldName implements UnderscoredName {

    private static final long serialVersionUID = 0L;

    /**
     * The separator is an underscore or a digit.
     *
     * <p>A digit instead of an underscore should be kept in a word.
     * So, the second group is not just {@code (\\d)}.
     */
    private static final String WORD_SEPARATOR = "(_)|((?<=\\d)|(?=\\d))";
    private static final Pattern WORD_SEPARATOR_PATTERN = Pattern.compile(WORD_SEPARATOR);

    private FieldName(String value) {
        super(value);
    }

    /**
     * Creates a field name with the passed value.
     */
    public static FieldName of(String value) {
        checkNotEmptyOrBlank(value);
        return new FieldName(value);
    }

    /**
     * Creates a field name for the passed descriptor.
     */
    public static FieldName of(FieldDescriptorProto field) {
        checkNotNull(field);
        return new FieldName(field.getName());
    }

    /**
     * Obtains an immutable list of words used in the name of the field.
     *
     * <p>A word is a part of the name, the first letter of which should be capitalized
     * when converting {@linkplain #toCamelCase() to CamelCase}.
     *
     * <p>So, the name is split by:
     * <ul>
     *     <li>an underscore excluding it from a word;</li>
     *     <li>a digit leaving it in a word.</li>
     * </ul>
     *
     * <p>The name is split in such a manner, because the Protobuf compiler does in the same manner
     * during the conversion to CamelCase.
     *
     * @see <a href="https://github.com/protocolbuffers/protobuf/blob/master/src/google/protobuf/compiler/java/java_helpers.cc#L161">
     *         Protoc Camel Case</a>
     */
    @Override
    public List<String> words() {
        var words = WORD_SEPARATOR_PATTERN.split(value());
        var result = ImmutableList.copyOf(words);
        return result;
    }

    /**
     * Obtains the field name in {@code javaCase}.
     */
    public String javaCase() {
        var camelCase = toCamelCase();
        var result = Character.toLowerCase(camelCase.charAt(0)) + camelCase.substring(1);
        return result;
    }

    /**
     * Obtains this field name as a single-entry field path.
     */
    public FieldPath asPath() {
        var field = Field.named(value());
        return field.path();
    }
}
