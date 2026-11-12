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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.util.Cleanup.closeAll;
import static io.spine.util.Cleanup.runAll;
import static io.spine.util.Cleanup.runKeepingFailure;
import static io.spine.util.Cleanup.throwIfAny;
import static io.spine.util.Cleanup.withSuppressed;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`Cleanup` Java API should")
class CleanupJavaSpec {

    @Test
    @DisplayName("keep the type of the failures in `withSuppressed()`")
    void keepFailureType() {
        var first = new IllegalStateException("first");
        var second = new IllegalArgumentException("second");
        List<Runnable> steps = List.of(
                () -> { throw first; },
                () -> { throw second; }
        );

        var thrown = assertThrows(IllegalStateException.class, () -> runEach(steps));

        assertThat(thrown).isSameInstanceAs(first);
        assertThat(thrown.getSuppressed()).asList()
                                          .containsExactly(second);
    }

    /**
     * Runs the given steps the way a Java caller of {@code withSuppressed()} does.
     *
     * <p>The kept failure is a {@code RuntimeException}, so it is thrown without a cast,
     * and without a {@code throws} clause.
     */
    private static void runEach(Iterable<Runnable> steps) {
        RuntimeException failure = null;
        for (var step : steps) {
            try {
                step.run();
            } catch (RuntimeException e) {
                failure = withSuppressed(failure, e);
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    @Test
    @DisplayName("accept method references and lambdas in `runAll()`")
    void runMethodReferences() {
        var first = new StubResource();
        var second = new StubResource();
        var third = new StubResource();

        runAll(first::close, second::close, () -> third.close());

        assertThat(first.closed).isTrue();
        assertThat(second.closed).isTrue();
        assertThat(third.closed).isTrue();
    }

    @Test
    @DisplayName("throw the first unchecked failure of `runAll()` as is")
    void throwUnchecked() {
        var failure = new IllegalStateException("first");
        var resource = new StubResource();

        var thrown = assertThrows(IllegalStateException.class, () -> runAll(
                () -> { throw failure; },
                resource::close
        ));

        assertThat(thrown).isSameInstanceAs(failure);
        assertThat(resource.closed).isTrue();
    }

    @Test
    @DisplayName("accept a list of resources in `closeAll()`")
    void closeList() {
        var first = new StubResource();
        var second = new StubResource();

        closeAll(List.of(first, second));

        assertThat(first.closed).isTrue();
        assertThat(second.closed).isTrue();
    }

    @Test
    @DisplayName("wrap a checked exception thrown by `close()` into `IllegalStateException`")
    void wrapChecked() {
        var checked = new Exception("checked");
        AutoCloseable resource = () -> {
            throw checked;
        };

        var thrown = assertThrows(IllegalStateException.class,
                                  () -> closeAll(List.of(resource)));

        assertThat(thrown).hasCauseThat()
                          .isSameInstanceAs(checked);
    }

    @Test
    @DisplayName("carry a failure through `runKeepingFailure()` and throw it with `throwIfAny()`")
    void carryFailure() {
        var first = new IllegalStateException("first");
        var second = new IllegalArgumentException("second");
        var resource = new StubResource();

        Throwable failure = null;
        failure = runKeepingFailure(failure, () -> { throw first; });
        failure = runKeepingFailure(failure, resource::close);
        failure = runKeepingFailure(failure, () -> { throw second; });
        var kept = failure;

        var thrown = assertThrows(IllegalStateException.class, () -> throwIfAny(kept));

        assertThat(thrown).isSameInstanceAs(first);
        assertThat(thrown.getSuppressed()).asList()
                                          .containsExactly(second);
        assertThat(resource.closed).isTrue();
    }

    @Test
    @DisplayName("return no failure from `runKeepingFailure()` if nothing fails")
    void noFailure() {
        var resource = new StubResource();

        var failure = runKeepingFailure(null, resource::close);

        assertThat(failure).isNull();
        assertThat(resource.closed).isTrue();
    }

    @Test
    @DisplayName("add the failure of a step to a failure that already happened")
    void addToExistingFailure() {
        var buildFailure = new IllegalStateException("build");
        var closeFailure = new IllegalArgumentException("close");

        var thrown = assertThrows(IllegalStateException.class,
                                  () -> failBuild(buildFailure, () -> { throw closeFailure; }));

        assertThat(thrown).isSameInstanceAs(buildFailure);
        assertThat(thrown.getSuppressed()).asList()
                                          .containsExactly(closeFailure);
    }

    /**
     * Fails a build with the given failure, and cleans up the way the documentation
     * of {@code runKeepingFailure()} suggests.
     *
     * <p>The result of {@code runKeepingFailure()} is ignored because it is the caught
     * failure itself. Rethrowing the caught failure keeps the precise rethrow of Java,
     * so the method needs no {@code throws} clause.
     */
    private static void failBuild(RuntimeException failure, Runnable cleanup) {
        try {
            throw failure;
        } catch (Throwable t) {
            runKeepingFailure(t, cleanup);
            throw t;
        }
    }

    @Test
    @DisplayName("wrap a checked failure into `IllegalStateException` in `throwIfAny()`")
    void throwChecked() {
        var checked = new Exception("checked");

        var thrown = assertThrows(IllegalStateException.class, () -> throwIfAny(checked));

        assertThat(thrown).hasCauseThat()
                          .isSameInstanceAs(checked);
    }

    /**
     * A resource which remembers that it was closed.
     */
    private static final class StubResource implements AutoCloseable {

        private boolean closed;

        @Override
        public void close() {
            closed = true;
        }
    }
}
