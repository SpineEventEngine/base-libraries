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

package io.spine.base.given;

import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Any;
import io.spine.base.EntityState;
import io.spine.testing.StubMessage;

import java.io.Serial;

@Immutable
public final class FakeEntityState extends StubMessage implements EntityState<Any> {
    @Serial
    private static final long serialVersionUID = 0;
}
