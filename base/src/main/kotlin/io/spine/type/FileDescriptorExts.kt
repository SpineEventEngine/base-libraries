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

import com.google.protobuf.Descriptors.FileDescriptor
import io.spine.type.ApiOption.beta
import io.spine.type.ApiOption.experimental
import io.spine.type.ApiOption.internal
import io.spine.type.ApiOption.spi
import kotlin.jvm.optionals.getOrNull

/**
 * Obtains the value of the `internal_all` option, if the option is present in the file.
 * If the option is not present, assumes `false`.
 */
public fun FileDescriptor.allTypesAreBeta(): Boolean? = optionValueOrNull(beta())

/**
 * Obtains the value of the `internal_all` option, if the option is present in the file.
 * If the option is not present, assumes `false`.
 */
public fun FileDescriptor.allTypesAreInternal(): Boolean? = optionValueOrNull(internal())

/**
 * Obtains the value of the `spi_all` option, if the option is present in the file.
 * If the option is not present, assumes `false`.
 */
public fun FileDescriptor.allTypesAreSpi(): Boolean? = optionValueOrNull(spi())

/**
 * Obtains the value of the `experimental_all` option, if the option is present in the file.
 * If the option is not present, assumes `false`.
 */
public fun FileDescriptor.allTypesAreExperimental(): Boolean? = optionValueOrNull(experimental())

private fun FileDescriptor.optionValueOrNull(opt: ApiOption): Boolean? =
    opt.findIn(this).getOrNull()
