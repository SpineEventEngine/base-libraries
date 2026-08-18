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

import java.util.Locale;

/**
 * Detects current operating system properties.
 *
 * <p>Based on {@code org.apache.tools.ant.taskdefs.condition.Os}.
 */
@SuppressWarnings("AccessOfSystemProperties") // to get current OS props.
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
            var separatorMatches = ":".equals(PATH_SEP);
            var notMac = !macOS.isCurrent()
                    || OS_NAME.endsWith("x")
                    || OS_NAME.contains(DARWIN);
            var notVms = !OS_NAME.contains("openvms");
            return separatorMatches && notVms && notMac;
        }
    };

    private static final String OS_NAME = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
    private static final String PATH_SEP = System.getProperty("path.separator");

    /**
     * OpenJDK is reported to call MacOS X "Darwin".
     *
     * @see <a href="https://issues.apache.org/bugzilla/show_bug.cgi?id=44889">Bug 1</a>
     * @see <a href="https://issues.apache.org/jira/browse/HADOOP-3318">Bug 2</a>
     */
    private static final String DARWIN = "darwin";

    /** A lower-cased name of the OS family. */
    private final String signature;

    /** Creates an instance with the signature taken as a lower-cased enum item name. */
    OsFamily() {
        this.signature = name().toLowerCase(Locale.ENGLISH);
    }

    /** Creates an instance with the passed signature value. */
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
}
