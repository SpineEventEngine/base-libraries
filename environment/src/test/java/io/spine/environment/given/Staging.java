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

@SuppressWarnings("AccessOfSystemProperties")
public final class Staging extends CustomEnvironmentType<Staging> {

    private static final String STAGING_ENV_TYPE_KEY =
            "io.spine.base.EnvironmentTest.is_staging";

    @Override
    public boolean enabled() {
        return String.valueOf(true)
                     .equalsIgnoreCase(System.getProperty(STAGING_ENV_TYPE_KEY));
    }

    @Override
    protected Staging self() {
        return this;
    }

    public static void set() {
        System.setProperty(STAGING_ENV_TYPE_KEY, String.valueOf(true));
    }

    public static void reset() {
        System.clearProperty(STAGING_ENV_TYPE_KEY);
    }
}

