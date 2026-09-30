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

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;

/**
 * Testing environment.
 *
 * <p>Detected by checking the stack trace for mentions of the known testing frameworks.
 *
 * <p>This option is mutually exclusive with {@link DefaultMode}, i.e. one of them is always enabled.
 */
public final class Tests extends StandardEnvironmentType<Tests> {

    private static final Tests INSTANCE = new Tests();

    @SuppressWarnings("DuplicateStringLiteralInspection" /* Used in another context. */)
    private static final ImmutableList<String> KNOWN_TESTING_FRAMEWORKS =
            ImmutableList.of("org.junit",
                             "org.testng",
                             "org.spekframework", // v2
                             "io.spine.testing",
                             "io.kotest");

    /**
     * The names of the packages that when discovered in a stacktrace would tell that
     * the code is executed under tests.
     *
     * @see #enabled()
     */
    public static ImmutableList<String> knownTestingFrameworks() {
        return KNOWN_TESTING_FRAMEWORKS;
    }

    /**
     * Obtains the singleton instance.
     */
    public static Tests type() {
        return INSTANCE;
    }

    /** Prevents direct instantiation. */
    private Tests() {
        super();
    }

    /**
     * Verifies if the code currently runs under a unit testing framework.
     *
     * <p>The method returns {@code true} if {@linkplain #knownTestingFrameworks()
     * known testing framework packages} are discovered in the stacktrace.
     *
     * @return {@code true} if the code runs under a testing framework, {@code false} otherwise
     * @implNote In addition to checking the stack trace, this method checks the
     *         environment variable value. If you wish to simulate not being in tests, the
     *         variable must be set to {@code false} explicitly. If your framework is not
     *         among the {@linkplain #knownTestingFrameworks() known ones}, make sure to set
     *         the system property explicitly.
     * @see #knownTestingFrameworks()
     */
    @Override
    public boolean enabled() {
        var property = new TestsProperty();
        if (property.isSet()) {
            return property.value();
        }

        var stacktrace = Throwables.getStackTraceAsString(new RuntimeException(""));
        var result = knownTestingFrameworks().stream()
                .anyMatch(stacktrace::contains);
        return result;
    }

    @Override
    protected Tests self() {
        return this;
    }
}
