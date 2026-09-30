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

package io.spine.code.fs;

import com.google.errorprone.annotations.Immutable;
import io.spine.value.ComparableStringValue;

import java.io.Serial;

/**
 * A name of a source code file.
 *
 * @param <F> the type of the file name for comparison type covariance
 */
@Immutable
public abstract class AbstractFileName<F extends AbstractFileName<F>>
        extends ComparableStringValue<F> {

    @Serial
    private static final long serialVersionUID = 0L;

    protected AbstractFileName(String value) {
        super(value);
    }
}
