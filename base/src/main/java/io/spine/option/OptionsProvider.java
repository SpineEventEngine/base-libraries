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

package io.spine.option;

import com.google.protobuf.ExtensionRegistry;

import java.util.ServiceLoader;

/**
 * A service provider interface for custom Protobuf options.
 */
public interface OptionsProvider {

    /**
     * Creates an {@link ExtensionRegistry} performing the registration for all
     * {@code OptionsProvider}s discovered by the service loading mechanism in
     * the current classpath.
     */
    static ExtensionRegistry registryWithAllOptions() {
        var registry = ExtensionRegistry.newInstance();
        var loader = ServiceLoader.load(OptionsProvider.class);
        for (var provider : loader) {
            provider.registerIn(registry);
        }
        return registry;
    }

    /**
     * Registers custom options in the given registry.
     *
     * <p>See the {@code registerAllExtensions(..)} method in the outer Java class generated from
     * the file with the extensions.
     *
     * <p>Example:
     * <pre> {@code
     * {@literal @}Override
     * public void registerIn(ExtensionRegistry registry) {
     *     MyOptionsProto.registerAllExtensions(registry)
     * }
     * }</pre>
     */
    void registerIn(ExtensionRegistry registry);
}
