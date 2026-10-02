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

import com.google.protobuf.Descriptors.Descriptor
import io.spine.type.ApiOption.beta
import io.spine.type.ApiOption.experimental
import io.spine.type.ApiOption.internal
import io.spine.type.ApiOption.spi
import kotlin.jvm.optionals.getOrNull

/**
 * Tells if the type represented by this [Descriptor] is marked as `beta`.
 * If the option value is not set in the type, returns `null`.
 */
public fun Descriptor.isBeta(): Boolean? = optionValueOrNull(beta())

/**
 * Tells if the type represented by this [Descriptor] is marked as `experimental_type`.
 * If the option value is not set in the type, returns `null`.
 */
public fun Descriptor.isExperimental(): Boolean? = optionValueOrNull(experimental())

/**
 * Tells if the type represented by this [Descriptor] is marked as `internal_type`.
 * If the option value is not set in the type, returns `null`.
 */
public fun Descriptor.isInternal(): Boolean? = optionValueOrNull(internal())

/**
 * Tells if the type represented by this [Descriptor] is marked as `spi_type`.
 * If the option value is not set in the type, returns `null`.
 */
public fun Descriptor.isSpi(): Boolean? = optionValueOrNull(spi())

private fun Descriptor.optionValueOrNull(opt: ApiOption): Boolean? =
    opt.findIn(this).getOrNull()
