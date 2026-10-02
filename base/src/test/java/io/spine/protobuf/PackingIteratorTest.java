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

import com.google.common.collect.Lists;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;

import static io.spine.protobuf.AnyPacker.unpack;
import static io.spine.protobuf.Messages.isDefault;
import static io.spine.protobuf.TypeConverter.toMessage;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`PackingIterator` should")
class PackingIteratorTest {

    private List<Message> list;
    private Iterator<Any> packer;

    @BeforeEach
    void setUp() {
        list = Lists.newArrayList(toMessage("one"),
                                  toMessage(2),
                                  toMessage(3),
                                  toMessage(4),
                                  toMessage(5));
        packer = new PackingIterator(list.iterator());
    }

    @Test
    @DisplayName("implement hasNext()")
    void implement_hasNext() {
        assertTrue(packer.hasNext());

        list.clear();

        assertFalse(packer.hasNext());
    }

    @Test
    @DisplayName("implement next()")
    void implement_next() {
        while (packer.hasNext()) {
            var packed = packer.next();
            assertNotNull(packed);
            assertFalse(isDefault(unpack(packed)));
        }
    }
}
