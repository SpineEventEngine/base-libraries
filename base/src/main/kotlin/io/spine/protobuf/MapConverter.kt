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
import io.spine.base.MapOfAnys
import io.spine.base.MapOfAnysKt.entry
import io.spine.base.mapOfAnys
import io.spine.protobuf.TypeConverter.toAny

/**
 * Converts a map of [kotlin.Any] to [MapOfAnys] proto message.
 *
 * Note that the [backward conversion][toObject] from [MapOfAnys]
 * to a map of [kotlin.Any] is not supported.
 */
@Internal
internal class MapConverter : ProtoConverter<MapOfAnys, Map<Any, Any>>() {

    override fun toObject(input: MapOfAnys): Map<Any, Any> =
        throw UnsupportedOperationException(
            "`${javaClass.name}` does not support conversion of Protobuf messages to `Map`."
        )

    override fun toMessage(input: Map<Any, Any>): MapOfAnys {
        val entries = input.map { it.toProtoEntry() }
        return mapOfAnys {
            entry.addAll(entries)
        }
    }
}

private fun Map.Entry<Any, Any>.toProtoEntry() = entry {
    key = toAny(this@toProtoEntry.key)
    value = toAny(this@toProtoEntry.value)
}
