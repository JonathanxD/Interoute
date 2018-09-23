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
package com.github.jonathanxd.interoute.test;

import com.github.jonathanxd.interoute.Interoute;
import com.github.jonathanxd.interoute.annotation.BackendConfigurer;
import com.github.jonathanxd.interoute.annotation.RouteInfo;
import com.github.jonathanxd.interoute.annotation.RouteTo;
import com.github.jonathanxd.interoute.annotation.RouterInterface;
import com.github.jonathanxd.interoute.backend.InterouteBackendConfigurer;
import com.github.jonathanxd.interoute.backend.def.DefaultBackendConfiguration;
import com.github.jonathanxd.interoute.backend.def.DefaultBackendDestination;
import com.github.jonathanxd.interoute.exception.RoutingException;
import com.github.jonathanxd.interoute.gen.AnnotationUnifier;
import com.github.jonathanxd.interoute.route.Destination;
import com.github.jonathanxd.interoute.route.MethodTypeSpecOrigin;
import com.github.jonathanxd.interoute.route.Origin;
import com.github.jonathanxd.interoute.route.Route;
import com.github.jonathanxd.interoute.route.SuppliedExecutorRoute;
import com.github.jonathanxd.iutils.exception.RethrowException;
import com.github.jonathanxd.iutils.object.result.Result;
import com.github.jonathanxd.kores.common.MethodTypeSpec;
import com.github.jonathanxd.kores.factory.Factories;

import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SimpleInterouteDefaultBackend {

    static int globalResult;

    @SuppressWarnings("unchecked")
    @Test
    public void methodRouteCalc() {
        Result<? extends InternalRouter, ? extends Exception> routerResult = /*new InternalRouterImpl();*/Interoute
                .createRouter(InternalRouter.class);

        if (routerResult instanceof Result.Err<?, ?>) {
            throw RethrowException.rethrow(((Result.Err<InternalRouter, ? extends Exception>) routerResult).error());
        } else {
            InternalRouter router = routerResult.successOrNull();
            try {
                Object result = router.calc(5, 2)
                        .execute()
                        .get()
                        .getSuccessOrError(integer -> integer, error -> error);

                Assert.assertEquals(5 + 2, result);

                router.calc2(5, 2);

                Assert.assertEquals(5 + 2, globalResult);

            } catch (InterruptedException | ExecutionException e) {
                throw RethrowException.rethrow(e);
            }
        }
    }

    @RouterInterface
    @BackendConfigurer(value = SimpleConfigurer.class)
    @Get
    public interface InternalRouter {
        @RouteTo("Math.plus")
        @Get
        Route<Integer> calc(@Body int a, int b);

        @RouteTo("Math.plus2")
        void calc2(int a, int b);
    }

    public static class MathPlus {
        public int plus(int a, int b) {
            return a + b;
        }

        public void plus2(int a, int b) {
            globalResult = this.plus(a, b);
        }
    }

    public static class SimpleConfigurer implements InterouteBackendConfigurer<DefaultBackendConfiguration> {
        public static final SimpleConfigurer INSTANCE = new SimpleConfigurer();

        @NotNull
        @Override
        public DefaultBackendConfiguration configure(@NotNull DefaultBackendConfiguration configuration) {
            return configuration.toBuilder()
                    .addInstance("Math", new MathPlus())
                    .build();
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    @RouteInfo(AnnotationUnifier.Self.class)
    public @interface Get {

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    @RouteInfo(AnnotationUnifier.Self.class)
    public @interface Body {

    }

    public class GetUnifier implements AnnotationUnifier<Annotation> {

        @SuppressWarnings("unchecked")
        @Override
        @NotNull
        public Annotation unify(@NotNull Annotation annotation) {
            return annotation;
        }
    }

    class InternalRouterImpl implements InternalRouter {

        MathPlus mathPlus = new MathPlus();

        @Override
        public Route<Integer> calc(int a, int b) {
            Origin origin = new MethodTypeSpecOrigin(new MethodTypeSpec(InternalRouterImpl.class, "calc",
                    Factories.typeSpec(Route.class, Integer.TYPE, Integer.TYPE)));
            Destination destination = new DefaultBackendDestination(mathPlus, new MethodTypeSpec(MathPlus.class, "plus",
                    Factories.typeSpec(Integer.TYPE, Integer.TYPE, Integer.TYPE)));
            return new SuppliedExecutorRoute<>(origin, destination, () -> CompletableFuture.supplyAsync(() -> {
                try {
                    return Result.ok(mathPlus.plus(a, b));
                } catch (Throwable t) {
                    return Result.error(new RoutingException(t));
                }
            }));
        }

        @Override
        public void calc2(int a, int b) {
            globalResult = a + b;
        }
    }
}
