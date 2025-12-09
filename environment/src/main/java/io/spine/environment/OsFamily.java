/*
 * Copyright 2023, TeamDev. All rights reserved.
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
