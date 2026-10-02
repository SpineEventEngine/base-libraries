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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.base.Identifier.newUuid;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`UnknownTypeException` should")
class UnknownTypeExceptionTest {

    @Test
    @DisplayName("have constructor with type name")
    void ctorWithTypeName() {
        var str = newUuid();
        var exception = new UnknownTypeException(str);

        assertTrue(exception.getMessage()
                            .contains(str));
    }

    @Test
    @DisplayName("have constructor with type name and cause")
    void ctorWithTypeAndCause() {
        var str = newUuid();
        var cause = new RuntimeException("");
        var exception = new UnknownTypeException(str, cause);

        assertTrue(exception.getMessage()
                            .contains(str));
        assertEquals(cause, exception.getCause());
    }
}
