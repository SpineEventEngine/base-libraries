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

package io.spine.type

import com.google.protobuf.Message
import io.spine.annotation.Internal
import io.spine.protobuf.defaultInstance

/**
 * Obtains the name of this message type.
 */
public val <T : Message> T.typeName: TypeName
    get() = TypeName.of(this)

/**
 * Tells if this message type is internal to a bounded context.
 */
@Suppress("ReturnCount") // We may want to be able to set breakpoints in this method.
public fun <T : Message> T.isInternal(): Boolean {
    if (javaClass.isAnnotationPresent(Internal::class.java)) {
        return true
    }
    val descriptor = descriptorForType
    descriptor.isInternal()?.let {
        return it
    }
    descriptor.file.allTypesAreInternal()?.let {
        return it
    }
    return false
}

/**
 * Tells if this class of messages is internal to a bounded context.
 */
public fun <M : Message> Class<M>.isInternal(): Boolean = defaultInstance.isInternal()
