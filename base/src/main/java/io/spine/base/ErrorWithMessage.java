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

import com.google.protobuf.Message;

/**
 * Common interface for exceptions that can convert to a message instance for
 * later inclusion into an {@link Error}.
 *
 * @param <M>
 *         the type of the error message provided by the exception
 */
public interface ErrorWithMessage<M extends Message> {

    /**
     * Converts an exception into a corresponding Protobuf message instance.
     */
    M asMessage();
}
