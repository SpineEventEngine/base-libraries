/*
 * Copyright 2026, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.type

import com.google.protobuf.Any
import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.Duration
import com.google.protobuf.Empty
import com.google.protobuf.Message
import com.google.protobuf.StringValue
import com.google.protobuf.Timestamp
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.optional.shouldBePresent
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.spine.base.Error
import io.spine.code.java.ClassName
import io.spine.code.proto.TypeSet
import io.spine.option.EntityOption
import io.spine.option.IfMissingOption
import io.spine.string.Separator
import io.spine.string.pi
import io.spine.test.types.KnownTask
import io.spine.test.types.KnownTaskId
import io.spine.test.types.KnownTaskName
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail

/**
 * Tests [io.spine.type.KnownTypes].
 */
@DisplayName("`KnownTypes` should")
internal class KnownTypesSpec {

    private val knownTypes = KnownTypes.instance()

    @Test
    fun `obtain type URLs of known proto types`() {
        knownTypes.allUrls().isEmpty() shouldBe false
    }

    @Test
    fun `build 'TypeRegistry' for known types`() {
        val typeRegistry = knownTypes.typeRegistry()
        val found: MutableList<Descriptor> = mutableListOf()
        val notFound: MutableList<MessageType> = mutableListOf()
        val messageTypes = knownTypes.asTypeSet().messageTypes()
        for (messageType in messageTypes) {
            val descriptor = typeRegistry.find(
                messageType.name().value
            )
            if (descriptor != null) {
                found.add(descriptor)
            } else {
                notFound.add(messageType)
            }
        }

        if (notFound.isNotEmpty()) {
            fail {
                val nl = Separator.nl()
                val indent = "  "
                val notFoundLines = notFound.map { t -> t.toString() }
                    .sorted().joinToString(nl).pi(indent)
                val foundLines = found.map { d -> d.fullName }
                    .sorted().joinToString(nl).pi(indent)

                "Unable to find descriptors for some types using the `TypeRegistry`.\n" +
                "Known message types: ${messageTypes.size}, not found descriptors:" +
                        " ${notFound.size}.\n" +
                "Message types missing in the `TypeRegistry`(${notFound.size}):" +
                        "\n$notFoundLines\n\n" +
                "Full names of found message type descriptors (${found.size}):\n${foundLines}\n"
            }
        }
        found.isEmpty() shouldBe false
    }

    @Nested internal inner class
    `contain types` {

        @Test
        fun `defined by Spine framework`() {
            assertContainsClass(EntityOption::class.java)
            assertContainsClass(Error::class.java)
            assertContainsClass(IfMissingOption::class.java)
        }

        @Test
        fun `from Google Protobuf`() {
            assertContainsClass(Any::class.java)
            assertContainsClass(Timestamp::class.java)
            assertContainsClass(Duration::class.java)
            assertContainsClass(Empty::class.java)
        }

        private fun assertContainsClass(msgClass: Class<out Message?>) {
            val typeUrl = TypeUrl.of(msgClass)
            val className = knownTypes.classNameOf(typeUrl)

            className shouldBe ClassName.of(msgClass)
        }

        @Test
        fun `nested into other proto types`() {
            val typeUrl = TypeUrl.from(EntityOption.Kind.getDescriptor())
            val className = knownTypes.classNameOf(typeUrl)

            className shouldBe ClassName.of(EntityOption.Kind::class.java)
        }
    }

    @Test
    fun `find type URL by type name`() {
        val typeUrlExpected = TypeUrl.from(StringValue.getDescriptor())
        val typeUrlActual = knownTypes.find(typeUrlExpected.typeName())
            .map { obj: Type<*, *> -> obj.url() }

        typeUrlActual shouldBePresent {
            it shouldBe typeUrlExpected
        }
    }

    @Test
    fun `obtain all types under a given package`() {
        val taskId = TypeUrl.from(KnownTaskId.getDescriptor())
        val taskName = TypeUrl.from(KnownTaskName.getDescriptor())
        val task = TypeUrl.from(KnownTask.getDescriptor())
        val packageName = "spine.test.types"
        val packageTypes = knownTypes.allFromPackage(packageName)

        packageTypes shouldContainAll listOf(taskId, taskName, task)
    }

    @Test
    fun `return empty set of types for unknown package`() {
        val packageName = "com.foo.invalid.package"
        val emptyTypesCollection: Set<*> = knownTypes.allFromPackage(packageName)

        emptyTypesCollection shouldNotBe null
        emptyTypesCollection.shouldBeEmpty()
    }

    @Test
    fun `do not return types by package prefix`() {
        val prefix = "spine.test.ty" // "spine.test.types" is a valid package
        val packageTypes: Collection<TypeUrl> = knownTypes.allFromPackage(prefix)

        packageTypes.shouldBeEmpty()
    }

    @Test
    fun `throw 'UnknownTypeException' for requesting info on an unknown type`() {
        val unexpectedUrl = TypeUrl.parse("prefix/unexpected.type")
        assertUnknownType { knownTypes.classNameOf(unexpectedUrl) }
    }

    @Test
    fun `print known type URLs in alphabetical order`() {
        val output = knownTypes.printAllTypes()

        val anyUrl = TypeUrl.from(Any.getDescriptor()).value()
        val timestampUrl = TypeUrl.from(Timestamp.getDescriptor()).value()
        val durationUrl = TypeUrl.from(Duration.getDescriptor()).value()

        output shouldContain anyUrl
        output shouldContain timestampUrl
        output shouldContain durationUrl

        val anyIndex = output.indexOf(anyUrl)
        val durationIndex = output.indexOf(durationUrl)
        val timestampIndex = output.indexOf(timestampUrl)

        anyIndex shouldBeLessThan timestampIndex
        durationIndex shouldBeLessThan timestampIndex
    }

    @Test
    fun `provide alphabetically sorted list of file names`() {
        val files = knownTypes.fileNames()

        files shouldContain "google/protobuf/any.proto"
        files shouldContain "google/protobuf/type.proto"
    }

    @Test
    fun `provide alphabetically sorted list of type names`() {
        val names = knownTypes.typeNames()

        names shouldContain "google.protobuf.Any"
        names shouldContain "spine.base.Error"
    }

    @Test
    fun `prohibit calling 'extendWith' from client code`() {
        assertThrows<SecurityException> {
            KnownTypes.Holder.extendWith(TypeSet.newBuilder().build())
        }
    }

    @Test
    fun `provide a string representation listing the types`() {
        knownTypes.toString() shouldContain "KnownTypes"
    }

    @Test
    fun `resolve to the loaded instance upon deserialization`() {
        val bytes = ByteArrayOutputStream().use { bytesOut ->
            ObjectOutputStream(bytesOut).use { it.writeObject(knownTypes) }
            bytesOut.toByteArray()
        }
        val restored = ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() }

        (restored is KnownTypes) shouldBe true
        restored.toString() shouldBe knownTypes.toString()
    }
}
