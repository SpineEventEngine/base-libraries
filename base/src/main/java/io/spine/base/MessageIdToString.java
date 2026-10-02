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
