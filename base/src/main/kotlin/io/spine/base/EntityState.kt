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

package io.spine.base

import com.google.errorprone.annotations.Immutable

/**
 * A common interface for entity state messages.
 *
 * Any message that defines an `(entity)` option with a valid `kind` is an entity state.
 *
 * The `kind` of the entity state defines the actual interface of the entity state
 * that could be this interface or one that extends it.
 * If the `kind` is [ENTITY][io.spine.option.EntityOption.Kind.ENTITY], the generated
 * message will implement this interface. Otherwise, it would be one of the subinterfaces.
 * For example, if the `kind` is [PROJECTION][io.spine.option.EntityOption.Kind.PROJECTION],
 * the generated message will implement [ProjectionState].
 *
 * The field of the entity state message, which is declared first in the code of
 * the message, is treated as its [identifier][Identifier].
 *
 * This convention has two goals:
 *
 *  1. The definition of an entity state always starts with its ID with no
 *   extra Protobuf options.
 *
 *  2. Developers don't forget to specify which of the fields declared in Protobuf
 *   corresponds to the entity ID.
 *
 * During code generation, the CoreJvm Compiler substitutes the generic parameter
 * [I] with an actual type of the first field of the entity state message.
 *
 * @param I The type of entity identifiers.
 *   It could be one of the types described in the `IdType` enum.
 */
@Immutable
@Suppress("unused" /* The parameter type <I> is meant to be used in the generated code. */)
public interface EntityState<I : Any> : Routable

/**
 * An entity state of an aggregate.
 *
 * Messages that have `(entity)` option with `kind` set to
 * [AGGREGATE][io.spine.option.EntityOption.Kind.AGGREGATE]
 * are instances of this class.
 */
public interface AggregateState<I : Any> : EntityState<I>

/**
 * An entity state of a projection.
 *
 * Messages that have `(entity)` option with `kind` set to
 * [PROJECTION][io.spine.option.EntityOption.Kind.PROJECTION]
 * are instances of this interface.
 *
 * @param I The type of the projection identifiers.
 */
public interface ProjectionState<I : Any> : EntityState<I>

/**
 * Same as [ProjectionState].
 */
public typealias ViewState<I> = ProjectionState<I>

/**
 * An entity state of a process manager.
 *
 * Messages that have `(entity)` option with `kind` set to
 * [PROCESS_MANAGER][io.spine.option.EntityOption.Kind.PROCESS_MANAGER]
 * are instances of this interface.
 */
public interface ProcessManagerState<I : Any> : EntityState<I>
