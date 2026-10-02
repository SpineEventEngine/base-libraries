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

package io.spine.environment;

/**
 * Abstract base for custom environment types.
 *
 * <p>{@code Environment} allows one to {@link Environment#register(Class) register custom types}.
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
