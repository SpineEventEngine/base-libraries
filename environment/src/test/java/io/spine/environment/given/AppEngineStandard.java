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

/**
 * Determines whether the system is running under Google App Engine Standard environment.
 */
@SuppressWarnings("AccessOfSystemProperties")
public class AppEngineStandard extends AppEngine<AppEngineStandard> {

    private static final String ENV_KEY = "io.spine.base.test.is_appengine";

    @Override
    protected boolean enabled() {
        var propertyValue = System.getProperty(ENV_KEY);
        return activeValue().equalsIgnoreCase(propertyValue);
    }

    @Override
    protected AppEngineStandard self() {
        return this;
    }

    /**
     * Enables the App Engine Standard environment.
     */
    public static void enable() {
        System.setProperty(ENV_KEY, activeValue());
    }

    /**
     * Disables the App Engine Standard environment.
     */
    public static void clear() {
        System.clearProperty(ENV_KEY);
    }

    private static String activeValue() {
        return String.valueOf(true);
    }
}
