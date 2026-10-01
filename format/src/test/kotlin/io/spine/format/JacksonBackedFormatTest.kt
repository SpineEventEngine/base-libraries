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

package io.spine.format

import com.google.common.collect.ImmutableList
import java.time.Instant
import java.util.*

/**
 * The abstract base for tests checking formats backed by the Jackson library.
 */
abstract class JacksonBackedFormatTest(format: Format<in Any>) :
    FormatTest<UserAccount>(format) {

    override fun createInstance(): UserAccount {
        return UserAccount.create(UUID.randomUUID().toString())
    }
}

data class UserAccount(
    val id: String,
    val creationTimestamp: Instant,     // Test `JavaTimeModule`.
    val emails: ImmutableList<EmailAddress>,  // Test `GuavaModule` with a custom item type.
    val gender: Optional<String>        // Test `Jdk8Module`.
) {
    companion object {
        fun create(id: String) = UserAccount(
            id,
            Instant.now(),
            ImmutableList.of(EmailAddress("j.doe@example.org"), EmailAddress("john@acme-corp.com")),
            gender = Optional.of("X")
        )
    }
}

// We don't want to bring Jakarta Mail just to test a custom type.
data class EmailAddress(val value: String)
