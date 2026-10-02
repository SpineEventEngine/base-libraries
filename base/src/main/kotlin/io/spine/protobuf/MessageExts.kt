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

@file:JvmName("Messages")

package io.spine.protobuf

import com.google.protobuf.Any
import com.google.protobuf.Message
import io.spine.annotation.Internal
import java.lang.reflect.Type
import kotlin.reflect.KClass

/**
 * Obtains the default instance of the passed message class.
 *
 * @param M the type of the message.
 * @return default instance of this message class.
 */
public val <M : Message> Class<M>.defaultInstance: M
    get() {
        @Suppress("UNCHECKED_CAST")
        return DefaultInstanceValue.get(this) as M
        // The previously used approach is commented out below.
        // Please do not remove until the `DefaultInstanceValue` object
        // goes out of the experimental status.
        //return defaultInstances.getUnchecked(this) as M
    }

/**
 * Associates default message instances with their classes using the [ClassValue] API.
 */
private object DefaultInstanceValue : ClassValue<Message>() {

    /**
     * Obtains a default instance for a message class.
     */
    override fun computeValue(type: Class<*>): Message {
        @Suppress("UNCHECKED_CAST") // It's OK: we control this private API.
        val messageType = type as Class<out Message>
        // It is safe to use the `Internal` utility class from Protobuf since it relies on
        // the fact that the generated class has the `getDefaultInstance()` static method.
        return com.google.protobuf.Internal.getDefaultInstance(messageType)
    }
}

/**
 * Creates a new builder for the passed message class.
 */
@Internal
@Suppress("TooGenericExceptionCaught") /*
  The caught exception is too broad because Kotlin does not have a multi-catch yet:
     https://discuss.kotlinlang.org/t/does-kotlin-have-multi-catch/486/39
  We expect `UncheckedExecutionException` or `NoSuchMethodException` if
  the passed class is just `Class<Message>` or another non-generated `Message` class
  that does not have a static method `getDefaultInstance()`.
*/
public fun <M : Message> builderFor(cls: Class<M>): Message.Builder {
    return try {
        cls.defaultInstance.toBuilder()
    } catch (e: Exception) {
        throw IllegalArgumentException(
            "Class `${cls.canonicalName}` must be a generated proto message.", e
        )
    }
}

/**
 * Creates a new builder for this message [Class].
 */
public fun Class<out Message>.newBuilder(): Message.Builder = builderFor(this)

/**
 * Creates a new builder for this message [KClass].
 */
public fun KClass<out Message>.newBuilder(): Message.Builder = builderFor(java)

/**
 * Ensures that the passed instance of `Message` is not an instance
 * of [com.google.protobuf.Any], and unwraps the message if `Any` is passed.
 */
public fun Message.ensureUnpacked(): Message =
    if (this is Any) {
        AnyPacker.unpack(this)
    } else {
        this
    }

/**
 * Verifies if the passed message object is its default state and is not `null`.
 *
 * @return `true` if the message is in the default state, `false` otherwise.
 */
public fun Message.isDefault(): Boolean = (defaultInstanceForType == this)

/**
 * Verifies if the passed message object is not its default state and is not `null`.
 *
 * @return `true` if the message is not in the default state, `false` otherwise.
 */
public fun Message.isNotDefault(): Boolean = !isDefault()

/**
 * Tells if this type is class of [Message].
 */
public fun Type.isMessageClass(): Boolean {
    return if (this is Class<*>) {
        Message::class.java.isAssignableFrom(this)
    } else {
        false
    }
}
