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

@file:JvmName("FileDescriptors")

package io.spine.protobuf

import com.google.protobuf.Descriptors.FileDescriptor
import com.google.protobuf.ExtensionRegistry
import io.spine.code.java.ClassName

/**
 * A [binary][ClassName.binaryName] name of the outer Java class associated with this file.
 */
public val FileDescriptor.outerClassName: String
    get() {
        val outerClass = ClassName.outerClass(this)
        return outerClass.binaryName()
    }

/**
 * An outer Java class associated with this proto file, if such a class already exists.
 * Otherwise, returns `null`.
 */
public val FileDescriptor.outerClass: Class<*>?
    get() {
        return try {
            val classLoader = javaClass.classLoader
            val outerClass = classLoader.loadClass(outerClassName)
            outerClass
        } catch (ignored: ClassNotFoundException) {
            null
        }
    }

/**
 * Reflectively calls the static `registerAllExtensions(..)` method, such as
 * [io.spine.option.OptionsProto.registerAllExtensions], on the [outerClass] generated for
 * this Protobuf file with the custom options declared.
 *
 * @throws IllegalStateException
 *          if the outer class for this proto file does not exist
 */
public fun FileDescriptor.registerAllExtensions(registry: ExtensionRegistry) {
    check(outerClass != null) {
        "The outer class `$outerClassName` for the file `$name` does not exist."
    }
    val method = outerClass!!.getDeclaredMethod(
        "registerAllExtensions", ExtensionRegistry::class.java
    )
    method.invoke(null, registry)
}
