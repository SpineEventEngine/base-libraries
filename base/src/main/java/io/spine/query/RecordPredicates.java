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

import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Message;

import java.util.function.UnaryOperator;

/**
 * A lambda expression that groups additional query predicates to join
 * to some existing {@link RecordQuery}.
 *
 * @param <I>
 *         the type of identifiers of queried records
 * @param <R>
 *         the type of queried records
 */
@Immutable
public interface RecordPredicates<I, R extends Message>
        extends UnaryOperator<RecordQueryBuilder<I, R>> {
}
