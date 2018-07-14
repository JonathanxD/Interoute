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

import com.github.jonathanxd.interoute.annotation.RequiresConfiguration;
import com.github.jonathanxd.interoute.backend.AbstractInvocationRouteBackend;
import com.github.jonathanxd.interoute.exception.DestinationParseException;
import com.github.jonathanxd.interoute.gen.GenerationUtil;
import com.github.jonathanxd.interoute.gen.RouteSpec;
import com.github.jonathanxd.interoute.route.def.DestinationResolver;
import com.github.jonathanxd.iutils.object.EitherUtil;
import com.github.jonathanxd.iutils.object.result.Result;
import com.github.jonathanxd.kores.Instruction;
import com.github.jonathanxd.kores.base.InvokeType;
import com.github.jonathanxd.kores.base.MethodDeclaration;
import com.github.jonathanxd.kores.common.MethodTypeSpec;
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.kores.literal.Literals;
import com.github.jonathanxd.kores.type.Generic;
import com.github.jonathanxd.kores.type.ImplicitKoresType;
import com.github.jonathanxd.kores.util.conversion.ConversionsKt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Default backend. This backend generates Routes which destination are always methods of other instances (just like dependency
 * injection).
 *
 * Routing rules:
 *
 * - The {@code Origin} must match {@code Destination} parameters, unless {@code Destination} has a single {@link Object[]}
 * parameter.
 *
 * - The {@code Origin} result type must either match {@code Destination} return type or be void.
 */
@RequiresConfiguration(DefaultBackendConfiguration.class)
public class DefaultInterouteBackend extends AbstractInvocationRouteBackend<DefaultBackendConfiguration> {

    public static final DefaultInterouteBackend INSTANCE = new DefaultInterouteBackend();

    public static DefaultInterouteBackend create() {
        return INSTANCE;
    }

    @NotNull
    @Override
    public DefaultBackendConfiguration createConfiguration() {
        return DefaultBackendConfiguration.newConfiguration();
    }


    @Override
    protected DestinationResolver<Type, MethodTypeSpec> destinationResolver(@NotNull RouteSpec routeSpec,
                                                                            @NotNull DefaultBackendConfiguration configuration) {
        return new ConfigurationBasedResolver(routeSpec, configuration);
    }

    @NotNull
    @Override
    protected Instruction getInstance(@Nullable String root) {
        Objects.requireNonNull(root);
        return DefaultBackendGenerationUtil.getInstance(Literals.STRING(root));
    }

    @NotNull
    @Override
    protected Instruction createInvocation(@NotNull MethodTypeSpec targetSpec,
                                           @Nullable Type rootType,
                                           @NotNull Instruction getInstanceInstruction,
                                           @NotNull RouteSpec routeSpec) {
        Objects.requireNonNull(rootType);

        return targetSpec.invoke(
                InvokeType.get(rootType),
                Factories.cast(Generic.type("T"), rootType,
                        getInstanceInstruction),
                ConversionsKt.getAccess(GenerationUtil.getRouteOriginParameters(routeSpec))
        );
    }

    static class ConfigurationBasedResolver implements DestinationResolver<Type, MethodTypeSpec> {

        private final RouteSpec routeSpec;
        private final DefaultBackendConfiguration configuration;

        ConfigurationBasedResolver(RouteSpec routeSpec,
                                   DefaultBackendConfiguration configuration) {
            this.routeSpec = routeSpec;
            this.configuration = configuration;
        }

        @NotNull
        @Override
        public RootResolver<Type> rootResolver() {
            return notation -> {
                Optional<Object> instance = configuration.getInstance(notation);

                return instance.<Result<Type, DestinationParseException>>map(o -> Result.ok(o.getClass()))
                        .orElseGet(() -> Result.error(this.rootTypeResolutionFailException(notation)));
            };
        }

        private DestinationParseException rootTypeResolutionFailException(String notation) {
            return new DestinationParseException(String.format("Could not resolve root type from specified notation: '%s'." +
                            " Either invalid notation or the type is not registered on the backend configuration.",
                    notation
            ));
        }

        @NotNull
        @Override
        public TargetResolver<Type, MethodTypeSpec> targetResolver() {
            return (notation, root) -> EitherUtil.getLeftOrRight(ImplicitKoresType.getBindedDefaultResolver(root)
                    .resolveMethods()
                    .map(
                            e -> Result.<MethodTypeSpec, DestinationParseException>error(
                                    this.targetResolutionFailException(notation, root)),
                            declarations -> this.filterMatch(root, declarations, notation)
                    ));
        }

        private DestinationParseException targetResolutionFailException(String notation, Type type) {
            return new DestinationParseException(
                    String.format("Could not resolve target method from specified notation: '%s' inside" +
                                    " specified type '%s'." +
                                    " Either invalid notation or the method is not present in the type.",
                            notation, type == null ? null : ImplicitKoresType.getCanonicalName(type)
                    ));
        }

        private Result<MethodTypeSpec, DestinationParseException> filterMatch(Type root,
                                                                              List<MethodDeclaration> methods,
                                                                              String name) {
            return methods.stream()
                    .filter(methodDeclaration -> Objects.equals(name, methodDeclaration.getName())
                            && this.parametersMatches(methodDeclaration, this.routeSpec))
                    .findFirst()
                    .map(decl -> new MethodTypeSpec(root, decl.getName(), decl.getTypeSpec()))
                    .<Result<MethodTypeSpec, DestinationParseException>>map(Result::ok)
                    .orElseGet(() -> Result.error(this.targetResolutionFailException(name, root)));
        }

        private boolean parametersMatches(MethodDeclaration methodDeclaration, RouteSpec routeSpec) {
            return Objects.equals(
                    methodDeclaration.getTypeSpec().getParameterTypes(),
                    this.routeSpec.getOrigin().getTypeSpec().getParameterTypes()
            );
        }
    }
}
