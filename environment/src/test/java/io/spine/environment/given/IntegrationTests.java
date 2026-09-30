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

package io.spine.environment.given;

import io.spine.environment.CustomEnvironmentType;
import io.spine.environment.Tests;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A stub implementation of a custom testing environment type
 * that depends on an external service.
 */
public final class IntegrationTests extends CustomEnvironmentType<IntegrationTests> {

    private static ThirdPartyService service = null;

    public static void injectService(ThirdPartyService s) {
        service = checkNotNull(s);
    }

    @Override
    protected boolean enabled() {
        var testsEnabled = Tests.type().enabled();
        var serviceStarted = service != null && service.isStarted();
        return testsEnabled && serviceStarted;
    }

    @Override
    protected IntegrationTests self() {
        return this;
    }
}
