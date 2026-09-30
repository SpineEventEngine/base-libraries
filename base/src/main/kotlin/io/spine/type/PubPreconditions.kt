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

@file:JvmName("PubPreconditions")

package io.spine.type

import com.google.errorprone.annotations.CanIgnoreReturnValue
import com.google.protobuf.Message
import io.spine.annotation.Internal
import io.spine.protobuf.defaultInstance

/**
 * Verifies that the given message instance is annotated with
 * [io.spine.annotation.Internal] and if so, returns it.
 *
 * @throws IllegalArgumentException
 *          if the message is not internal.
 */
@CanIgnoreReturnValue
public fun requireInternal(msg: Message): Message {
    require(msg.isInternal()) {
        "The message class `${msg::class.java.canonicalName}` is not" +
                " annotated as `${Internal::class.java.canonicalName}`."
    }
    return msg
}

/**
 * Verifies if the given message is not internal to a bounded context,
 * returning it if so.
 *
 * @throws UnpublishedLanguageException if the given message is internal.
 */
@CanIgnoreReturnValue
public fun requirePublished(msg: Message): Message {
    if (msg.isInternal()) {
        throw UnpublishedLanguageException(msg)
    }
    return msg
}

/**
 * Verifies if the given class of messages is a part of published language
 * of a bounded context, returning it if so.
 *
 * @throws UnpublishedLanguageException if the message class is internal to the bounded context.
 */
@CanIgnoreReturnValue
public fun <M : Message> requirePublished(cls: Class<M>): Class<M> {
    if (cls.isInternal()) {
        throw UnpublishedLanguageException(cls.defaultInstance)
    }
    return cls
}


