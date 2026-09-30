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

package io.spine.util;

import com.google.common.base.Converter;

import java.io.Serializable;

/**
 * Abstract base for serializable converters.
 *
 * @param <A>
 *         source type
 * @param <B>
 *         target type
 * @see SerializableFunction
 */
public abstract class SerializableConverter<A, B>
        extends Converter<A, B>
        implements Serializable {

    private static final long serialVersionUID = 0L;
}
