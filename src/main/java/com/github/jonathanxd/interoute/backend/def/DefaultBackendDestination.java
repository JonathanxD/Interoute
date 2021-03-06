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
package com.github.jonathanxd.interoute.backend.def;

import com.github.jonathanxd.interoute.route.Destination;
import com.github.jonathanxd.kores.common.MethodTypeSpec;

import org.jetbrains.annotations.Contract;

import java.util.Objects;

/**
 * Default backend implementation of destination.
 */
public class DefaultBackendDestination implements Destination {

    /**
     * Target instance.
     */
    private final Object instance;

    /**
     * Target method to invoke.
     */
    private final MethodTypeSpec methodTypeSpec;

    public DefaultBackendDestination(Object instance, MethodTypeSpec methodTypeSpec) {
        this.instance = instance;
        this.methodTypeSpec = methodTypeSpec;
    }

    /**
     * Gets target instance.
     *
     * @return Target instance.
     */
    public Object getInstance() {
        return this.instance;
    }

    /**
     * Gets target method to invoke.
     *
     * @return Target method to invoke.
     */
    public MethodTypeSpec getMethodTypeSpec() {
        return this.methodTypeSpec;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultBackendDestination that = (DefaultBackendDestination) o;
        return Objects.equals(this.getInstance(), that.getInstance()) &&
                Objects.equals(this.getMethodTypeSpec(), that.getMethodTypeSpec());
    }

    @Contract(pure = true)
    @Override
    public int hashCode() {
        return Objects.hash(this.getInstance(), this.getMethodTypeSpec());
    }

    @Override
    public String toString() {
        return "DefaultBackendDestination{" +
                "instance=" + this.getInstance() +
                ", methodTypeSpec=" + this.getMethodTypeSpec() +
                '}';
    }
}
