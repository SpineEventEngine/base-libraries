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

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import io.spine.annotation.Internal;
import io.spine.string.Stringifiers;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.io.Serial;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.base.Time.currentTime;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A {@code Throwable} that has a {@link Message} as its state.
 *
 * <p>Typically used to signalize about a command rejection, occurred in a system. In which case
 * the {@code message} thrown is a detailed description of the rejection reason.
 */
public abstract class RejectionThrowable extends Throwable {

    @Serial
    private static final long serialVersionUID = 0L;

    private final RejectionMessage message;

    /** The moment of creation of this object. */
    private final Timestamp timestamp;

    /** Optional ID of the entity that threw the message. */
    private @MonotonicNonNull Any producerId;

    protected RejectionThrowable(RejectionMessage message) {
        super();
        this.message = checkNotNull(message);
        this.timestamp = currentTime();
    }

    /**
     * Obtains the thrown rejection message.
     */
    public RejectionMessage messageThrown() {
        return message;
    }

    /**
     * Obtains the time when the rejection message was created.
     */
    public Timestamp timestamp() {
        return timestamp;
    }

    /**
     * Initializes the ID of the entity that has thrown the message.
     *
     * <p>This internal API method can be called only once. It is supposed to be used by
     * the framework and must not be called by the user's code.
     *
     * @param  producerId the ID of the entity packed into {@code Any}
     * @return a reference to this {@code ThrowableMessage} instance
     */
    @Internal
    @CanIgnoreReturnValue
    public synchronized RejectionThrowable initProducer(Any producerId) {
        checkNotNull(producerId);
        if (this.producerId != null) {
            var unpackedId = Identifier.unpack(producerId);
            var stringId = Stringifiers.toString(unpackedId);
            throw newIllegalStateException("Producer already initialized: `%s`.", stringId);
        }
        this.producerId = producerId;
        return this;
    }

    /**
     * Obtains the ID of the entity that has thrown the message.
     */
    public synchronized Optional<Any> producerId() {
        return Optional.ofNullable(producerId);
    }
}
