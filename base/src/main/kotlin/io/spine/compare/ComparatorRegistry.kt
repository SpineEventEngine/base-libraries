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

package io.spine.compare

import java.util.ServiceLoader

/**
 * The comparator registry, which maintains a mapping between a [Class]
 * and its associated comparator.
 *
 * A new comparator can be added to the registry [directly][register],
 * or dynamically with the help of [ComparatorProvider] service provider.
 */
public object ComparatorRegistry {

    private val map = mutableMapOf<Class<*>, Comparator<*>>()

    init {
        loadServiceProviders()
    }

    /**
     * Registers a [comparator] for the given [clazz].
     *
     * The method overrides the previously set comparator, if any.
     */
    @JvmStatic
    public fun <T> register(clazz: Class<T>, comparator: Comparator<T>) {
        map[clazz] = comparator
    }

    /**
     * Registers a [comparator] for the specified type [T].
     *
     * The method overrides the previously set comparator, if any.
     */
    public inline fun <reified T : Any> register(comparator: Comparator<T>): Unit =
        register(T::class.java, comparator)

    /**
     * Returns a comparator for the given [clazz].
     *
     * @throws IllegalStateException if there is no comparator for the given [clazz].
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST") // Type safety is enforced by `register()` method signature.
    public fun <T> get(clazz: Class<T>): Comparator<T> {
        check(contains(clazz))
        return map[clazz]!! as Comparator<T>
    }

    /**
     * Returns a comparator for the specified type [T].
     *
     * @throws IllegalStateException if there is no comparator for the type [T].
     */
    public inline fun <reified T : Any> get(): Comparator<T> = get(T::class.java)

    /**
     * Returns a comparator for the given [clazz], if any.
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST") // Type safety is enforced by `register()` method signature.
    public fun <T> find(clazz: Class<T>): Comparator<T>? = map[clazz] as Comparator<T>?

    /**
     * Returns a comparator for the specified type [T], if any.
     */
    public inline fun <reified T : Any> find(): Comparator<T>? = find(T::class.java)

    /**
     * Tells whether the registry has a comparator for the given [clazz].
     */
    @JvmStatic
    public fun contains(clazz: Class<*>): Boolean = map.containsKey(clazz)

    /**
     * Tells whether the registry has a comparator for the specified type [T].
     */
    public inline fun <reified T : Any> contains(): Boolean = contains(T::class.java)

    /**
     * Returns the types for which comparators are currently registered.
     *
     * The returned set is a snapshot; subsequent registrations do not
     * affect it, and modifying it does not affect the registry.
     */
    @JvmStatic
    public fun types(): Set<Class<*>> = map.keys.toSet()

    private fun loadServiceProviders() {
        ServiceLoader.load(ComparatorProvider::class.java)
            .forEach { it.registerIn(this) }
    }
}
