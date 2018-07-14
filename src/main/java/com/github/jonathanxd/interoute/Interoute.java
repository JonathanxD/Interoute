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
package com.github.jonathanxd.interoute;

import com.github.jonathanxd.interoute.exception.RouterCreationException;
import com.github.jonathanxd.iutils.object.result.Result;

/**
 * Router factory.
 */
public final class Interoute {

    /**
     * Creates a {@link com.github.jonathanxd.interoute.route.Router} from {@code router} interface.
     *
     * @param router Router interface.
     * @param <I>    Router type.
     * @return Result with either router instance or {@link RouterCreationException}.
     */
    public static <I> Result<? extends I, RouterCreationException> createRouter(Class<I> router) {
        return Result.<Class<I>, RouterCreationException>ok(router)
                .flatMap(InteroureAnnotationParse::validate)
                .flatMap(InteroureAnnotationParse::getRouterSpec)
                .flatMap(r -> r.getBackend().<I>generate(r).mapError(RouterCreationException::new));
    }

}
