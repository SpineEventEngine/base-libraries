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

package io.spine.protobuf

import io.spine.annotation.Internal
import io.spine.base.ListOfAnys
import io.spine.base.listOfAnys
import io.spine.protobuf.TypeConverter.toAny

/**
 * Converts a list of [kotlin.Any] to [ListOfAnys] proto message.
 *
 * Note that the [backward conversion][toObject] from [ListOfAnys]
 * to a list of [kotlin.Any] is not supported.
 */
@Internal
internal class ListConverter : ProtoConverter<ListOfAnys, List<Any>>() {

    override fun toObject(input: ListOfAnys): List<Any> =
        throw UnsupportedOperationException(
            "`${javaClass.name}` does not support conversion of Protobuf messages to `List`."
        )

    override fun toMessage(input: List<Any>): ListOfAnys {
        val values = input.map { toAny(it) }
        return listOfAnys {
            value.addAll(values)
        }
    }
}
