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
package com.github.jonathanxd.interoute.route.def;

import com.github.jonathanxd.interoute.exception.DestinationParseException;
import com.github.jonathanxd.iutils.object.result.Result;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Resolver of destination notation to valid objects.
 *
 * @param <R> Root element type.
 * @param <T> Child element type/target element type.
 */
public interface DestinationResolver<R, T> {

    /**
     * Creates a {@link DestinationResolver} from {@code rootResolver} and {@code targetResolver}.
     *
     * @param rootResolver   Root element resolver.
     * @param targetResolver Target element resolver.
     * @param <R>            Root element type.
     * @param <T>            Target element type.
     * @return {@link DestinationResolver} which resolves elements using {@code rootResolver} and {@code targetResolver}.
     */
    @NotNull
    static <R, T> DestinationResolver<R, T> create(@NotNull RootResolver<R> rootResolver,
                                                   @NotNull TargetResolver<R, T> targetResolver) {
        return new DestinationResolver<R, T>() {
            @NotNull
            @Override
            public RootResolver<R> rootResolver() {
                return rootResolver;
            }

            @NotNull
            @Override
            public TargetResolver<R, T> targetResolver() {
                return targetResolver;
            }
        };
    }

    /**
     * Resolver of root element.
     *
     * @return Resolver of root element.
     */
    @NotNull
    RootResolver<R> rootResolver();

    /**
     * Resolver of target element.
     *
     * @return Resolver of target element.
     */
    @NotNull
    TargetResolver<R, T> targetResolver();

    /**
     * Resolver of root element.
     *
     * @param <R> Root element type.
     */
    interface RootResolver<R> {
        /**
         * Resolves the root element specified in {@code notation}.
         *
         * @param notation Element notation.
         * @return {@link Result} with either resolved element or {@link DestinationParseException notation parsing exception}.
         */
        @NotNull
        Result<R, DestinationParseException> resolve(@NotNull String notation);
    }

    /**
     * Resolver of target element.
     *
     * @param <R> Root element type.
     * @param <T> Target element type.
     */
    interface TargetResolver<R, T> {
        /**
         * Resolves the target element specified in {@code notation}.
         *
         * @param notation Element notation.
         * @param root     Root element (the element which the element is located).
         * @return {@link Result} with either resolved element or {@link DestinationParseException notation parsing exception}.
         */
        @NotNull
        Result<T, DestinationParseException> resolve(@NotNull String notation, @Nullable R root);
    }
}
