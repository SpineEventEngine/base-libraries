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

package io.spine.annotation

import java.lang.annotation.Inherited
import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.CLASS

/**
 * Indicates an interface that is implemented in the generated code adding desired behavior
 * using default methods declared in the interface.
 *
 * ## Motivation
 *
 * This annotation allows documenting the intent of the interface.
 * It also allows instructing IDEs to consider annotated interfaces as implemented before
 * the code generation phase, or if the interfaces are used only from projects that depend
 * on the one declaring these interfaces.
 *
 * For example, the Spine Base project introduces the `io.spine.base.CommandMessage` interface.
 * There are no command messages generated in the Base project because it does not provide any
 * backend API. The interface is used by multiple subprojects of the Spine SDK that depend
 * on Base, but it is not used within the project.
 * Annotating the interface with `GeneratedMixin` addresses the issue.
 *
 * ## Creating a mixin interface
 *
 * In order to generate a class that implements a custom mixin interface:
 * 1. Create the interface and mark it with this annotation.
 * 2. Declare methods of interest accessing properties of the generated types following
 *    the Protobuf convention for the accessor methods.
 *    For example, if a message has a property named `foo_bar`, the method to declare will
 *    be `getFooBar()`.
 * 3. Add `default` methods. Presumably, bodies of these methods will call accessor
 *    methods declared earlier.
 * 4. Mark corresponding proto messages using the `(is).java_type` option (if for
 *    one message), or `(every_is).java_type` option for a file, if the interface is common
 *    for all the message declared in this file. These options instruct Spine Compiler to
 *    make the generated code implement this interface.
 *
 * The annotation should *NOT* be used on interfaces that do not provide default methods
 * because they will not be mixins.
 */
@Retention(SOURCE)
@Target(CLASS)
@Inherited
@MustBeDocumented
public annotation class GeneratedMixin
