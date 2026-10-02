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

import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.StringValue;
import com.google.protobuf.UInt64Value;
import io.spine.option.EntityOption;
import io.spine.option.IfMissingOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.testing.Assertions.assertIllegalArgument;
import static io.spine.testing.DisplayNames.NOT_ACCEPT_NULLS;
import static io.spine.type.Assertions.assertUnknownType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Provides only class-level tests.
 *
 * <p>Other methods of {@link TypeName} are just over {@link TypeUrl} that are tested by
 * its own set of tests.
 */
@DisplayName("`TypeName` should")
class TypeNameTest {

    @Test
    @DisplayName(NOT_ACCEPT_NULLS)
    void npeCheck() {
        new NullPointerTester()
                .setDefault(Descriptor.class, EntityOption.getDefaultInstance()
                                                          .getDescriptorForType())
                .testAllPublicStaticMethods(TypeName.class);
    }

    @Test
    @DisplayName("reject empty name")
    void rejectEmptyName() {
        assertIllegalArgument(() -> TypeName.of(""));
    }

    @Test
    @DisplayName("return simple type name")
    void simpleTypeName() {
        assertEquals(StringValue.class.getSimpleName(),
                     TypeName.of(StringValue.class)
                             .simpleName());
    }

    @Test
    @DisplayName("return simple name if no package")
    void simpleNameIfNoPackage() {
        // A msg type without Protobuf package
        var name = IfMissingOption.class.getSimpleName();
        var typeUrl = TypeName.of(name).toUrl();

        var actual = TypeName.from(typeUrl).simpleName();

        assertEquals(name, actual);
    }

    @Nested
    @DisplayName("obtain instance")
    class ObtainInstance {

        @Test
        @DisplayName("for `Message`")
        void forMessage() {
            var typeName = TypeName.of(StringValue.getDefaultInstance());
            assertNotNull(typeName);
            assertEquals(StringValue.class.getSimpleName(), typeName.simpleName());
        }

        @Test
        @DisplayName("for Java class")
        void forJavaClass() {
            var typeName = TypeName.of(StringValue.class);
            assertNotNull(typeName);
            assertEquals(StringValue.class.getSimpleName(), typeName.simpleName());
        }

        @Test
        @DisplayName("by descriptor")
        void byDescriptor() {
            var typeName = TypeName.from(UInt64Value.getDescriptor());
            assertNotNull(typeName);
            assertEquals(UInt64Value.class.getSimpleName(), typeName.simpleName());
        }
    }

    @Test
    @DisplayName("provide proto descriptor")
    void descriptorByTypeName() {
        var typeName = TypeName.of("spine.test.types.KnownTask");
        var typeDescriptor = typeName.messageDescriptor();
        assertNotNull(typeDescriptor);
        assertEquals(typeName.value(), typeDescriptor.getFullName());
    }

    @Test
    @DisplayName("fail to find invalid type descriptor")
    void invalidTypeDescriptor() {
        var invalidTypeName = TypeName.of("no.such.package.InvalidType");
        assertUnknownType(invalidTypeName::genericDescriptor);
    }
}
