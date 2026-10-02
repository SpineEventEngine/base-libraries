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

package io.spine.protobuf;

import com.google.common.collect.UnmodifiableIterator;
import com.google.protobuf.Any;
import com.google.protobuf.Message;

import java.util.Iterator;

/**
 * An iterator that packs messages from the source iterator.
 *
 * @see AnyPacker#pack(Iterator)
 */
final class PackingIterator extends UnmodifiableIterator<Any> {

    private final Iterator<Message> source;

    PackingIterator(Iterator<Message> source) {
        super();
        this.source = source;
    }

    @Override
    public boolean hasNext() {
        return source.hasNext();
    }

    /**
     * Takes the message from the source iterator, wraps it into {@code Any}
     * and returns.
     *
     * <p>If the source iterator returns {@code null} message, the default instance
     * of {@code Any} will be returned.
     *
     * @return the packed message or default {@code Any}
     */
    @Override
    public Any next() {
        var next = source.next();
        var result = next != null
                     ? AnyPacker.pack(next)
                     : Any.getDefaultInstance();
        return result;
    }
}
