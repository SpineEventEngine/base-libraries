/*
 * Copyright 2025, TeamDev. All rights reserved.
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

package io.spine.environment;

import io.spine.environment.given.AppEngine;
import io.spine.environment.given.AppEngineStandard;
import io.spine.environment.given.IntegrationTests;
import io.spine.environment.given.Local;
import io.spine.environment.given.Staging;
import io.spine.environment.given.ThirdPartyService;
import io.spine.environment.given.Travis;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.environment.TestsProperty.TESTS_VALUES;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("`Environment` should")
@SuppressWarnings("AccessOfSystemProperties")
class EnvironmentTest {

    /*
     * Environment protection START
     *
     * We remember the state and restore it after this test suite is complete because other tests
     * may initialize the environment.
     */
    @SuppressWarnings("StaticVariableMayNotBeInitialized")
    private static Environment storedEnvironment;

    private Environment environment;

    @BeforeAll
    static void storeEnvironment() {
        storedEnvironment = Environment.instance()
                                       .createCopy();
    }

    @SuppressWarnings("StaticVariableUsedBeforeInitialization")
    @AfterAll
    static void restoreEnvironment() {
        Environment.instance()
                   .restoreFrom(storedEnvironment);
    }

    /* Environment protection END */

    @BeforeEach
    void setUp() {
        environment = Environment.instance();
    }

    @AfterEach
    void cleanUp() {
        Environment.instance()
                   .reset();
        System.clearProperty(TestsProperty.KEY);
    }

    @Nested
    @DisplayName("tell that we are under tests if")
    class UnderTests {

        @Test
        @DisplayName("the env. variable `ENV_TESTS_KEY` set directly")
        void environmentVar() {
            TESTS_VALUES.forEach(
                    value -> {
                        System.setProperty(TestsProperty.KEY, value);
                        assertThat(environment.is(Tests.class)).isTrue();
                    }
            );
        }

        @Test
        @DisplayName("run under known testing framework")
        void underTestFramework() {
            // As we run this from under JUnit...
            assertThat(environment.is(Tests.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("tell that we are not under tests if")
    class NotUnderTests {

        @Test
        @DisplayName("the property value is set to an unsupported value")
        void environmentVarUnknownValue() {
            System.setProperty(TestsProperty.KEY, "neitherTrueNor1");

            assertThat(environment.is(DefaultMode.class)).isTrue();
        }
    }

    @Test
    @DisplayName("turn tests mode on")
    void turnTestsOn() {
        environment.setTo(Tests.class);

        assertThat(environment.is(Tests.class)).isTrue();
    }

    @Test
    @DisplayName("turn production mode on")
    void turnProductionOn() {
        environment.setTo(DefaultMode.class);

        assertThat(environment.is(DefaultMode.class)).isTrue();
    }

    @Test
    @DisplayName("turn a custom mode on")
    void turnCustomTypeOn() {
        environment.register(Staging.class);

        Staging.reset();
        assertThat(environment.is(Staging.class)).isFalse();

        environment.setTo(Staging.class);
        assertThat(environment.is(Staging.class)).isTrue();
    }

    @Test
    @DisplayName("clear the " + TestsProperty.KEY+ " system property on `reset()`")
    void clearOnReset() {
        environment.reset();

        assertNull(System.getProperty(TestsProperty.KEY));
    }

    @Nested
    @DisplayName("when registering custom environment types")
    class CustomEnvTypes {

        @Test
        @DisplayName("allow to provide user defined environment types")
        void provideCustomTypes() {
            environment.register(Staging.class)
                       .register(Local.class);

            // Now that `Environment` knows about `LOCAL`, it should use it as fallback.
            assertThat(environment.is(Local.class)).isTrue();
        }

        @Test
        @DisplayName("fallback to the `TESTS` environment")
        void fallBack() {
            Environment.instance()
                       .register(Travis.class);
            assertThat(environment.is(Travis.class)).isFalse();
            assertThat(environment.is(Tests.class)).isTrue();
        }
    }

    @Test
    @DisplayName("follow assignment-compatibility when determining the type")
    void polymorphicEnv() {
        environment.register(AppEngineStandard.class);

        AppEngineStandard.enable();
        assertThat(environment.is(AppEngine.class)).isTrue();
        AppEngineStandard.clear();
    }

    @Test
    @DisplayName("detect the current environment correctly using the `type` method")
    void determineUsingType() {
        assertThat(environment.is(Tests.class)).isTrue();
    }

    @Test
    @DisplayName("detect the current custom environment in presence of custom types")
    void determineUsingTypeInPresenceOfCustom() {
        environment.register(Local.class)
                   .register(Staging.class);

        assertThat(environment.is(Local.class)).isTrue();
    }

    @Test
    @DisplayName("return the instance of the default environment type")
    void returnInstance() {
        assertThat(environment.type()).isSameInstanceAs(Tests.class);
    }

    @Test
    @DisplayName("return the instance of a custom environment type")
    void returnCustomInstance() {
        environment.register(Local.class)
                   .register(Staging.class);

        assertThat(environment.type()).isSameInstanceAs(Local.class);
    }

    @Test
    @DisplayName("cache a custom environment type")
    void cacheCustom() {
        environment.register(Staging.class);

        Staging.set();
        assertThat(environment.is(Staging.class)).isTrue();

        Staging.reset();
        assertThat(new Staging().enabled()).isFalse();
        assertThat(environment.is(Staging.class)).isTrue();
    }

    /**
     * This test checks whether {@code Tests} type is preserved and is visible from a different
     * thread. This is a common case for tests that involve a multithreading environment or
     * test client-server communication.
     */
    @Test
    @DisplayName("cache the `Tests` environment type")
    void cacheTests() throws InterruptedException {
        var envCached = new AtomicBoolean(false);
        assertThat(environment.is(Tests.class));
        var thread = new Thread(() -> {
            TestsProperty.clear();
            assertThat(environment.is(Tests.class)).isTrue();
            assertThat(Tests.type().enabled()).isFalse();
            envCached.set(true);
        });
        thread.start();
        thread.join();
        assertThat(envCached.get()).isTrue();
    }

    @Test
    @DisplayName("implementing custom `Tests`-like types")
    void integrationTests() {
        var service = new ThirdPartyService();
        environment.register(IntegrationTests.class);

        // We're under tests but service is not injected.
        assertThat(environment.is(IntegrationTests.class))
                .isFalse();

        IntegrationTests.injectService(service);
        environment.autoDetect();

        // Service is injected but is not started.
        assertThat(environment.is(IntegrationTests.class))
                .isFalse();

        service.start();
        environment.autoDetect();
        
        assertThat(environment.is(IntegrationTests.class))
                .isTrue();

        service.stop();
        environment.autoDetect();

        assertThat(environment.is(IntegrationTests.class))
                .isFalse();
    }


    @Nested
    @DisplayName("allow configuring callbacks")
    class Callbacks {

        private boolean called;

        @BeforeEach
        void clearFlag() {
            called = false;
        }

        @Test
        @DisplayName("called by `Environment`")
        void called() {
            environment.whenDetected(
                    Tests.class,
                    type -> called = true
            );
            assertThat(environment.is(Tests.class)).isTrue();
            assertThat(called).isTrue();
        }

        @Test
        @DisplayName("not called when cleared")
        void notCalled() {
            environment.whenDetected(Tests.class, type -> called = true);
            environment.whenDetected(Tests.class, null);

            assertThat(environment.is(Tests.class)).isTrue();
            assertThat(called).isFalse();
        }
    }
}
