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

import com.google.protobuf.Descriptors.ServiceDescriptor
import io.spine.type.ApiOption.spi
import kotlin.jvm.optionals.getOrNull

/**
 * Tells if the type represented by this [ServiceDescriptor] is marked as `spi_type`.
 * If the option value is not set in the type, returns `null`.
 */
public fun ServiceDescriptor.isSpi(): Boolean? = optionValueOrNull(spi())

private fun ServiceDescriptor.optionValueOrNull(opt: ApiOption): Boolean? =
    opt.findIn(this).getOrNull()
