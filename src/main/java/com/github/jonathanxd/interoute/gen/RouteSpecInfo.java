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
package com.github.jonathanxd.interoute.gen;

import com.github.jonathanxd.interoute.annotation.RouteInfo;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Specification of {@link RouteInfo}
 */
public final class RouteSpecInfo<T> {

    /**
     * Annotation type.
     */
    private final Class<?> annotationType;

    /**
     * Annotation unifier type (value provided in {@link RouteInfo#value()}).
     */
    private final Class<? extends AnnotationUnifier<T>> unifier;

    /**
     * Unification instance.
     */
    private final T instance;

    public RouteSpecInfo(Class<?> annotationType, Class<? extends AnnotationUnifier<T>> unifier, T instance) {
        this.annotationType = annotationType;
        this.unifier = unifier;
        this.instance = instance;
    }

    /**
     * Gets the annotation type.
     *
     * @return Annotation type.
     */
    @Contract(pure = true)
    @NotNull
    public Class<?> getAnnotationType() {
        return this.annotationType;
    }

    /**
     * Gets the annotation unifier.
     *
     * @return Annotation unifier.
     */
    @Contract(pure = true)
    @NotNull
    public Class<? extends AnnotationUnifier<T>> getUnifier() {
        return this.unifier;
    }

    /**
     * Gets the unified instance.
     *
     * @return unified instance.
     */
    @Contract(pure = true)
    @NotNull
    public T getInstance() {
        return this.instance;
    }
}
