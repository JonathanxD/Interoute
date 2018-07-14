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

import com.github.jonathanxd.interoute.exception.RoutingException;
import com.github.jonathanxd.iutils.object.result.Result;

import java.util.concurrent.CompletableFuture;

/**
 * Base interface of route implementation, routes always have an {@link #getOrigin() Origin} and a {@link #getDestination()
 * Desination} and could be {@link #execute() executed} as many times as needed.
 *
 * Routes are commonly executed {@code asynchronously}, thus always returns a {@link CompletableFuture}, but this {@link
 * CompletableFuture} should only fail with an exception if something wrong happened on the backend side, exceptions threw by
 * {@link #getDestination() Destination} are stored in {@link Result} instance.
 */
public interface Route<R> {

    /**
     * Executes the route and return a {@link CompletableFuture} which completes when the {@link #getDestination() Destination}
     * returns a {@link Result result}.
     *
     * @return {@link CompletableFuture} which completes with {@link Result} responded by {@link #getDestination() Destination}.
     */
    CompletableFuture<Result<R, RoutingException>> execute();

    /**
     * Gets the origin of the route.
     *
     * @return Origin of the route.
     */
    Origin getOrigin();

    /**
     * Gets the destination of the route.
     *
     * @return Destination of the route.
     */
    Destination getDestination();
}
