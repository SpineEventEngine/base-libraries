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

package io.spine.annotation;

import org.junit.jupiter.api.DisplayName;

/**
 * This test suite documents the way the {@link Generated} annotation
 * can be used on a class.
 *
 * <p>This test suite does not run assertions because it does
 * not make much sense for annotations. Instead, it contains nested
 * static classes with the annotations applied.
 */
@SuppressWarnings("UnusedNestedClass") // Nested classes are annotation targets.
@DisplayName("`@Generated` annotation in Java should")
class GeneratedJavaSpec {

    /**
     * This class mimics a generated class that has one line annotation.
     */
    @Generated("With one line value")
    private static class SingleLineAnnotation {
    }

    /**
     * This class mimics a generated class annotated with an array of strings.
     */
    @Generated({"Line 1", "Line 2", "Line 3"})
    private static class ValueArrayAnnotation {
    }

    @Generated(
            value = {"With array", "of strings"},
            timestamp = "20:05", // could be any string, but the ISO format is preferred.
            comments = "Some comments"
    )
    @SuppressWarnings("EmptyClass")
    private static class AllArguments {
    }
}
