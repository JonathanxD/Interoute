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
package com.github.jonathanxd.interoute.route;

import com.github.jonathanxd.kores.common.MethodTypeSpec;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Origin implementation which the origin is a {@link MethodTypeSpec}.
 */
public final class MethodTypeSpecOrigin implements Origin {
    /**
     * Origin method.
     */
    @NotNull
    private final MethodTypeSpec methodTypeSpec;

    public MethodTypeSpecOrigin(@NotNull MethodTypeSpec methodTypeSpec) {
        this.methodTypeSpec = methodTypeSpec;
    }

    /**
     * Gets the origin method.
     *
     * @return Origin method.
     */
    @Contract(pure = true)
    @NotNull
    public MethodTypeSpec getMethodTypeSpec() {
        return this.methodTypeSpec;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodTypeSpecOrigin that = (MethodTypeSpecOrigin) o;
        return Objects.equals(this.getMethodTypeSpec(), that.getMethodTypeSpec());
    }

    @Contract(pure = true)
    @Override
    public int hashCode() {
        return Objects.hash(this.getMethodTypeSpec());
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public String toString() {
        return "MethodTypeSpecOrigin{" +
                "methodTypeSpec=" + this.getMethodTypeSpec() +
                '}';
    }
}
