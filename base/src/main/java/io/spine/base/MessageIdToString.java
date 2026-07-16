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

package io.spine.base;

import com.google.common.reflect.TypeToken;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import io.spine.string.StringifierRegistry;

import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.type.ProtoTexts.shortDebugString;
import static java.util.Objects.requireNonNull;

/**
 * Utilities for converting message-based identifiers to String.
 */
final class MessageIdToString {

    private static final Pattern PATTERN_COLON_SPACE = Pattern.compile(": ");
    private static final String EQUAL_SIGN = "=";

    private MessageIdToString() {
    }

    static String convert(Message message) {
        checkNotNull(message);
        String result;
        var registry = StringifierRegistry.instance();
        var msgClass = message.getClass();
        var msgToken = TypeToken.of(msgClass);
        var msgType = msgToken.getType();
        var optional = registry.find(msgType);
        if (optional.isPresent()) {
            var converter = optional.get();
            result = converter.convert(message);
        } else {
            result = doConvert(message);
        }
        return requireNonNull(result);
    }

    private static String doConvert(Message message) {
        var values = message.getAllFields().values();
        String result;
        if (values.isEmpty()) {
            result = Identifier.EMPTY_ID;
        } else if (values.size() == 1) {
            var object = values.iterator().next();
            result = object instanceof Message msg
                     ? convert(msg)
                     : object.toString();
        } else {
            result = messageWithMultipleFieldsToString(message);
        }
        return result;
    }

    private static String messageWithMultipleFieldsToString(MessageOrBuilder message) {
        var result = shortDebugString(message);
        result = PATTERN_COLON_SPACE.matcher(result)
                                    .replaceAll(EQUAL_SIGN);
        return result;
    }
}
