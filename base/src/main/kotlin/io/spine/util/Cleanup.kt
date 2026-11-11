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

@file:JvmName("Cleanup")

package io.spine.util

/**
 * Runs all the given [steps], even if some of them fail, and then throws the first
 * failure, with the later ones [suppressed][withSuppressed].
 *
 * The steps run in the order they are passed. The failures of the later steps are added
 * to the first one as suppressed in the same order.
 *
 * Kotlin callers pass lambdas:
 *
 * ```kotlin
 * runAll({ commandBus.close() }, { eventBus.close() })
 * ```
 *
 * Java callers pass method references or lambdas:
 *
 * ```java
 * import static io.spine.util.Cleanup.runAll;
 *
 * runAll(commandBus::close, eventBus::close);
 * ```
 *
 * ## Failures of all kinds
 *
 * A failure of any kind is caught, an [Error][kotlin.Error] too, such as
 * an `AssertionError` thrown by a test hook. So no failure skips the rest of the steps.
 * This is what the `try`-with-resources statement of Java and `use` of Kotlin do
 * when closing resources. Like them, this function also catches the errors of the JVM,
 * such as `OutOfMemoryError`. After one, running the rest of the steps is a best effort.
 *
 * ## Interruption
 *
 * If a step throws an `InterruptedException`, the interrupt status of the current thread
 * is restored once all the steps have run. So the interruption does not cut the later steps
 * short, and it is not lost.
 *
 * ## Checked exceptions
 *
 * The first failure is thrown as is if it is unchecked, which means
 * a [RuntimeException] or an [Error][kotlin.Error]. A checked exception is thrown as
 * the cause of an [IllegalStateException]. This function declares no checked exceptions,
 * so Java callers need not catch or declare one. The later failures are then suppressed
 * by the cause, not by the `IllegalStateException`.
 *
 * Only a step written in Kotlin can throw a checked exception without declaring it,
 * since Kotlin does not check them. For [closeAll], it can also come from
 * [close()][java.lang.AutoCloseable.close].
 *
 * @param steps The steps to run.
 * @throws RuntimeException The first failure, if it is a [RuntimeException].
 * @throws kotlin.Error The first failure, if it is an [Error][kotlin.Error].
 * @throws IllegalStateException With the first failure as the cause, if it is
 *   a checked exception.
 * @see closeAll
 */
public fun runAll(vararg steps: Runnable): Unit =
    steps.asList().forEachThenThrow { it.run() }

/**
 * Closes all the resources of this `Iterable`, even if some of them fail to close, and
 * then throws the first failure, with the later ones [suppressed][withSuppressed].
 *
 * The resources are closed in the order of iteration. The failures of the later resources
 * are added to the first one as suppressed in the same order.
 *
 * The resources are copied before closing any of them. So closing a resource may remove it
 * from this `Iterable`, for example, by unregistering it from a live collection.
 *
 * ```kotlin
 * repositories.closeAll()
 * ```
 *
 * ```java
 * import static io.spine.util.Cleanup.closeAll;
 *
 * closeAll(repositories);
 * ```
 *
 * A failure of any kind is caught, an [Error][kotlin.Error] too. A checked exception
 * thrown by [close()][java.lang.AutoCloseable.close] is wrapped into
 * an [IllegalStateException], and an interruption is not lost.
 * Please see [runAll] for details.
 *
 * @throws RuntimeException The first failure, if it is a [RuntimeException].
 * @throws kotlin.Error The first failure, if it is an [Error][kotlin.Error].
 * @throws IllegalStateException With the first failure as the cause, if it is
 *   a checked exception.
 * @see runAll
 */
public fun Iterable<AutoCloseable>.closeAll(): Unit =
    toList().forEachThenThrow { it.close() }

/**
 * Runs the given [step], carrying the given [failure] forward as the first one.
 *
 * Use it when the steps are not known up front, so that [runAll] cannot take them all
 * at once. For example, when some steps run only on a condition, or when cleaning up
 * after a failure that has already happened.
 *
 * If the step succeeds, returns the given [failure], which may be `null`.
 * If the step fails, returns the first of the failures: the given one, with the failure of
 * the step [suppressed][withSuppressed], or the failure of the step, if [failure] is `null`.
 * As with [withSuppressed], a failure is never added to itself. So, if [failure] is
 * not `null`, the result is that very instance.
 *
 * The result is typed as `Throwable`, even if the given failure is a `RuntimeException`,
 * since the step may fail with an [Error][kotlin.Error] or a checked exception.
 *
 * ## Collecting failures
 *
 * Collect the failures with this function, and then throw the kept one, if any,
 * with [throwIfAny]:
 *
 * ```java
 * import static io.spine.util.Cleanup.runKeepingFailure;
 * import static io.spine.util.Cleanup.throwIfAny;
 *
 * Throwable failure = null;
 * if (inbox != null) {
 *     failure = runKeepingFailure(failure, inbox::unregister);
 * }
 * failure = runKeepingFailure(failure, storage::close);
 * throwIfAny(failure);
 * ```
 *
 * ## Cleaning up after a failure
 *
 * When a failure has already happened, the result is that failure. So the caller can
 * rethrow it as is:
 *
 * ```java
 * } catch (Throwable t) {
 *     var unused = runKeepingFailure(t, context::close); // Returns `t`.
 *     throw t;
 * }
 * ```
 *
 * ```kotlin
 * } catch (e: Throwable) {
 *     runKeepingFailure(e) { context.close() }
 *     throw e
 * }
 * ```
 *
 * In Java, the result is assigned because the function's result must not be ignored:
 * ignoring it would lose the failure of the step when there was no failure before.
 * Rethrowing `t` keeps the precise rethrow of Java, so the method needs no
 * `throws Throwable` clause.
 *
 * ## Errors and interruption
 *
 * A failure of any kind is caught, an [Error][kotlin.Error] too, as [runAll] does.
 *
 * If the step throws an `InterruptedException`, the interrupt status of the current
 * thread is restored before this function returns, so the interruption is not lost.
 * Unlike [runAll], which restores it once all its steps have run, this function restores
 * it right away. The steps the caller runs next therefore see the interrupt status set.
 * A later step that blocks, for example, awaiting termination, may then fail at once
 * with another `InterruptedException`.
 *
 * @param failure The failure kept so far, or `null` if nothing has failed yet.
 * @param step The step to run.
 * @return The first failure, or `null` if nothing has failed.
 * @see throwIfAny
 */
@Suppress("TooGenericExceptionCaught") // Catches every failure, as `runAll` does.
public fun runKeepingFailure(failure: Throwable?, step: Runnable): Throwable? =
    try {
        step.run()
        failure
    } catch (e: InterruptedException) {
        Thread.currentThread().interrupt()
        failure.withSuppressed(e)
    } catch (e: Throwable) {
        failure.withSuppressed(e)
    }

/**
 * Throws the given [failure], if it is not `null`.
 *
 * Throws the failure the way [runAll] does. An unchecked failure, which means
 * a [RuntimeException] or an [Error][kotlin.Error], is thrown as is. A checked exception
 * is thrown as the cause of an [IllegalStateException], so Java callers need not catch
 * or declare one.
 *
 * Use it to throw a failure kept by [runKeepingFailure].
 *
 * @param failure The failure to throw, or `null` if nothing has failed.
 * @throws RuntimeException The given failure, if it is a [RuntimeException].
 * @throws kotlin.Error The given failure, if it is an [Error][kotlin.Error].
 * @throws IllegalStateException With the given failure as the cause, if it is
 *   a checked exception.
 * @see runKeepingFailure
 */
public fun throwIfAny(failure: Throwable?) {
    if (failure != null) {
        throw failure.unchecked()
    }
}

/**
 * Keeps the first of two failures, adding the [next] one to it as suppressed.
 *
 * If this failure is `null`, meaning that nothing has failed so far, returns [next].
 * Otherwise, adds [next] to this failure as [suppressed][Throwable.addSuppressed],
 * and returns this failure. So this failure is modified, not copied.
 *
 * Use it to collect the failures of several steps, all of which must run,
 * so that the first failure is thrown and the later ones are not lost:
 *
 * ```kotlin
 * var failure: Throwable? = null
 * for (resource in resources) {
 *     try {
 *         resource.close()
 *     } catch (e: Exception) {
 *         failure = failure.withSuppressed(e)
 *     }
 * }
 * failure?.let { throw it }
 * ```
 *
 * When the steps can run one after another, [runAll] and [closeAll] do all of the above.
 * When the steps are not known up front, [runKeepingFailure] runs them one at a time.
 *
 * ## Self-suppression
 *
 * If [next] is this very failure, nothing is added, and this failure is returned as is.
 * A throwable cannot suppress itself: [Throwable.addSuppressed] would throw
 * an `IllegalArgumentException`, which would replace the failure being kept and skip
 * the remaining steps. Two steps may fail with the same instance, for example,
 * when they rethrow a stored failure or share an exception object.
 *
 * ## Usage from Java
 *
 * ```java
 * import static io.spine.util.Cleanup.withSuppressed;
 *
 * RuntimeException failure = null;
 * for (var resource : resources) {
 *     try {
 *         resource.close();
 *     } catch (RuntimeException e) {
 *         failure = withSuppressed(failure, e);
 *     }
 * }
 * if (failure != null) {
 *     throw failure;
 * }
 * ```
 *
 * @param T The type of the failures. A caller which keeps a `RuntimeException` gets
 *   a `RuntimeException` back, and so can throw it without wrapping.
 *
 * @param next The failure that happened after this one.
 * @return This failure, if it is not `null`, or [next] otherwise.
 */
public fun <T : Throwable> T?.withSuppressed(next: T): T {
    if (this == null) {
        return next
    }
    if (this !== next) {
        addSuppressed(next)
    }
    return this
}

/**
 * Performs the given [action] on each element, even if it fails for some of them, and
 * then throws the first failure, with the later ones [suppressed][withSuppressed].
 *
 * Restores the interrupt status of the current thread after the loop, if the action
 * threw an [InterruptedException] for any of the elements.
 *
 * A checked exception is thrown as the cause of an [IllegalStateException].
 */
@Suppress("TooGenericExceptionCaught") // Catches every failure, so that none skips the rest.
private inline fun <T> Iterable<T>.forEachThenThrow(action: (T) -> Unit) {
    var failure: Throwable? = null
    var interrupted = false
    for (element in this) {
        try {
            action(element)
        } catch (e: InterruptedException) {
            interrupted = true
            failure = failure.withSuppressed(e)
        } catch (e: Throwable) {
            failure = failure.withSuppressed(e)
        }
    }
    if (interrupted) {
        Thread.currentThread().interrupt()
    }
    throwIfAny(failure)
}

/**
 * Returns this throwable if it is unchecked.
 * Otherwise, returns an [IllegalStateException] with this throwable as the cause.
 */
private fun Throwable.unchecked(): Throwable =
    @Suppress("RemoveRedundantQualifierName") // Make which `Error` clear.
    when (this) {
        is RuntimeException, is kotlin.Error -> this
        else -> IllegalStateException(this)
    }
