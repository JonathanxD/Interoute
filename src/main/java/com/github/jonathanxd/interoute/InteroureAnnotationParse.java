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

import com.github.jonathanxd.interoute.annotation.BackendConfigurer;
import com.github.jonathanxd.interoute.annotation.RequiresConfiguration;
import com.github.jonathanxd.interoute.annotation.RouteInfo;
import com.github.jonathanxd.interoute.annotation.RouteTo;
import com.github.jonathanxd.interoute.annotation.RouterInterface;
import com.github.jonathanxd.interoute.backend.InterouteBackend;
import com.github.jonathanxd.interoute.backend.InterouteBackendConfiguration;
import com.github.jonathanxd.interoute.backend.InterouteBackendConfigurer;
import com.github.jonathanxd.interoute.exception.RouterCreationException;
import com.github.jonathanxd.interoute.gen.AnnotationUnifier;
import com.github.jonathanxd.interoute.gen.RouteSpec;
import com.github.jonathanxd.interoute.gen.RouteSpecInfo;
import com.github.jonathanxd.interoute.gen.RouterSpec;
import com.github.jonathanxd.interoute.util.InstanceFactories;
import com.github.jonathanxd.iutils.object.result.Result;
import com.github.jonathanxd.iutils.reflection.Reflection;
import com.github.jonathanxd.kores.util.conversion.ConversionsKt;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * "Parser" of Interoute annotations. This class contains utilities to create Interoute specifications from annotations.
 */
public class InteroureAnnotationParse {

    /**
     * Creates {@link RouteSpec} from {@code router} interface.
     *
     * @param router Router interface.
     * @param <C>    Backend configuration.
     * @return Result with either router specification or {@link RouterCreationException}.
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public static <C extends InterouteBackendConfiguration> Result<RouterSpec<C>, RouterCreationException> getRouterSpec(
            @NotNull Class<?> router) {
        RouterInterface declaredAnnotation = router.getDeclaredAnnotation(RouterInterface.class);

        InterouteBackend<C> backend = (InterouteBackend<C>) InstanceFactories.create(declaredAnnotation.value());

        if (backend.getClass().isAnnotationPresent(RequiresConfiguration.class)
                && (!router.isAnnotationPresent(BackendConfigurer.class)
                || router.getDeclaredAnnotation(BackendConfigurer.class).value().length == 0)) {
            return Result.error(new RouterCreationException(
                    String.format("Provided backed '%s' requires at least one @BackendConfigurer.",
                            declaredAnnotation.value().getCanonicalName())));
        } else {
            return Result.ok(new RouterSpec<>(
                            router,
                            backend,
                            InteroureAnnotationParse.createConfiguration(backend, router),
                            Reflection.getMethods(router).stream()
                                    .map(InteroureAnnotationParse::createRouteSpec)
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .collect(Collectors.toList()),
                            InteroureAnnotationParse.createSpecInfoList(router)
                    )
            );
        }
    }

    /**
     * Creates backend configuration and configure using configurers provided by {@code router} interface annotations.
     *
     * @param backend Backend.
     * @param router  Router interface with configurer annotations.
     * @param <C>     Configuration type.
     * @return Backend configuration.
     */
    @SuppressWarnings("unchecked")
    public static <C extends InterouteBackendConfiguration> C createConfiguration(InterouteBackend<C> backend,
                                                                                  Class<?> router) {
        C configuration = backend.createConfiguration();
        BackendConfigurer backendConfigurer = router.getDeclaredAnnotation(BackendConfigurer.class);

        if (backendConfigurer != null) {
            for (Class<? extends InterouteBackendConfigurer<?>> aClass : backendConfigurer.value()) {
                InterouteBackendConfigurer<C> instance = (InterouteBackendConfigurer<C>) InstanceFactories.create(aClass);
                configuration = instance.configure(configuration);
            }
        }

        return configuration;
    }

    /**
     * Creates route specification.
     *
     * @param method Method with {@link RouteTo} annotation.
     * @return Optional with parsed {@link RouteSpec route specification} or {@link Optional#empty()} if the method is not
     * annotated with {@link RouteTo}.
     */
    public static Optional<RouteSpec> createRouteSpec(Method method) {
        RouteTo routeTo = method.getDeclaredAnnotation(RouteTo.class);

        if (routeTo == null) {
            return Optional.empty();
        } else {
            return Optional.of(new RouteSpec(
                    ConversionsKt.toMethodDeclaration(method),
                    routeTo.value(),
                    InteroureAnnotationParse.createSpecInfoList(method)));
        }
    }

    /**
     * Creates {@link RouteSpecInfo} from {@code annotatedElement}.
     *
     * @param annotatedElement Element with spec annotations.
     * @return List of parsed {@link RouteSpecInfo}.
     */
    private static List<RouteSpecInfo<?>> createSpecInfoList(AnnotatedElement annotatedElement) {
        return Arrays.stream(annotatedElement.getDeclaredAnnotations())
                .map(it -> InteroureAnnotationParse.createSpecInfo(it.annotationType(), it))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Creates spec info from spec annotation.
     *
     * @param annotationType Spec annotation type.
     * @param annotation     Spec annotation instance.
     * @return Optional of {@link RouteSpecInfo} if this annotation is annotated with {@link RouteInfo}, otherwise empty optional.
     */
    private static Optional<RouteSpecInfo<?>> createSpecInfo(Class<? extends Annotation> annotationType,
                                                             Annotation annotation) {
        return Optional.ofNullable(annotationType.getDeclaredAnnotation(RouteInfo.class))
                .map(routeInfo -> InteroureAnnotationParse.createSpecInfo(annotationType, annotation, routeInfo));
    }

    /**
     * Creates spec info instance.
     *
     * @param annotationType Spec annotation type.
     * @param annotation     Spec annotation instance.
     * @param routeInfo      Route info of spec annotation.
     * @return Spec info instance.
     */
    @SuppressWarnings("unchecked")
    private static RouteSpecInfo<?> createSpecInfo(Class<? extends Annotation> annotationType,
                                                   Annotation annotation,
                                                   RouteInfo routeInfo) {
        return new RouteSpecInfo<>(
                annotationType,
                (Class<? extends AnnotationUnifier<Object>>) routeInfo.value(),
                InstanceFactories.create(routeInfo.value()).unify(annotation)
        );
    }

    /**
     * Validate {@code router} interface.
     *
     * @param router Router interface to validate.
     * @param <I>    Router type.
     * @return Result of validation.
     */
    public static <I> Result<Class<I>, RouterCreationException> validate(Class<I> router) {
        if (router.isInterface()) {
            return Result.ok(router);
        } else {
            return Result.error(new RouterCreationException("Input 'router' class is not an interface!"));
        }
    }
}
