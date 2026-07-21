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

package io.spine.code.proto;

import com.google.protobuf.ByteString;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type;

import java.util.Optional;

import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * Enumeration of the Protobuf scalar value types and corresponding Java types.
 *
 * <p>{@code Type.TYPE_GROUP} is NOT supported, so do not create an associated
 * Java type for it.
 *
 * @see <a href="https://developers.google.com/protocol-buffers/docs/proto3#scalar">
 * Protobuf scalar types</a>
 */
@SuppressWarnings("BadImport") // We static-import `Type` for brevity.
public enum ScalarType {
    DOUBLE(Type.TYPE_DOUBLE, double.class),
    FLOAT(Type.TYPE_FLOAT, float.class),
    INT32(Type.TYPE_INT32, int.class),
    INT64(Type.TYPE_INT64, long.class),
    UINT32(Type.TYPE_UINT32, int.class),
    UINT64(Type.TYPE_UINT64, long.class),
    SINT32(Type.TYPE_SINT32, int.class),
    SINT64(Type.TYPE_SINT64, long.class),
    FIXED32(Type.TYPE_FIXED32, int.class),
    FIXED64(Type.TYPE_FIXED64, long.class),
    SFIXED32(Type.TYPE_SFIXED32, int.class),
    SFIXED64(Type.TYPE_SFIXED64, long.class),
    BOOL(Type.TYPE_BOOL, boolean.class),
    STRING(Type.TYPE_STRING, String.class),
    BYTES(Type.TYPE_BYTES, ByteString.class);

    private final Type protoScalarType;
    private final Class<?> javaClass;

    ScalarType(Type protoScalarType, Class<?> javaClass) {
        this.protoScalarType = protoScalarType;
        this.javaClass = javaClass;
    }

    /**
     * Returns the name of the corresponding Java type for the Protobuf scalar type.
     *
     * @param protoScalar
     *         the Protobuf scalar type
     * @return the name of the corresponding Java type
     */
    public static String javaTypeName(Type protoScalar) {
        return javaType(protoScalar).getName();
    }

    /**
     * Returns the corresponding Java type for the Protobuf scalar type.
     *
     * @param protoScalar
     *         the Protobuf scalar type
     * @return the corresponding Java type
     */
    public static Class<?> javaType(Type protoScalar) {
        for (var scalarType : values()) {
            if (scalarType.protoScalarType == protoScalar) {
                return scalarType.javaClass;
            }
        }
        throw newIllegalStateException("Protobuf type `%s` is not a scalar value type.",
                                       protoScalar);
    }

    /**
     * Verifies if the passed field has scalar type.
     */
    public static boolean isScalarType(FieldDescriptorProto field) {
        return of(field).isPresent();
    }

    /**
     * Finds the scalar type corresponding to the type of the passed field.
     *
     * <p>Returns {@code Optional.empty()} if no such type exists.
     *
     * @see #isScalarType(com.google.protobuf.DescriptorProtos.FieldDescriptorProto)
     */
    public static Optional<ScalarType> of(FieldDescriptorProto field) {
        var type = field.getType();
        for (var scalarType : values()) {
            if (scalarType.protoScalarType() == type) {
                return Optional.of(scalarType);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the Java class corresponding to this scalar type.
     */
    public Class<?> javaClass() {
        return javaClass;
    }

    /**
     * Returns the Protobuf scalar type corresponding to this instance.
     */
    public Type protoScalarType() {
        return protoScalarType;
    }
}
