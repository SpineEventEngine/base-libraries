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

package io.spine.environment

import com.google.common.testing.EqualsTester
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`EnvironmentType` should")
internal class EnvironmentTypeSpec {

    @Test
    fun `obey the 'equals' and 'hashCode' contract`() {
        EqualsTester()
            .addEqualityGroup(StubType(), StubType())
            .addEqualityGroup(OtherStubType(), OtherStubType())
            .testEquals()
    }
}

private class StubType : EnvironmentType<StubType>() {
    override fun enabled(): Boolean = false
    override fun self(): StubType = this
}

private class OtherStubType : EnvironmentType<OtherStubType>() {
    override fun enabled(): Boolean = false
    override fun self(): OtherStubType = this
}
