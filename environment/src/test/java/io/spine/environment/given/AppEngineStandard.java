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
     * Disables teh App Engine Standard environment.
     */
    public static void clear() {
        System.clearProperty(ENV_KEY);
    }

    private static String activeValue() {
        return String.valueOf(true);
    }
}
