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

import io.spine.annotation.VisibleForTesting;

import java.util.Arrays;
import java.util.Locale;

/**
 * A family of operating systems.
 *
 * <p>Each item tells whether the operating system under which the code runs
 * belongs to the family.
 *
 * <p>The families are not mutually exclusive. A macOS host belongs to both
 * {@link #macOS} and {@link #Unix}.
 *
 * <p>Based on {@code org.apache.tools.ant.taskdefs.condition.Os}.
 */
@SuppressWarnings("AccessOfSystemProperties") // Reads properties of the current OS.
public enum OsFamily {

    /** Microsoft Windows. */
    Windows,

    /**
     * Apple macOS, including the systems which report themselves as {@code Darwin}.
     *
     * <p>A macOS host belongs to the {@link #Unix} family too.
     */
    macOS("mac") {
        @Override
        boolean matches(String osName, String pathSeparator) {
            return super.matches(osName, pathSeparator) || osName.contains(DARWIN);
        }
    },

    /**
     * A Unix-like operating system, told by the path separator it uses.
     *
     * <p>The family includes {@link #macOS}, but excludes OpenVMS and
     * classic (pre-OS X) macOS.
     */
    Unix {
        @Override
        boolean matches(String osName, String pathSeparator) {
            var separatorMatches = ":".equals(pathSeparator);
            var notClassicMac = !macOS.matches(osName, pathSeparator)
                    || osName.endsWith("x")
                    || osName.contains(DARWIN);
            var notVms = !osName.contains("openvms");
            return separatorMatches && notVms && notClassicMac;
        }
    };

    private static final String OS_NAME =
            System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH);
    private static final String PATH_SEP = System.getProperty("path.separator", "");

    /**
     * OpenJDK is reported to call Mac OS X {@code Darwin}.
     *
     * @see <a href="https://issues.apache.org/bugzilla/show_bug.cgi?id=44889">Ant bug 44889</a>
     * @see <a href="https://issues.apache.org/jira/browse/HADOOP-3318">HADOOP-3318</a>
     */
    private static final String DARWIN = "darwin";

    /**
     * The substring of the lower-cased {@code os.name} value which identifies the family.
     *
     * <p>Defaults to the lower-cased constant name, unless a constant passes
     * its own value, as {@link #macOS} does.
     */
    private final String signature;

    /**
     * Obtains the family of the current operating system.
     *
     * <p>As the families overlap, the method returns the most specific of them.
     * For example, a macOS host gives {@link #macOS} rather than {@link #Unix}.
     *
     * @throws IllegalStateException
     *         if the current operating system belongs to none of the families
     */
    public static OsFamily detect() {
        return detect(OS_NAME, PATH_SEP);
    }

    /**
     * Obtains the most specific family of the operating system with the passed properties.
     *
     * @param osName
     *         the lower-cased value of the {@code os.name} system property
     * @param pathSeparator
     *         the value of the {@code path.separator} system property
     * @throws IllegalStateException
     *         if the operating system belongs to none of the families
     */
    @VisibleForTesting
    static OsFamily detect(String osName, String pathSeparator) {
        // The more specific families are declared first, so the first match is the most specific.
        var current = Arrays.stream(values())
                .filter(family -> family.matches(osName, pathSeparator))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Unable to detect the family of the operating system `" + osName + "`."));
        return current;
    }

    /** Creates an instance with the signature taken from the lower-cased constant name. */
    OsFamily() {
        this.signature = name().toLowerCase(Locale.ENGLISH);
    }

    /** Creates an instance with the passed signature value. */
    OsFamily(String signature) {
        this.signature = signature;
    }

    /**
     * Tells whether the operating system under which the code is executed belongs
     * to this OS family.
     */
    public boolean isCurrent() {
        var result = matches(OS_NAME, PATH_SEP);
        return result;
    }

    /**
     * Tells whether the operating system with the passed properties belongs to this family.
     *
     * @param osName
     *         the lower-cased value of the {@code os.name} system property
     * @param pathSeparator
     *         the value of the {@code path.separator} system property
     */
    @VisibleForTesting
    boolean matches(String osName, String pathSeparator) {
        var result = osName.contains(signature);
        return result;
    }
}
