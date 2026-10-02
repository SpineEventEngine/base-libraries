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

import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Locale;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

/**
 * Detects current operating system properties.
 *
 * <p>Based on {@code org.apache.tools.ant.taskdefs.condition.Os}.
 */
public enum OsFamily {

    Windows,

    macOS("mac") {
        @Override
        public boolean isCurrent() {
            return super.isCurrent() || OS_NAME.contains(DARWIN);
        }
    },

    Unix {
        @Override
        public boolean isCurrent() {
            if (macOS.isCurrent()) {
                return false;
            }
            var separatorMatches = ":".equals(PATH_SEP);
            var darwinOrX = OS_NAME.endsWith("x") || OS_NAME.contains(DARWIN);
            var notVms = !OS_NAME.contains("openvms");
            return separatorMatches && notVms && darwinOrX;
        }
    };

    private static final String OS_NAME = prop("os.name").toLowerCase(Locale.ENGLISH);
    private static final String PATH_SEP = prop("path.separator");

    /**
     * OpenJDK is reported to call macOS "Darwin".
     *
     * @see <a href="https://issues.apache.org/bugzilla/show_bug.cgi?id=44889">Bug 1</a>
     * @see <a href="https://issues.apache.org/jira/browse/HADOOP-3318">Bug 2</a>
     */
    private static final String DARWIN = "darwin";

    /**
     * A lower-cased name of the OS family.
     */
    private final String signature;

    /**
     * Obtains the family of the current operating system.
     */
    @NonNull
    public static OsFamily detect() {
        var current = Arrays.stream(values())
                .filter(OsFamily::isCurrent)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Unable to detect current operating system.")
                );
        return current;
    }

    /**
     * Creates an instance with the signature taken as a lower-cased enum item name.
     */
    OsFamily() {
        this.signature = name().toLowerCase(Locale.ENGLISH);
    }

    /**
     * Creates an instance with the passed signature value.
     */
    OsFamily(String signature) {
        this.signature = signature;
    }

    /**
     * Tells if the operating system under which the code is executed belongs
     * to this OS family.
     */
    public boolean isCurrent() {
        var result = OS_NAME.contains(signature);
        return result;
    }

    /**
     * Obtains the value of the system property with the given name.
     *
     * <p>Added for brevity of the code.
     */
    @NonNull
    @SuppressWarnings("AccessOfSystemProperties") // to get current OS props.
    private static String prop(String name) {
        var property = System.getProperty(name);
        checkState(property != null, "Unable to obtain the system property `%s`", name);
        return requireNonNull(property);
    }
}
