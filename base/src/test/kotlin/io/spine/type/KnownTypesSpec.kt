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
import io.spine.tools.proto.type.MoreKnownTypes
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
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
    fun `allow calling 'extendWith' from 'MoreKnownTypes'`() {
        assertDoesNotThrow {
            MoreKnownTypes.extendWithNothing()
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
