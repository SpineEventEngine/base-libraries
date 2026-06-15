/*
 * Copyright 2026, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import io.spine.annotation.Internal;
import io.spine.string.Stringifiers;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

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

    private static final long serialVersionUID = 0L;

    private final RejectionMessage message;

    /** The moment of creation of this object. */
    private final Timestamp timestamp;

    /** Optional ID of the entity that thrown the message. */
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
     * Obtains ID of the entity that has thrown the message.
     */
    public synchronized Optional<Any> producerId() {
        return Optional.ofNullable(producerId);
    }
}
