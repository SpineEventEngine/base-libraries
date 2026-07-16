/*
 * Copyright 2022, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.query;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

import static java.lang.String.format;

/**
 * Defines how the queried records are compared against the desired parameter values.
 *
 * <h3><a name="supported_types"><strong>Supported field types</strong></a></h3>
 *
 * <p>The equality comparisons support the fields of all types. The operation is performed
 * via the {@link Objects#equals} method. A {@code null} reference is considered equal
 * to another {@code null} reference.
 *
 * <p>Order-based comparison supports only the values of {@code Comparable} types and
 * {@link com.google.protobuf.Timestamp}s. When trying to compare unsupported types,
 * an {@code UnsupportedOperationException} is thrown.
 *
 * <p>It is required that the runtime Java class of the two compared values is the same. Otherwise,
 * an {@code IllegalArgumentException} is thrown.
 */
public enum ComparisonOperator {

    /**
     * The actual value must be equal to the value of the subject parameter.
     */
    EQUALS {
        @Override
        public boolean eval(@Nullable Object left, @Nullable Object right) {
            return Objects.equals(left, right);
        }

        @Override
        public String toString() {
            return "==";
        }
    },

    /**
     * The actual value must be less than the value of the subject parameter.
     */
    LESS_THAN {
        @Override
        public boolean eval(@Nullable Object left, @Nullable Object right) {
            return GREATER_THAN.eval(right, left);
        }

        @Override
        public String toString() {
            return "<";
        }
    },

    /**
     * The actual value must be less or equal to the value of the subject parameter.
     */
    LESS_OR_EQUALS {
        @Override
        public boolean eval(@Nullable Object left, @Nullable Object right) {
            return LESS_THAN.eval(left, right)
                    || EQUALS.eval(left, right);
        }

        @Override
        public String toString() {
            return "<=";
        }
    },

    /**
     * The actual value must be greater than the value of the subject parameter.
     */
    GREATER_THAN {
        @SuppressWarnings({
                "ChainOfInstanceofChecks", // Generic but limited operand types.
                "PatternMatchingInstanceof", // To keep generic for `Comparable<?>`.
                "rawtypes", "unchecked"    // Types are checked at runtime.
        })
        @Override
        public boolean eval(@Nullable Object left, @Nullable Object right) {
            if (left == null || right == null) {
                return false;
            }
            if (left.getClass() != right.getClass()) {
                throw new IllegalArgumentException(
                        format("Cannot compare an instance of %s to an instance of %s.",
                               left.getClass(),
                               right.getClass())
                );
            }
            if (left instanceof Timestamp firstT) {
                var secondT = (Timestamp) right;
                return Timestamps.compare(firstT, secondT) > 0;
            }
            if (left instanceof Comparable<?>) {
                Comparable cmpLeft = (Comparable<?>) left;
                Comparable cmpRight = (Comparable<?>) right;
                var comparisonResult = cmpLeft.compareTo(cmpRight);
                return comparisonResult > 0;
            }
            throw new UnsupportedOperationException(format(
                    "Comparison operations are not supported for type %s.",
                    left.getClass()
                        .getCanonicalName())
            );
        }

        @Override
        public String toString() {
            return ">";
        }
    },

    /**
     * The actual value must be greater or equal to the value of the subject parameter.
     */
    GREATER_OR_EQUALS {
        @Override
        public boolean eval(@Nullable Object left, @Nullable Object right) {
            return GREATER_THAN.eval(left, right)
                    || EQUALS.eval(left, right);
        }

        @Override
        public String toString() {
            return ">=";
        }
    };

    /**
     * Evaluates the expression of joining the given operands with a certain operator.
     *
     * @return {@code true} if the expression evaluates into {@code true}, {@code false} otherwise
     */
    public abstract boolean eval(@Nullable Object left, @Nullable Object right);
}
