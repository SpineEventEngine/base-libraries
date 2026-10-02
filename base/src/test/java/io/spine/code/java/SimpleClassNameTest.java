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

package io.spine.code.java;

import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import com.google.protobuf.TimestampOrBuilder;
import io.spine.base.Error;
import io.spine.code.proto.FileName;
import io.spine.code.proto.FileSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link SimpleClassName}.
 *
 * <p>Even though the code where {@link SimpleClassName} resides no longer depends on the
 * {@code base} module, this test uses descriptors copied from the {@code base} stored in
 * resources. That's why the {@code ErrorProto} descriptor is available for these tests.
 */
@DisplayName("`SimpleClassName` should")
class SimpleClassNameTest {

    private static final FileSet mainSet = FileSet.load();
    private static final String ERROR_PROTO = "ErrorProto";

    private FileDescriptor errorProto;

    @SuppressWarnings("OptionalGetWithoutIsPresent") /* The file is present in resources. */
    @BeforeEach
    void setUp() {
        var errorFileName = FileName.from(Error.getDescriptor()
                                               .getFile()
                                               .toProto());
        errorProto = mainSet.tryFind(errorFileName)
                            .get();
    }

    @Test
    @DisplayName("obtain outer class name")
    void obtain_outer_class_name() {
        assertEquals(ERROR_PROTO, SimpleClassName.outerOf(errorProto.toProto())
                                                 .value());
    }

    @Test
    @DisplayName("obtain declared outer class name")
    void obtain_declared_outer_class_name() {
        var className = SimpleClassName.declaredOuterClassName(errorProto);

        assertTrue(className.isPresent());
        assertEquals(ERROR_PROTO, className.get()
                                           .value());
    }

    @Test
    @DisplayName("obtain default builder class name")
    void obtain_default_builder_class_name() {
        assertTrue(SimpleClassName.ofBuilder()
                                  .value()
                                  .contains(Message.Builder.class.getSimpleName()));
    }

    @Test
    @DisplayName("obtain name for message or builder")
    void obtain_name_for_message_or_builder() {
        assertEquals(TimestampOrBuilder.class.getSimpleName(),
                     SimpleClassName.messageOrBuilder(Timestamp.class.getSimpleName())
                                    .value());
    }

    @Test
    @DisplayName("obtain value by descriptor")
    void obtain_value_by_descriptor() {
        assertEquals(Timestamp.class.getSimpleName(),
                     SimpleClassName.ofMessage(Timestamp.getDescriptor())
                                    .value());
    }
}
