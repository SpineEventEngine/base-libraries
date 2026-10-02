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

package io.spine.tools.proto.type

import io.spine.code.proto.TypeSet
import io.spine.type.KnownTypes

/**
 * A test double standing in for the production `MoreKnownTypes` class of
 * the `tool-base/proto-code` module.
 *
 * [KnownTypes.Holder.extendWith] admits exactly one caller, matched by its
 * fully qualified name at runtime. This object bears that name so that the
 * allowed value is pinned by a test in this repository, rather than only by
 * a build failure in a project consuming both libraries.
 *
 * @see io.spine.security.InvocationGuard
 */
internal object MoreKnownTypes {

    /**
     * Calls the guarded method with an empty type set, which leaves
     * the known types intact.
     *
     * The call must stay direct. The guard resolves the immediate caller
     * frame, so an extra frame from another class would break the match
     * quietly, leaving the test passing for the wrong reason.
     */
    fun extendWithNothing() {
        KnownTypes.Holder.extendWith(TypeSet.newBuilder().build())
    }
}
