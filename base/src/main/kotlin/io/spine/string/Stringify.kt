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

@file:JvmName("Stringify")

package io.spine.string

import com.google.common.escape.Escaper
import com.google.common.escape.Escapers
import com.google.protobuf.Duration
import com.google.protobuf.Message
import com.google.protobuf.Timestamp
import java.lang.reflect.Type
import kotlin.reflect.KClass

/**
 * Obtains a string representation of this object using a [Stringifier] for the object type.
 *
 * @see Stringifiers.toString
 */
public inline fun <reified T : Any> T.stringify(): String =
    Stringifiers.toString(this, T::class.java)

/**
 * Same as [Stringifiers.fromString] for brevity in Kotlin code.
 */
public inline fun <reified T : Any> fromString(str: String): T =
    Stringifiers.fromString(str, T::class.java)

/**
 * Same as [Stringifier.fromString] accepting [KClass] instead of [Class].
 */
public fun <T : Any> fromString(str: String, cls: KClass<T>): T =
    Stringifiers.fromString(str, cls.java)

/**
 * Same as [Stringifiers.newForListOf] for brevity in Kotlin code.
 */
public inline fun <reified T : Any> listStringifier(): Stringifier<List<T>> =
    Stringifiers.newForListOf(T::class.java)

/**
 * Same as [Stringifiers.newForListOf] for brevity in Kotlin code.
 */
public inline fun <reified T: Any> listStringifier(delimiter: Char): Stringifier<List<T>> =
    Stringifiers.newForListOf(T::class.java, delimiter)

/**
 * Same as [Stringifiers.newForMapOf] for brevity in Kotlin code.
 */
public inline fun <reified K : Any, reified V : Any> mapStringifier(): Stringifier<Map<K, V>> =
    Stringifiers.newForMapOf(K::class.java, V::class.java)

/**
 * Same as [Stringifiers.newForMapOf] for brevity in Kotlin code.
 */
public inline fun <reified K : Any, reified V : Any> mapStringifier(
    delimiter: Char
): Stringifier<Map<K, V>> = Stringifiers.newForMapOf(K::class.java, V::class.java, delimiter)

/**
 * Utility object for working with `Stringifier`s.
 *
 * **API Note**: This object is Kotlin port of the `Stringifiers` class from Java.
 * It is kept as an object, rather than a group of top-level functions, to preserve backward
 * compatibility with the existing Java code. It is expected that it will be gradually
 * migrated to top-level functions with an appropriate deprecation cycle.
 */
@Suppress("TooManyFunctions") // Need to gather all of them here for easier usage.
public object Stringifiers {

    /**
     * Converts the passed value to the string representation.
     *
     * Use this method for converting non-generic objects. For generic objects,
     * please use the overload that accepts [Type] as the second parameter.
     *
     * @param obj
     *         the object to convert.
     * @param T
     *        the type of the object.
     * @return the string representation of the passed object.
     */
    @JvmStatic
    public fun <T : Any> toString(obj: T): String = toString(obj, obj::class.java)

    /**
     * Converts the passed value to the string representation.
     *
     * The same as [toString] but with the ability for static import in Java.
     */
    @JvmStatic
    public fun <T : Any> stringify(obj: T): String = toString(obj)

    /**
     * Converts the passed value to the string representation.
     *
     * This method must be used if the passed object is a generic type.
     *
     * @param obj
     *         to object to convert.
     * @param typeOfT
     *         the type of the passed object.
     * @param T
     *         the type of the object to convert.
     * @return the string representation of the passed object.
     * @throws MissingStringifierException
     *         if passed value cannot be converted.
     */
    public fun <T : Any> toString(obj: T, typeOfT: Type): String {
        val stringifier = StringifierRegistry.getFor<T>(typeOfT)
        val result = stringifier.convert(obj)
        return result!!
    }

    /**
     * Converts string value to the specified type.
     *
     * @param str
     *         the string to convert.
     * @param typeOfT
     *         the type into which to convert the string.
     * @param T
     *         the type of the value to return.
     * @return the parsed value from string.
     * @throws MissingStringifierException
     *          if passed value cannot be converted.
     */
    @JvmStatic
    public fun <T : Any> fromString(str: String, typeOfT: Class<T>): T {
        val stringifier = StringifierRegistry.getFor<T>(typeOfT)
        val result = stringifier.reverse().convert(str)
        return result!!
    }

    /**
     * Obtains `Stringifier` for the map with default delimiter for the passed map elements.
     *
     * @param keyClass
     *         the class of keys maintained by this map.
     * @param valueClass
     *         the class of mapped values.
     * @param K
     *         the type of keys maintained by this map.
     * @param V
     *         the type of the values stored in this map.
     * @return the stringifier for the map.
     */
    @JvmStatic
    public fun <K : Any, V : Any> newForMapOf(
        keyClass: Class<K>,
        valueClass: Class<V>
    ): Stringifier<Map<K, V>> = MapStringifier(keyClass, valueClass)

    /**
     * Obtains `Stringifier` for the map with custom delimiter for the passed map elements.
     *
     * @param keyClass
     *         the class of keys maintained by this map.
     * @param valueClass
     *         the class of mapped values.
     * @param delimiter
     *         the delimiter for the passed map elements via string.
     * @param K
     *         the type of keys maintained by this map.
     * @param V
     *         the type of mapped values.
     * @return the stringifier for the map.
     */
    public fun <K : Any, V : Any> newForMapOf(
        keyClass: Class<K>,
        valueClass: Class<V>,
        delimiter: Char
    ): Stringifier<Map<K, V>> = MapStringifier(keyClass, valueClass, delimiter)

    /**
     * Obtains `Stringifier` for `Boolean` values.
     */
    @JvmStatic
    public fun forBoolean(): Stringifier<Boolean> = BooleanStringifier.getInstance()

    /**
     * Obtains `Stringifier` for `Integer` values.
     */
    @JvmStatic
    public fun forInteger(): Stringifier<Int> = IntegerStringifier.getInstance()

    /**
     * Obtains the `Stringifier` for `Long` values.
     */
    @JvmStatic
    public fun forLong(): Stringifier<Long> = LongStringifier.getInstance()

    /**
     * Obtains a `Stringifier` for `String` values, which simply returns the given string.
     */
    @JvmStatic
    public fun forString(): Stringifier<String> = NoOpStringifier.getInstance()

    /**
     * Obtains the default stringifier for `Duration` instances.
     *
     * This stringifier is automatically registered in the [StringifierRegistry].
     *
     * @see com.google.protobuf.util.Durations.toString
     * @see com.google.protobuf.util.Durations.parse
     */
    @JvmStatic
    public fun forDuration(): Stringifier<Duration> = DurationStringifier.getInstance()

    /**
     * Obtains a stringifier that coverts a Timestamp into to RFC 3339 date string format.
     *
     * @see com.google.protobuf.util.Timestamps.toString
     * @see com.google.protobuf.util.Timestamps.parse
     */
    @JvmStatic
    public fun forTimestamp(): Stringifier<Timestamp> = TimestampStringifier.getInstance()

    /**
     * Obtains `Stringifier` for a list with default delimiter for the passed list elements.
     *
     * @param elementClass
     *         the class of the list elements.
     * @param T
     *         the type of the elements in this list.
     * @return the stringifier for the list.
     */
    @JvmStatic
    public fun <T : Any> newForListOf(elementClass: Class<T>): Stringifier<List<T>> =
        ListStringifier(elementClass)

    /**
     * Obtains `Stringifier` for a list with the custom delimiter for the passed list elements.
     *
     * @param elementClass
     *         the class of the list elements.
     * @param delimiter
     *         the delimiter or the list elements passed via string.
     * @param T
     *         the type of the elements in this list.
     * @return the stringifier for the list.
     */
    public fun <T : Any> newForListOf(
        elementClass: Class<T>,
        delimiter: Char
    ): Stringifier<List<T>> = ListStringifier(elementClass, delimiter)

    /**
     * Obtains a `Stringifier` for the passed `enum` class.
     *
     * @param enumClass
     *         the class of the `enum`.
     * @param T
     *         the type of the `enum`
     * @return the stringifier for the passed `enum` class.
     */
    @JvmStatic
    public fun <T : Enum<T>> newForEnum(enumClass: Class<T>): Stringifier<T> =
        EnumStringifier(enumClass)

    /**
     * Obtains the default `Stringifier` for the `Message` classes.
     *
     * @param messageClass
     *         the message class.
     * @param T
     *         the type of the message.
     * @return the default stringifier
     */
    @JvmStatic
    public fun <T : Message> newForMessage(messageClass: Class<T>): Stringifier<T> =
        DefaultMessageStringifier(messageClass)

    /**
     * Creates the `Escaper` that escapes contained '\' and passed characters.
     *
     * @param charToEscape
     *         the char to escape.
     * @return the constructed escaper
     */
    @JvmStatic
    public fun createEscaper(charToEscape: Char): Escaper {
        val escapedChar = "\\" + charToEscape
        val result = Escapers.builder()
            .addEscape('\"', "\\\"")
            .addEscape(charToEscape, escapedChar)
            .build()
        return result
    }
}
