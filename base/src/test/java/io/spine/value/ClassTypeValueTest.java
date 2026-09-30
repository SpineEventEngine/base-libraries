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

package io.spine.value;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.testing.SerializableTester.reserializeAndAssert;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("ClassTypeValue should")
class ClassTypeValueTest {

    private final Class<?> cls = getClass();
    private final ClassTypeValue<?> classTypeValue =  new AClassValue(cls);

    @Test
    @DisplayName("return enclosed value")
    void enclosedValue() {
        assertEquals(cls, classTypeValue.value());
    }

    @Test
    @DisplayName("give enclosed class name in toString()")
    void classNameInString() {
        assertEquals(cls.getName(), classTypeValue.toString());
    }

    @Test
    @DisplayName("be equal to another with the same class value")
    void equality() {
        new EqualsTester().addEqualityGroup(new AClassValue(cls), new AClassValue(cls))
                          .addEqualityGroup(new AClassValue(Boolean.class))
                          .testEquals();
    }
    
    @Test
    @DisplayName("be serializable")
    void serialize() {
        reserializeAndAssert(new AClassValue(Void.class));
    }

    private static class AClassValue extends ClassTypeValue<Object> {

        private static final long serialVersionUID = 0L;

        private AClassValue(Class<?> value) {
            super(value);
        }
    }
}
