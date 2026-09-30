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

package io.spine.base;

import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.base.Errors.causeOf;
import static io.spine.base.Errors.fromThrowable;
import static io.spine.base.Identifier.newUuid;
import static io.spine.testing.TestValues.randomString;

@DisplayName("Errors utility class should")
class ErrorsTest extends UtilityClassTest<Errors> {

    ErrorsTest() {
        super(Errors.class);
    }

    @Nested
    @DisplayName("convert cause of throwable to `Error`")
    class GetCause {

        private Throwable cause;
        private Throwable throwable;

        @BeforeEach
        void createExceptions() {
            var causeMessage = randomString();
            cause = new RuntimeException(causeMessage);
            throwable = new IllegalStateException(cause);
        }

        @Test
        @DisplayName("with error code")
        void withCode() {
            var errorCode = 404;
            var error = causeOf(throwable, errorCode);

            assertHasCause(error);
            assertThat(error.getCode())
                    .isEqualTo(errorCode);
        }

        @Test
        @DisplayName("without error code")
        void withoutCode() {
            var error = causeOf(throwable);

            assertHasCause(error);
        }

        private void assertHasCause(Error error) {
            assertThat(error.getMessage())
                    .isEqualTo(cause.getMessage());
        }
    }


    @Test
    @DisplayName("convert throwable to `Error`")
    void convertThrowableToError() {
        var expected = newUuid();
        var throwable = new RuntimeException(expected);

        var error = fromThrowable(throwable);

        assertThat(error.getMessage())
                .isEqualTo(expected);
    }
}
