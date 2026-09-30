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

package io.spine.util;

import com.google.common.testing.NullPointerTester;
import io.spine.testing.TestValues;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.testing.Assertions.assertIllegalArgument;
import static io.spine.testing.Assertions.assertIllegalState;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static io.spine.util.Exceptions.newIllegalStateException;
import static io.spine.util.Exceptions.unsupported;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`Exceptions` utility class should")
class ExceptionsTest extends UtilityClassTest<Exceptions> {

    ExceptionsTest() {
        super(Exceptions.class);
    }

    @Override
    protected void configure(NullPointerTester tester) {
        tester.setDefault(Exception.class, new RuntimeException(""))
              .setDefault(Throwable.class, new Error())
              .testAllPublicStaticMethods(Exceptions.class);
    }

    @Nested
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @DisplayName("throw UnsupportedOperationException")
    class ThrowsUnsupported {

        @Test
        @DisplayName("without message")
        void noParams() {
            assertThrows(UnsupportedOperationException.class, Exceptions::unsupported);
        }

        @Test
        @DisplayName("with message")
        void withMessage() {
            assertThrows(
                    UnsupportedOperationException.class,
                    () -> unsupported(TestValues.randomString())
            );
        }

        @Test
        @DisplayName("with formatted message")
        void formattedMessage() {
            var arg1 = getClass().getCanonicalName();
            var arg2 = 100500L;
            var exception = assertThrows(
                    UnsupportedOperationException.class,
                    () -> unsupported("%s %d", arg1, arg2));
            var exceptionMessage = exception.getMessage();
            assertTrue(exceptionMessage.contains(arg1));
            assertTrue(exceptionMessage.contains(String.valueOf(arg2)));
        }
    }

    @Nested
    @DisplayName("throw `IllegalArgumentException` with")
    @SuppressWarnings({"ResultOfMethodCallIgnored", "ThrowableNotThrown"})
    class ThrowIAE {

        @Test
        @DisplayName("formatted message")
        void formattedMessage() {
            assertIllegalArgument(
                    () -> newIllegalArgumentException("%d, %d, %s kaboom", 1, 2, "three"));
        }

        @Test
        @DisplayName("formatted message with cause")
        void messageAndCause() {
            assertIllegalArgument(() -> newIllegalArgumentException(
                    new RuntimeException("checking"), "%s", "stuff")
            );
        }
    }

    @Nested
    @DisplayName("throw `IllegalStateException` with")
    @SuppressWarnings({"ResultOfMethodCallIgnored", "ThrowableNotThrown"})
    class ThrowISE {

        @Test
        @DisplayName("formatted message")
        void formattedMessage() {
            assertIllegalState(() -> newIllegalStateException("%s check %s", "state", "failed"));
        }

        @Test
        @DisplayName("formatted message with cause")
        void messageWithCause() {
            assertIllegalState(() -> newIllegalStateException(
                    new RuntimeException(getClass().getSimpleName()), "%s %s", "taram", "param")
            );
        }
    }
}
