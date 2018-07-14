/*
 *      Interoute - Interface routing framework. <https://github.com/JonathanxD/Interoute>
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2018 TheRealBuggy/JonathanxD (https://github.com/JonathanxD/) <jonathan.scripter@programmer.net>
 *      Copyright (c) contributors
 *
 *
 *      Permission is hereby granted, free of charge, to any person obtaining a copy
 *      of this software and associated documentation files (the "Software"), to deal
 *      in the Software without restriction, including without limitation the rights
 *      to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *      copies of the Software, and to permit persons to whom the Software is
 *      furnished to do so, subject to the following conditions:
 *
 *      The above copyright notice and this permission notice shall be included in
 *      all copies or substantial portions of the Software.
 *
 *      THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *      IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *      FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *      AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *      LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *      OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *      THE SOFTWARE.
 */
package com.github.jonathanxd.interoute.util;

import com.github.jonathanxd.iutils.object.result.Result;
import com.github.jonathanxd.iutils.reflection.Reflection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Stores instance factories.
 */
public final class InstanceFactories {
    private static List<RegisteredInstanceFactory<?>> FACTORY_LIST = new ArrayList<>();

    static {
        InstanceFactories.registerFactory(Object.class, 0, clazz -> {
            try {
                return Result.ok(Reflection.getInstance(clazz));
            } catch (Throwable t) {
                return Result.error(t);
            }
        });

        InstanceFactories.registerFactory(Object.class, 0, clazz -> {
            try {
                return Result.ok(clazz.newInstance());
            } catch (Throwable t) {
                return Result.error(t);
            }
        });
    }

    /**
     * Creates {@link T} instance from {@code tClass}.
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> tClass) {
        List<Result<T, Throwable>> fails = new ArrayList<>();

        for (RegisteredInstanceFactory<?> registeredInstanceFactory : FACTORY_LIST.stream()
                .filter(it -> it.type.isAssignableFrom(tClass))
                .sorted(Comparator.comparingInt(RegisteredInstanceFactory::getPriority))
                .collect(Collectors.toList())) {
            Result<T, Throwable> instance = ((RegisteredInstanceFactory<T>) registeredInstanceFactory).getInstanceFactory()
                    .create(tClass);

            if (instance instanceof Result.Ok<?, ?>) {
                return ((Result.Ok<T, Throwable>) instance).success();
            } else {
                fails.add(instance);
            }
        }


        IllegalArgumentException exception = new IllegalArgumentException(
                String.format("Cannot create instance of type `%s`: There is no factory registered for this type.",
                        tClass.getCanonicalName()));

        for (Result<T, Throwable> fail : fails) {
            exception.addSuppressed(Objects.requireNonNull(fail.errorOrNull()));
        }

        throw exception;
    }

    /**
     * Registers {@code factory} of {@code type} {@link T} with {@code priority}.
     */
    public static <T> void registerFactory(Class<T> type, int priority, InstanceFactory<T> factory) {
        FACTORY_LIST.add(new RegisteredInstanceFactory<>(factory, type, priority));
    }

    static final class RegisteredInstanceFactory<T> {
        final InstanceFactory<T> instanceFactory;
        final Class<T> type;
        final int priority;

        RegisteredInstanceFactory(InstanceFactory<T> instanceFactory, Class<T> type, int priority) {
            this.instanceFactory = instanceFactory;
            this.type = type;
            this.priority = priority;
        }

        public InstanceFactory<T> getInstanceFactory() {
            return this.instanceFactory;
        }

        public Class<T> getType() {
            return this.type;
        }

        public int getPriority() {
            return this.priority;
        }
    }
}
