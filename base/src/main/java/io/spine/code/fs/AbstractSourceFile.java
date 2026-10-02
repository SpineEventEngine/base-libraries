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

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.io.IoPreconditions.checkExists;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllLines;
import static java.nio.file.Files.write;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * Abstract base for source code files.
 */
public abstract class AbstractSourceFile extends FsObject {

    private @Nullable ImmutableList<String> lines;

    /**
     * Creates a file at the given path.
     */
    protected AbstractSourceFile(Path path) {
        super(path);
    }

    /**
     * Loads the content of the file from the file system.
     */
    @OverridingMethodsMustInvokeSuper
    protected void load() {
        var path = path();
        checkExists(path.toFile());
        try {
            var loaded = readAllLines(path);
            lines = ImmutableList.copyOf(loaded);
        } catch (IOException e) {
            throw newIllegalStateException(e, "Unable to read the file `%s`.", path);
        }
    }

    /**
     * Rewrites this file.
     */
    @OverridingMethodsMustInvokeSuper
    public void store() {
        var path = path();
        try {
            write(path, lines(), UTF_8, TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw newIllegalStateException(e, "Unable to write to the file `%s`.", path);
        }
    }

    /**
     * Obtains the lines of the {@linkplain #load() loaded} file.
     *
     * @return the content of the file or an empty list, if the file was not loaded
     */
    protected final ImmutableList<String> lines() {
        return this.lines == null
               ? ImmutableList.of()
               : this.lines;
    }

    protected final void update(ImmutableList<String> newLines) {
        checkNotNull(newLines);
        this.lines = newLines;
    }
}
