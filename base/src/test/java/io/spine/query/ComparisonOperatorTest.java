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

package io.spine.query;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Any;
import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.query.ComparisonOperator.EQUALS;
import static io.spine.query.ComparisonOperator.GREATER_OR_EQUALS;
import static io.spine.query.ComparisonOperator.GREATER_THAN;
import static io.spine.query.ComparisonOperator.LESS_OR_EQUALS;
import static io.spine.query.ComparisonOperator.LESS_THAN;
import static io.spine.testing.TestValues.nullRef;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`ComparisonOperator` should")
class ComparisonOperatorTest {

    private final ImmutableList<ComparisonOperator> orderingOperators =
            ImmutableList.of(GREATER_THAN, GREATER_OR_EQUALS, LESS_THAN, LESS_OR_EQUALS);

    @Nested
    @DisplayName("compare supported values A (smaller) and B (bigger):")
    @SuppressWarnings("DuplicateStringLiteralInspection")   // reusing the method sources.
    final class CompareSupported {

        @ParameterizedTest
        @DisplayName("(A = A) is `true`")
        @MethodSource("io.spine.query.given.ComparisonOperatorTestEnv#equalValues")
        void equalToTrue(Object left, Object right) {
            assertThat(EQUALS.eval(left, right)).isTrue();
            assertThat(EQUALS.eval(right, left)).isTrue();
        }

        @ParameterizedTest
        @DisplayName("(A = B) is `false`")
        @MethodSource("io.spine.query.given.ComparisonOperatorTestEnv#notEqualValues")
        void equalToFalse(Object left, Object right) {
            assertThat(EQUALS.eval(left, right)).isFalse();
            assertThat(EQUALS.eval(right, left)).isFalse();
        }

        @ParameterizedTest
        @DisplayName("(B > A) is `true`")
        @MethodSource("io.spine.query.given.ComparisonOperatorTestEnv#greaterThanValues")
        void greaterThan(Object left, Object right) {
            assertThat(GREATER_THAN.eval(left, right)).isTrue();
        }

        @ParameterizedTest
        @DisplayName("(A > B) is `false`")
        @MethodSource("io.spine.query.given.ComparisonOperatorTestEnv#greaterThanValues")
        void reverseGreaterThan(Object left, Object right) {
            assertThat(GREATER_THAN.eval(right, left)).isFalse();
        }

        @ParameterizedTest
        @DisplayName("(B >= A) is `true`")
        @MethodSource("io.spine.query.given.ComparisonOperatorTestEnv#greaterThanValues")
        void greaterOrEqualsForDifferentValues(Object left, Object right) {
            assertThat(GREATER_OR_EQUALS.eval(left, right)).isTrue();
        }

        @ParameterizedTest
        @DisplayName("(A >= B) is `false`")
        @MethodSource("io.spine.query.given.ComparisonOperatorTestEnv#greaterThanValues")
        void reverseGreaterOrEqualsForDifferentValues(Object left, Object right) {
            assertThat(GREATER_OR_EQUALS.eval(right, left)).isFalse();
        }

        @ParameterizedTest
        @DisplayName("(B >= B) is `true`")
        @MethodSource("io.spine.query.given.ComparisonOperatorTestEnv#equalOrderableValues")
        void greaterOrEqualsForSame(Object left, Object right) {
            assertThat(GREATER_OR_EQUALS.eval(left, right)).isTrue();
            assertThat(GREATER_OR_EQUALS.eval(right, left)).isTrue();
        }

        @ParameterizedTest
        @DisplayName("(A < B) is `true`")
        @MethodSource("io.spine.query.given.ComparisonOperatorTestEnv#lessThanValues")
        void lessThan(Object left, Object right) {
            assertThat(LESS_THAN.eval(left, right)).isTrue();
        }

        @ParameterizedTest
        @DisplayName("(B < A) is `false`")
        @MethodSource("io.spine.query.given.ComparisonOperatorTestEnv#lessThanValues")
        void reverseLessThan(Object left, Object right) {
            assertThat(LESS_THAN.eval(right, left)).isFalse();
        }

        @ParameterizedTest
        @DisplayName("(A <= B) is `true`")
        @MethodSource("io.spine.query.given.ComparisonOperatorTestEnv#lessThanValues")
        void lessOrEqualForDifferentValues(Object left, Object right) {
            assertThat(LESS_OR_EQUALS.eval(left, right)).isTrue();
        }

        @ParameterizedTest
        @DisplayName("(B <= A) is `false`")
        @MethodSource("io.spine.query.given.ComparisonOperatorTestEnv#lessThanValues")
        void reverseLessOrEqualForDifferentValues(Object left, Object right) {
            assertThat(LESS_OR_EQUALS.eval(right, left)).isFalse();
        }

        @ParameterizedTest
        @DisplayName("(A <= A) is `true`")
        @MethodSource("io.spine.query.given.ComparisonOperatorTestEnv#equalOrderableValues")
        void lessOrEqualForSame(Object left, Object right) {
            assertThat(LESS_OR_EQUALS.eval(left, right)).isTrue();
            assertThat(LESS_OR_EQUALS.eval(right, left)).isTrue();
        }
    }

    @Nested
    @DisplayName("reject values in `>`, `>=`, `<` and `<=` operations")
    final class Reject {

        @Test
        @DisplayName("by throwing `IllegalArgumentException` " +
                "if the types of values are not the same")
        void notSame() {
            Object left = Timestamp.getDefaultInstance();
            Object right = 0;
            for (var operator : orderingOperators) {
                assertThrows(IllegalArgumentException.class,
                             () -> operator.eval(left, right));
            }
        }

        @Test
        @DisplayName("by throwing an `UnsupportedOperationException` " +
                "if their types types are neither `Comparable` nor `Timestamp`")
        void unsupported() {
            Object left = Any.getDefaultInstance();
            Object right = Any.getDefaultInstance();
            for (var operator : orderingOperators) {
                assertThrows(UnsupportedOperationException.class,
                             () -> operator.eval(left, right));
            }
        }
    }

    @Test
    @DisplayName("return `false` in `>`, `>=`, `<` and `<=` if any of the operands is `null`")
    void orderingOperatorsReturnFalseForTwoNulls() {
        Timestamp nullOperand = nullRef();
        var anotherOperand = Timestamp.getDefaultInstance();
        for (var operator : orderingOperators) {
            assertThat(operator.eval(nullOperand, anotherOperand)).isFalse();
            assertThat(operator.eval(anotherOperand, nullOperand)).isFalse();
        }
    }

    @Test
    @DisplayName("return `true` for (`null` = `null`)")
    void nullEqualsNull() {
        assertThat(EQUALS.eval(nullRef(), nullRef())).isTrue();
    }
}
