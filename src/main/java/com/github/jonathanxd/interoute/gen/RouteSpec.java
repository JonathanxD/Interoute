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

import com.github.jonathanxd.kores.base.MethodDeclaration;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Specification of {@link com.github.jonathanxd.interoute.annotation.RouteTo}.
 */
public final class RouteSpec {
    /**
     * Origin method.
     */
    @NotNull
    private final MethodDeclaration origin;

    /**
     * Origin parameters specification
     */
    @NotNull
    private final List<RouteParameterSpec> originParameterSpecs;

    /**
     * Destination notation.
     */
    @NotNull
    private final String destination;

    /**
     * Route spec info list.
     */
    @NotNull
    private final List<RouteSpecInfo<?>> routeSpecInfoList;

    public RouteSpec(@NotNull MethodDeclaration origin,
                     @NotNull List<RouteParameterSpec> originParameterSpecs,
                     @NotNull String destination,
                     @NotNull List<RouteSpecInfo<?>> routeSpecInfoList) {
        this.origin = origin;
        this.originParameterSpecs = originParameterSpecs;
        this.destination = destination;
        this.routeSpecInfoList = Collections.unmodifiableList(new ArrayList<>(routeSpecInfoList));
    }

    /**
     * Gets the origin method.
     *
     * @return Origin method.
     */
    @Contract(pure = true)
    @NotNull
    public MethodDeclaration getOrigin() {
        return this.origin;
    }

    /**
     * Gets the origin method parameters specs.
     *
     * @return Origin method parameters specs.
     */
    @Contract(pure = true)
    @NotNull
    public List<RouteParameterSpec> getOriginParameterSpecs() {
        return this.originParameterSpecs;
    }

    /**
     * Gets the destination notation.
     *
     * @return Destination notation.
     */
    @Contract(pure = true)
    @NotNull
    public String getDestination() {
        return this.destination;
    }

    /**
     * Gets the route specification info list.
     *
     * @return Route specification info list.
     */
    @Contract(pure = true)
    @NotNull
    public List<RouteSpecInfo<?>> getRouteSpecInfoList() {
        return this.routeSpecInfoList;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RouteSpec routeSpec = (RouteSpec) o;
        return Objects.equals(this.getOrigin(), routeSpec.getOrigin()) &&
                Objects.equals(this.getOriginParameterSpecs(), routeSpec.getOriginParameterSpecs()) &&
                Objects.equals(this.getDestination(), routeSpec.getDestination()) &&
                Objects.equals(this.getRouteSpecInfoList(), routeSpec.getRouteSpecInfoList());
    }

    @Contract(pure = true)
    @Override
    public int hashCode() {
        return Objects.hash(this.getOrigin(), this.getOriginParameterSpecs(), this.getDestination(), this.getRouteSpecInfoList());
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public String toString() {
        return "RouteSpec{" +
                "origin=" + origin +
                ", destination='" + destination + '\'' +
                ", routeSpecInfoList=" + routeSpecInfoList +
                ", originParameterSpecs=" + originParameterSpecs +
                '}';
    }
}
