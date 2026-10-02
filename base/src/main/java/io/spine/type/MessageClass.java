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

package io.spine.type;

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Message;
import io.spine.value.ClassTypeValue;

import java.io.Serial;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Objects;
import java.util.Queue;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A base class for value objects storing references to message classes.
 *
 * @param <M>
 *         type of message
 */
public abstract class MessageClass<M extends Message> extends ClassTypeValue<M> {

    @Serial
    private static final long serialVersionUID = 0L;

    /** The URL of the type of proto messages represented by this class. */
    private final TypeUrl typeUrl;

    protected MessageClass(Class<? extends M> value) {
        super(value);
        this.typeUrl = TypeUrl.of(value);
    }

    protected MessageClass(Class<? extends M> value, TypeUrl typeUrl) {
        super(value);
        this.typeUrl = checkNotNull(typeUrl);
    }

    /**
     * Obtains the type URL of messages of this class.
     */
    public TypeUrl typeUrl() {
        return typeUrl;    
    }

    /**
     * Obtains the type name corresponding to this message class.
     */
    public TypeName typeName() {
        return typeUrl.typeName();
    }

    /**
     * Gathers all interfaces (extending {@link Message}) of the passed class,
     * and up in the hierarchy.
     *
     * <p>The {@link Message} interface is not included in the result.
     */
    public static ImmutableSet<Class<? extends Message>>
    interfacesOf(Class<? extends Message> cls) {
        checkNotNull(cls);
        ImmutableSet.Builder<Class<? extends Message>> builder = ImmutableSet.builder();
        var interfaces = cls.getInterfaces();
        Queue<Class<?>> deque = new ArrayDeque<>(Arrays.asList(interfaces));
        while (!deque.isEmpty()) {
            var anInterface = deque.poll();
            if (Message.class.isAssignableFrom(anInterface)
                    && !anInterface.equals(Message.class)) {
                @SuppressWarnings("unchecked")
                var cast = (Class<? extends Message>) anInterface;
                builder.add(cast);
            }
            interfaces = anInterface.getInterfaces();
            if (interfaces.length > 0) {
                deque.addAll(Arrays.asList(interfaces));
            }
        }
        return builder.build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MessageClass)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        var other = (MessageClass<?>) o;
        return typeUrl.equals(other.typeUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), typeUrl);
    }
}
