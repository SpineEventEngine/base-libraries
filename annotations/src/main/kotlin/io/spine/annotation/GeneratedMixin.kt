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
 * It also allows to instruct IDEs to consider annotated interfaces as implemented before
 * the code generation phase, or if the interfaces are used only from projects that depend
 * on the one declaring these interfaces.
 *
 * For example, Spine Base project introduces the `io.spine.base.CommandMessage` interface.
 * There are no command messages generated in the Base project because it does not provide any
 * backend API. The interface is used by multiple subprojects of the Spine SDK that depend
 * on Base, but it is not used withing the project.
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
