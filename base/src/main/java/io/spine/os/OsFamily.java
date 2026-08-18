/*
 * Copyright 2026, TeamDev. All rights reserved.
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

package io.spine.os;

import io.spine.annotation.VisibleForTesting;

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
     * <p>The family includes {@link #macOS}, but excludes OpenVMS.
     */
    Unix {
        @Override
        boolean matches(String osName, String pathSeparator) {
            var separatorMatches = ":".equals(pathSeparator);
            var notMac = !macOS.matches(osName, pathSeparator)
                    || osName.endsWith("x")
                    || osName.contains(DARWIN);
            var notVms = !osName.contains("openvms");
            return separatorMatches && notVms && notMac;
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

    /** The lower-cased name of the OS family. */
    private final String signature;

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
