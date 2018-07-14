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

import com.github.jonathanxd.interoute.backend.InterouteBackend;
import com.github.jonathanxd.interoute.backend.InterouteBackendConfiguration;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

/**
 * Specification of {@link com.github.jonathanxd.interoute.annotation.RouterInterface}.
 *
 * @param <C> Backend configuration type.
 */
public final class RouterSpec<C extends InterouteBackendConfiguration> {

    /**
     * Router interface.
     */
    @NotNull
    private final Type routerInterface;

    /**
     * Implementation generation backend.
     */
    @NotNull
    private final InterouteBackend<C> backend;

    /**
     * Backend configuration.
     */
    @NotNull
    private final C configuration;

    /**
     * Routes specifications.
     */
    @NotNull
    private final List<RouteSpec> routeSpecList;

    public RouterSpec(@NotNull Type routerInterface,
                      @NotNull InterouteBackend<C> backend,
                      @NotNull C configuration,
                      @NotNull List<RouteSpec> routeSpecList) {
        this.routerInterface = routerInterface;
        this.backend = backend;
        this.configuration = configuration;
        this.routeSpecList = routeSpecList;
    }

    /**
     * Gets the router interface.
     *
     * @return Router interface.
     */
    @Contract(pure = true)
    @NotNull
    public Type getRouterInterface() {
        return this.routerInterface;
    }

    /**
     * Gets the implementation generation backend.
     *
     * @return Implementation generation backend.
     */
    @Contract(pure = true)
    @NotNull
    public InterouteBackend<C> getBackend() {
        return this.backend;
    }

    /**
     * Gets the backend configuration.
     *
     * @return Backend configuration.
     */
    @Contract(pure = true)
    @NotNull
    public C getConfiguration() {
        return this.configuration;
    }

    /**
     * Gets the routes specifications list.
     *
     * @return Routes specifications list.
     */
    @Contract(pure = true)
    @NotNull
    public List<RouteSpec> getRouteSpecList() {
        return this.routeSpecList;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RouterSpec that = (RouterSpec) o;
        return Objects.equals(this.getRouterInterface(), that.getRouterInterface()) &&
                Objects.equals(this.getBackend(), that.getBackend()) &&
                Objects.equals(this.getRouteSpecList(), that.getRouteSpecList());
    }

    @Contract(pure = true)
    @Override
    public int hashCode() {
        return Objects.hash(this.getRouterInterface(), this.getBackend(), this.getRouteSpecList());
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public String toString() {
        return "RouterSpec{" +
                "routerInterface=" + this.getRouterInterface() +
                ", backend=" + this.getBackend() +
                ", routeSpecList=" + this.getRouteSpecList() +
                '}';
    }
}
