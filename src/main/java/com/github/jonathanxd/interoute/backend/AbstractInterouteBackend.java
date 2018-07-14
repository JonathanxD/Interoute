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

import com.github.jonathanxd.interoute.backend.InterouteBackend;
import com.github.jonathanxd.interoute.backend.InterouteBackendConfiguration;
import com.github.jonathanxd.interoute.exception.GenerationException;
import com.github.jonathanxd.interoute.gen.ClassGenerationUtil;
import com.github.jonathanxd.interoute.gen.GenerationUtil;
import com.github.jonathanxd.interoute.gen.RouteSpec;
import com.github.jonathanxd.interoute.gen.RouterSpec;
import com.github.jonathanxd.iutils.collection.Collections3;
import com.github.jonathanxd.iutils.object.result.Result;
import com.github.jonathanxd.kores.Instruction;
import com.github.jonathanxd.kores.Instructions;
import com.github.jonathanxd.kores.base.ClassDeclaration;
import com.github.jonathanxd.kores.base.MethodDeclaration;
import com.github.jonathanxd.kores.common.VariableRef;
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.kores.type.ImplicitKoresType;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractInterouteBackend<C extends InterouteBackendConfiguration> implements InterouteBackend<C> {

    @NotNull
    @Override
    public <T> Result<? extends T, GenerationException> generate(@NotNull RouterSpec<C> routerSpec) {
        C configuration = routerSpec.getConfiguration();
        ClassDeclaration.Builder classDeclaration = GenerationUtil.generateStandardRouterClass(routerSpec.getRouterInterface())
                .specifiedName("com.github.jonathanxd.interoute.backend.live."
                        + ImplicitKoresType.getSimpleName(routerSpec.getRouterInterface())
                        + "$Router");

        List<Result<MethodDeclaration, GenerationException>> results =
                this.getRouteImplementations(routerSpec.getRouteSpecList(), configuration);

        List<GenerationException> generationErrors = this.getErrorList(results);

        if (!generationErrors.isEmpty()) {
            return Result.error(GenerationException.fromExceptionList(generationErrors));
        } else {
            List<MethodDeclaration> methods = this.getMethodList(results);
            classDeclaration = classDeclaration.methods(Collections3.concat(classDeclaration.getMethods(), methods));

            Class<? extends T> generatedClass = ClassGenerationUtil
                    .load(ClassGenerationUtil.generate(classDeclaration.build()), null);

            return ClassGenerationUtil.create(generatedClass, this, configuration)
                    .mapError(GenerationException::new);
        }
    }

    private List<GenerationException> getErrorList(List<Result<MethodDeclaration, GenerationException>> results) {
        return results.stream()
                .filter(r -> r instanceof Result.Err<?, ?>)
                .map(r -> (Result.Err<MethodDeclaration, GenerationException>) r)
                .map(r -> r.error())
                .collect(Collectors.toList());
    }

    private List<MethodDeclaration> getMethodList(List<Result<MethodDeclaration, GenerationException>> results) {
        return results.stream()
                .filter(r -> r instanceof Result.Ok<?, ?>)
                .map(r -> (Result.Ok<MethodDeclaration, GenerationException>) r)
                .map(Result.Ok::success)
                .collect(Collectors.toList());
    }

    private List<Result<MethodDeclaration, GenerationException>> getRouteImplementations(List<RouteSpec> routeSpecList,
                                                                                         C configuration) {
        return routeSpecList.stream()
                .map(route -> this.getRouteImplementationMethod(route, configuration))
                .collect(Collectors.toList());
    }

    private Result<MethodDeclaration, GenerationException> getRouteImplementationMethod(RouteSpec routeSpec,
                                                                                        C configuration) {
        return this.getRouteImplementation(routeSpec, configuration)
                .map(instructions ->
                        MethodDeclaration.Builder.builder()
                                .annotations(Factories.overrideAnnotation())
                                .publicModifier()
                                .returnType(GenerationUtil.getRouteOriginReturnType(routeSpec))
                                .name(GenerationUtil.getRouteOriginName(routeSpec))
                                .parameters(GenerationUtil.getRouteOriginParameters(routeSpec))
                                .body(instructions)
                                .build()
                );
    }

    private Result<Instructions, GenerationException> getRouteImplementation(RouteSpec routeSpec,
                                                                             C configuration) {
        return this.route(routeSpec, configuration).map(Instructions::fromPart);
    }

    protected abstract Result<Instruction, GenerationException> route(RouteSpec routeSpec, C configuration);

}
