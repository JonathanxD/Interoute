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
package com.github.jonathanxd.interoute.backend;

import com.github.jonathanxd.interoute.backend.def.DefaultBackendGenerationUtil;
import com.github.jonathanxd.interoute.exception.GenerationException;
import com.github.jonathanxd.interoute.gen.GenerationUtil;
import com.github.jonathanxd.interoute.gen.RouteGenerationUtil;
import com.github.jonathanxd.interoute.gen.RouteSpec;
import com.github.jonathanxd.interoute.route.Route;
import com.github.jonathanxd.interoute.route.def.DestinationResolver;
import com.github.jonathanxd.interoute.route.def.SimpleTypeMethodDestinationNotation;
import com.github.jonathanxd.iutils.function.combiner.Combiners;
import com.github.jonathanxd.iutils.object.result.Result;
import com.github.jonathanxd.kores.Instruction;
import com.github.jonathanxd.kores.Types;
import com.github.jonathanxd.kores.common.MethodTypeSpec;
import com.github.jonathanxd.kores.common.VariableRef;
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.kores.type.ImplicitKoresType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractInvocationRouteBackend<C extends InterouteBackendConfiguration> extends AbstractInterouteBackend<C> {
    @Override
    protected Result<Instruction, GenerationException> route(RouteSpec routeSpec, C configuration) {
        return SimpleTypeMethodDestinationNotation
                .parseType(routeSpec.getDestination(), this.destinationResolver(routeSpec, configuration))
                .flatMap(
                        notation -> this.routeTargetInstruction(routeSpec, notation),
                        destinationParseError -> Result.error(new GenerationException(destinationParseError))
                );
    }

    protected abstract DestinationResolver<Type, MethodTypeSpec> destinationResolver(@NotNull RouteSpec routeSpec,
                                                                                     @NotNull C configuration);

    private Result<Instruction, GenerationException> routeTargetInstruction(RouteSpec routeSpec,
                                                                            SimpleTypeMethodDestinationNotation destinationNotation) {
        return destinationNotation.getRoot()
                .<Result<Type, GenerationException>>map(Result::ok)
                .orElseGet(() -> Result
                        .error(new GenerationException(String.format("Missing root type in route spec: %s.", routeSpec))))
                .combineSuccess(Result.ok(destinationNotation.getTarget()), Combiners.pair())
                .map((typeAndMethod) -> {
                    Type type = typeAndMethod.getFirst();
                    MethodTypeSpec spec = typeAndMethod.getSecond();

                    String rootString = destinationNotation.getRootString().orElseThrow(IllegalStateException::new);

                    return this.generateRouteInstanceWithInvocation(rootString, type, spec, routeSpec);
                });

    }

    @NotNull
    protected abstract Instruction getInstance(@Nullable String root);

    @NotNull
    protected abstract Instruction createInvocation(@NotNull MethodTypeSpec targetSpec,
                                                    @Nullable Type rootType,
                                                    @NotNull Instruction getInstanceInstruction,
                                                    @NotNull RouteSpec routeSpec);

    private Instruction generateRouteInstanceWithInvocation(@Nullable String rootString,
                                                            @Nullable Type rootType,
                                                            @NotNull MethodTypeSpec targetSpec,
                                                            @NotNull RouteSpec routeSpec) {
        Instruction getInstanceInstruction = this.getInstance(rootString);

        Instruction routeInvocation = this.createInvocation(targetSpec, rootType, getInstanceInstruction, routeSpec);

        Instruction routeCreationInstruction = this.generateRouteCreationInstruction(
                routeInvocation,
                DefaultBackendGenerationUtil.createDestination(getInstanceInstruction, targetSpec),
                routeSpec
        );

        if (ImplicitKoresType.is(GenerationUtil.getRouteOriginReturnType(routeSpec), Types.VOID)) {
            return GenerationUtil.executeRouteAndWait(routeCreationInstruction);
        } else {
            return Factories.returnValue(Route.class, routeCreationInstruction);
        }
    }

    private Instruction generateRouteCreationInstruction(Instruction routeTargetInvocation,
                                                         Instruction destination,
                                                         RouteSpec routeSpec) {
        return RouteGenerationUtil.createSuppliedExecutorRoute(
                this.getVariables(routeSpec),
                routeTargetInvocation,
                destination,
                routeSpec
        );
    }

    private List<VariableRef> getVariables(RouteSpec routeSpec) {
        return GenerationUtil.getRouteOriginParameters(routeSpec).stream()
                .map(p -> new VariableRef(p.getType(), p.getName()))
                .collect(Collectors.toList());
    }
}
