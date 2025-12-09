/*
 * Copyright 2022, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.environment;

/**
 * Abstract base for custom environment types.
 *
 * <p>{@code Environment} allows to {@link Environment#register(Class) register custom types}.
 * In this case the environment detection functionality iterates over all known types, starting
 * with those registered by the framework user:
 *
 * <pre>
 *
 * public final class Application {
 *
 *     static {
 *         Environment.instance()
 *                    .register(Staging.class)
 *                    .register(LoadTesting.class);
 *     }
 *
 *     private final ConnectionPool pool;
 *
 *     private Application() {
 *         Environment environment = Environment.instance();
 *         if (environment.is(Tests.class) {
 *              // Single connection is enough for tests.
 *             this.pool = new ConnectionPoolImpl(PoolCapacity.of(1));
 *         } else {
 *             if(environment.is(LoadTesting.class) {
 *                 this.pool =
 *                         new ConnectionPoolImpl(PoolCapacity.fromConfig("load_tests.yml"));
 *             } else {
 *                 this.pool =
 *                         new ConnectionPoolImpl(PoolCapacity.fromConfig("cloud_deployment.yml"));
 *             }
 *         }
 *         //...
 *     }
 * }
 * </pre>
 *
 * <p><b>When registering custom types, please ensure</b> their mutual exclusivity.
 * If two or more environment types {@linkplain EnvironmentType#enabled() consider themselves
 * enabled} at the same time, the behaviour of {@link Environment#is(Class)} is undefined.
 *
 * @param <T>
 *         the type of the environment for type covariance
 */
public abstract class CustomEnvironmentType<T extends CustomEnvironmentType<T>>
        extends EnvironmentType<T> {

    /**
     * Custom environment types must have a public no-arg constructor.
     *
     * <p>This constructor is used by {@link Environment} upon
     * the {@linkplain Environment#register(Class) registration}.
     */
    protected CustomEnvironmentType() {
        super();
    }
}
