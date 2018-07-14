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

import com.github.jonathanxd.interoute.gen.RouteSpecInfo;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class RouteSpecInfoUtil {
    /**
     * Filter all route spec info instances which {@link RouteSpecInfo#getInstance() unified instance} is instance of {@code
     * unificationType}.
     *
     * @param stream          Stream to apply filter.
     * @param unificationType Unification type instance.
     * @param <T>             Unification type.
     * @return Stream with filtered spec info elements.
     */
    @SuppressWarnings("unchecked")
    public static <T> Stream<? extends RouteSpecInfo<? extends T>> filterByUnificationType(
            @NotNull Stream<RouteSpecInfo<?>> stream,
            @NotNull Class<T> unificationType) {
        return stream
                .filter(it -> unificationType.isInstance(it.getInstance()))
                .map(it -> (RouteSpecInfo<? extends T>) it);
    }

}
