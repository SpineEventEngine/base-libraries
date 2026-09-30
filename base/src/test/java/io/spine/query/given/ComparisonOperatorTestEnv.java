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

package io.spine.query.given;

import com.google.protobuf.FieldMask;
import com.google.protobuf.StringValue;
import com.google.protobuf.util.Timestamps;
import org.junit.jupiter.params.provider.Arguments;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.stream.Stream;

import static io.spine.protobuf.AnyPacker.pack;

/**
 * Test values used in comparison tests of a {@link io.spine.query.ComparisonOperator}.
 */
@SuppressWarnings("unused") /* Serve as method sources */
public final class ComparisonOperatorTestEnv {

    private ComparisonOperatorTestEnv() {
    }

    public static Stream<Arguments> equalValues() {
        return Stream.of(equalPrimitives(), equalComparables(), equalMessages(), equalTimestamps());
    }

    public static Stream<Arguments> equalOrderableValues() {
        return Stream.of(equalPrimitives(), equalComparables(), equalTimestamps());
    }

    public static Stream<Arguments> notEqualValues() {
        return Stream.of(ascPrimitives(), ascComparables(), ascTimestamps(), differentMessages());
    }

    public static Stream<Arguments> greaterThanValues() {
        return Stream.of(descPrimitives(), descComparables(), descTimestamps());
    }

    public static Stream<Arguments> lessThanValues() {
        return Stream.of(ascPrimitives(), ascComparables(), ascTimestamps());
    }

    private static Arguments equalPrimitives() {
        return Arguments.of(50, 50);
    }

    private static Arguments equalComparables() {
        return Arguments.of(BigDecimal.valueOf(1000), BigDecimal.valueOf(1000));
    }

    private static Arguments equalMessages() {
        var message = pack(FieldMask.getDefaultInstance());
        var anotherMessage = pack(FieldMask.getDefaultInstance());
        return Arguments.of(message, anotherMessage);
    }

    private static Arguments differentMessages() {
        return Arguments.of(StringValue.of("A value"), StringValue.of("A very different value"));
    }

    private static Arguments equalTimestamps() {
        return Arguments.of(Timestamps.fromSeconds(111),
                            Timestamps.fromSeconds(111));
    }

    private static Arguments ascPrimitives() {
        return Arguments.of(1L, 2L);
    }

    private static Arguments ascComparables() {
        return Arguments.of("firstComparable", "secondComparable");
    }

    private static Arguments ascTimestamps() {
        return Arguments.of(Timestamps.fromSeconds(1),
                            Timestamps.fromSeconds(2));
    }

    private static Arguments descPrimitives() {
        return Arguments.of(100.0f, 77.0f);
    }

    private static Arguments descComparables() {
        return Arguments.of(BigInteger.valueOf(500), BigInteger.valueOf(499));
    }

    private static Arguments descTimestamps() {
        return Arguments.of(Timestamps.fromSeconds(44),
                            Timestamps.fromSeconds(22));

    }
}
