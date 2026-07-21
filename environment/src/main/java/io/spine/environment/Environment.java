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

package io.spine.environment;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spine.annotation.SPI;
import io.spine.annotation.VisibleForTesting;
import io.spine.logging.WithLogging;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.reflect.Invokables.callParameterlessCtor;
import static io.spine.string.Diags.backtick;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.lang.String.format;

/**
 * Provides information about the environment (current platform used, etc.).
 *
 * <h2>Detecting the type of the environment</h2>
 *
 * <p>It is possible to {@linkplain #type() obtain the type} of the current
 * environment, or to check whether the current environment type {@linkplain #is(Class) matches
 * another type}.
 * <pre>
 *
 * public final class Application {
 *
 *     private final EmailSender sender;
 *
 *     private Application() {
 *         var environment = Environment.instance();
 *         if (environment.is(Tests.class)) {
 *             // Do not send out emails if in tests.
 *             this.sender = new MockEmailSender();
 *         } else {
 *             this.sender = EmailSender.withConfig("email_gateway.yml");
 *         }
 *         //...
 *     }
 * }
 * </pre>
 *
 * <p>The following standard types are always available for checking with the current environment:
 *
 * <ul>
 *     <li><em>{@link Tests}</em> is detected if the current call stack has a reference to
 *     a {@linkplain Tests#knownTestingFrameworks() unit testing framework}.

 *     <li><em>{@link DefaultMode}</em> is set in all other cases.
 * </ul>
 *
 * <p>The framework users may define their custom settings depending on the current environment
 * type and then {@linkplain #register(Class) register} them for further detection. Custom types
 * are {@linkplain CustomEnvironmentType#enabled() evaluated} in the order reverse to registration.
 * That is, the last-registered type would be checked first and so on.
 * Please see {@link CustomEnvironmentType} for details.
 *
 * <h3>When environment changes</h3>
 *
 * <p>When calculating the {@linkplain Environment#type() current type} {@code Environment}
 * finds the first {@linkplain EnvironmentType#enabled() enabled} {@code EnvironmentType}, and
 * then remembers it as the currently active. Subsequent calls will not cause re-evaluation of
 * the {@link EnvironmentType#enabled()}.
 *
 * <p>If later the current type becomes logically inactive, {@code Environment} needs to be
 * updated by one of the following approaches:
 * <ul>
 *     <li>Setting a new environment type, directly by calling {@link #setTo(Class)}.
 *     <li>Calling the {@link #reset()} method (which would drop the currently selected type)
 *     and re-registering environment types.
 * </ul>
 * <pre>
 *
 *     var environment = Environment.instance();
 *     environment.register(AwsLambda.class);
 *     assertThat(environment.is(AwsLambda.class)).isTrue();
 *
 *     System.clearProperty(AwsLambda.AWS_ENV_VARIABLE);
 *
 *     // Even though `AwsLambda` is not active, we have remembered the value, and
 *     // `is(AwsLambda.class)` is `true`.
 *     assertThat(environment.is(AwsLambda.class)).isTrue();
 *
 *     environment.reset();
 *     environment.register(AwsLambda.class);
 *
 *     // When `reset()` is called, the stored value is cleared.
 *     assertThat(environment.is(AwsLambda.class)).isFalse();
 * </pre>
 *
 * @see EnvironmentType
 * @see CustomEnvironmentType
 * @see Tests
 * @see DefaultMode
 */
@SPI
public final class Environment implements WithLogging {

    private static final ImmutableList<StandardEnvironmentType<?>> STANDARD_TYPES =
            ImmutableList.of(Tests.type(), DefaultMode.type());

    private static final Environment INSTANCE = new Environment();

    /**
     * The types the environment can be in.
     *
     * <p>Always contains {@link #STANDARD_TYPES} as the last two elements.
     *
     * @see #register(EnvironmentType)
     */
    private ImmutableList<EnvironmentType<?>> knownTypes;

    /**
     * The type the environment is in.
     *
     * <p>If {@code null} the type will be {@linkplain #type() determined} among
     * {@linkplain #knownTypes already known} types.
     *
     * @implNote This field is explicitly initialized to avoid the "non-initialized" warning
     *         when queried for the first time.
     */
    private @Nullable Class<? extends EnvironmentType<?>> currentType = null;

    /**
     * Creates a new instance with only {@linkplain #STANDARD_TYPES base known types}.
     */
    private Environment() {
        this.knownTypes = standardOnly();
    }

    private static ImmutableList<EnvironmentType<?>> standardOnly() {
        return ImmutableList.copyOf(STANDARD_TYPES);
    }

    /** Creates a new instance with the copy of the state of the passed environment. */
    private Environment(Environment copy) {
        this.knownTypes = copy.knownTypes;
        setCurrentType(copy.currentType);
    }

    /**
     * Remembers the specified environment type, allowing one {@linkplain #is(Class) to
     * determine whether it's enabled} later.
     *
     * <p>Note that the default types are still present.
     * When trying to determine which environment type is enabled, the user-defined types are
     * checked first, in the first-registered to last-registered order.
     *
     * @param type
     *         a user-defined environment type
     * @return this instance of {@code Environment}
     * @see Tests
     * @see DefaultMode
     */
    @CanIgnoreReturnValue
    private Environment register(EnvironmentType<?> type) {
        if (!knownTypes.contains(type)) {
            var currentlyKnown = knownTypes;
            knownTypes = ImmutableList.<EnvironmentType<?>>builder()
                    .add(type)
                    .addAll(currentlyKnown)
                    .build();
            autoDetect();
        }
        return this;
    }

    /**
     * Gives custom environment types a chance to be detected as the current one
     * in response to the changes of their surroundings.
     *
     * <p>Unlike {@link #reset()} this method keeps custom types, but clears
     * the currently detected one.
     */
    @VisibleForTesting
    void autoDetect() {
        setCurrentType(null);
    }

    /**
     * Configures a callback to be called when the corresponding environment type is
     * {@linkplain EnvironmentType#enabled() detected}.
     *
     * @param cls
     *         the class of the environment type, which must be
     *         {@linkplain #register(Class) registered} prior making this call
     * @param callback
     *         the callback to be called or {@code null} to clear previously configure callback
     * @param <T>
     *         the type of the environment
     */
    public <T extends EnvironmentType<T>>
    void whenDetected(Class<T> cls, @Nullable Consumer<T> callback) {
        var found = knownTypes.stream()
                .filter(t -> t.getClass().equals(cls))
                .findFirst()
                .orElseThrow(() -> newIllegalArgumentException(
                        "The environment type `%s` was not registered." +
                                " Please call `register(%s)`.",
                        cls.getCanonicalName(), cls.getCanonicalName()
                ));
        @SuppressWarnings("unchecked")
        var foundType = (T) found;
        foundType.onDetected(callback);
    }

    /**
     * Remembers the specified environment type, allowing one {@linkplain #is(Class) to
     * determine whether it's enabled} later.
     *
     * <p>The specified {@code type} must have a parameterless constructor. The
     * {@code EnvironmentType} is going to be instantiated using the parameterless constructor.
     *
     * @param type
     *         environment type to register
     * @return this instance of {@code Environment}
     */
    @CanIgnoreReturnValue
    public Environment register(Class<? extends CustomEnvironmentType<?>> type) {
        EnvironmentType<?> newType = callParameterlessCtor(type);
        return register(newType);
    }

    /** Returns the singleton instance. */
    public static Environment instance() {
        return INSTANCE;
    }

    /**
     * Creates a copy of the instance so that it can be later
     * restored via {@link #restoreFrom(Environment)} by cleanup in tests.
     */
    @VisibleForTesting
    public Environment createCopy() {
        return new Environment(this);
    }

    /**
     * Determines whether the current environment is the same as the specified one.
     *
     * <p>If custom environment types have been {@linkplain #register(Class) registered},
     * the method goes through them in the latest-registered to earliest-registered order.
     * Then, checks {@link Tests} and {@link DefaultMode}.
     *
     * <p>Please note that this method follows assignment compatibility:
     * <pre>
     *
     *     abstract class AppEngine extends EnvironmentType {
     *         ...
     *     }
     *
     *     final class AppEngineStandard extends AppEngine {
     *         ...
     *     }
     *
     *     Environment environment = Environment.instance();
     *
     *     // Assuming we are under App Engine Standard
     *     assertThat(environment.is(AppEngine.class)).isTrue();
     *
     * </pre>
     *
     * @return whether the current environment type matches the specified one
     */
    @SuppressWarnings("rawtypes") // to avoid ugly casts at call sites.
    public boolean is(Class<? extends EnvironmentType> type) {
        var current = type();
        var result = type.isAssignableFrom(current);
        return result;
    }

    /**
     * Returns the type of the current environment.
     *
     * <p>If the type was not {@linkplain #setTo(Class) selected explicitly} before,
     * returns the first one that is {@linkplain EnvironmentType#enabled() enabled},
     * starting from the most recently registered type.
     *
     * @see #register(Class)
     */
    public Class<? extends EnvironmentType<?>> type() {
        if (currentType == null) {
            EnvironmentType<?> result;
            result = firstEnabled();
            var cls = classOf(result);
            setCurrentType(cls);
            result.callback();
            return cls;
        } else {
            return currentType;
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends EnvironmentType<?>> classOf(EnvironmentType<?> result) {
        return (Class<? extends EnvironmentType<?>>) result.getClass();
    }

    private EnvironmentType<?> firstEnabled() {
        var result = knownTypes.stream()
                .filter(EnvironmentType::enabled)
                .findFirst()
                .orElseThrow(() -> newIllegalStateException(
                        "`Environment` could not find an active environment type."
                ));
        return result;
    }

    /**
     * Restores the state from the instance created by {@link #createCopy()}.
     *
     * <p>Call this method when cleaning up tests that modify {@code Environment}.
     */
    @VisibleForTesting
    public void restoreFrom(Environment copy) {
        // Make sure this matches the set of fields copied in the copy constructor.
        this.knownTypes = copy.knownTypes;
        setCurrentType(copy.currentType);
    }

    /**
     * Sets the current environment type to the specified one. Overrides the current value.
     *
     * <p>If the supplied type was not {@linkplain #register(Class) registered} previously,
     * it is registered.
     */
    public void setTo(Class<? extends EnvironmentType<?>> type) {
        checkNotNull(type);
        if (CustomEnvironmentType.class.isAssignableFrom(type)) {
            @SuppressWarnings("unchecked") // checked one line above
            var customType =
                    (Class<? extends CustomEnvironmentType<?>>) type;
            register(customType);
        }
        setCurrentType(type);
    }

    private void setCurrentType(@Nullable Class<? extends EnvironmentType<?>> newCurrent) {
        var previous = this.currentType;
        this.currentType = newCurrent;
        if (previous == null) {
            if (newCurrent != null) {
                logger().atDebug().log(() -> format(
                        "`Environment` set to `%s`.", newCurrent.getName()));
            }
        } else {
            if (previous.equals(newCurrent)) {
                logger().atDebug().log(() -> format(
                        "`Environment` stays `%s`.", newCurrent.getName()));
            } else {
                var newType = newCurrent != null
                              ? backtick(newCurrent.getName())
                              : "undefined";
                logger().atDebug().log(() -> format(
                        "`Environment` turned from `%s` to %s.", previous.getName(), newType));
            }
        }
    }

    /**
     * Resets the instance and clears the {@link TestsProperty}.
     *
     * <p>Erases all registered environment types, leaving only {@code Tests} and
     * {@code Production}.
     */
    @VisibleForTesting
    public void reset() {
        autoDetect();
        this.knownTypes = standardOnly();
        TestsProperty.clear();
    }
}
