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

import io.spine.annotation.Internal;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A subscribable message field that can be passed to subscription filters.
 *
 * <p>For each message that can be subscribed to, and also for any number of configured additional
 * types, the Spine routines will generate a nested {@code Field} type that exposes all message
 * fields as {@code SubscribableField} instances. Example:
 * <pre>
 * // Given message declarations.
 * message OrderView {
 *     option (entity).kind = PROJECTION;
 *
 *     OrderId id = 1;
 *     // ...
 * }
 *
 * message OrderId {
 *     string value = 1;
 * }
 *
 * // The following Java class will be generated.
 * public final class OrderView // implements Message, etc. {
 *
 *     // ...
 *
 *     public static final class Field {
 *
 *         private Field {
 *             // Prevent instantiation.
 *         }
 *
 *         public static OrderIdField id() {
 *             return new OrderIdField(...);
 *         }
 *
 *         public static final class OrderIdField extends EntityStateField {
 *
 *             private OrderIdField(...) {
 *                 // Instantiation is allowed only inside the `Field` class.
 *             }
 *
 *             public EntityStateField value() {
 *                 return new EntityStateField(...);
 *             }
 *         }
 *     }
 * }
 * </pre>
 *
 * <p>The values retrieved via methods of the {@code Field} type may then be passed to a client to
 * form a subscription request.
 *
 * <p>The class descendants differentiate between the various field types to enable the typed
 * filter creation on the client side.
 *
 * <p>See the Spine code generation routines in the {@code tool-base} module for extensive details
 * on how the types are generated.
 *
 * @apiNote In the generated code this class is, among others, inherited by the types that
 *        declare nested message fields as own public instance methods, as follows:
 *        <pre>
 *        public EntityStateField someFieldName() {...}
 *        </pre>
 *        Thus, the {@code SubscribableField} class has to avoid name clashes with proto fields
 *        declared this way, hence the otherwise redundant "get-" prefix on the {@link #getField()}
 *        method. For the same reason the class does not inherit from
 *        {@link io.spine.value.ValueHolder}.
 */
@SuppressWarnings("AbstractClassWithoutAbstractMethods")
// Prevent instantiation in favor of concrete subclasses.
public abstract class SubscribableField {

    private final Field field;

    protected SubscribableField(Field field) {
        this.field = checkNotNull(field);
    }

    @Internal
    public Field getField() {
        return field;
    }
}
