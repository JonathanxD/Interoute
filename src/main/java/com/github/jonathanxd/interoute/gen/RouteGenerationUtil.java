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

import com.github.jonathanxd.interoute.exception.RoutingException;
import com.github.jonathanxd.interoute.route.Destination;
import com.github.jonathanxd.interoute.route.Origin;
import com.github.jonathanxd.interoute.route.SuppliedExecutorRoute;
import com.github.jonathanxd.iutils.collection.Collections3;
import com.github.jonathanxd.iutils.object.result.Result;
import com.github.jonathanxd.kores.Instruction;
import com.github.jonathanxd.kores.Instructions;
import com.github.jonathanxd.kores.KoresPartKt;
import com.github.jonathanxd.kores.MutableInstructions;
import com.github.jonathanxd.kores.Types;
import com.github.jonathanxd.kores.base.Alias;
import com.github.jonathanxd.kores.base.CatchStatement;
import com.github.jonathanxd.kores.base.InvokeType;
import com.github.jonathanxd.kores.base.KoresModifier;
import com.github.jonathanxd.kores.base.LocalCode;
import com.github.jonathanxd.kores.base.MethodDeclaration;
import com.github.jonathanxd.kores.base.TryStatement;
import com.github.jonathanxd.kores.common.MethodTypeSpec;
import com.github.jonathanxd.kores.common.VariableRef;
import com.github.jonathanxd.kores.factory.DynamicInvocationFactory;
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.kores.factory.InvocationFactory;
import com.github.jonathanxd.kores.factory.VariableFactory;
import com.github.jonathanxd.kores.type.Generic;
import com.github.jonathanxd.kores.type.ImplicitKoresType;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import kotlin.Unit;

public final class RouteGenerationUtil {

    private static long ROUTE_NAME = Long.MIN_VALUE;

    /**
     * Creates the {@link SuppliedExecutorRoute} which will be returned by the routing method.
     *
     * @param variables   Variables to be used in the supplier.
     * @param invocation  Route invocation instruction.
     * @param destination {@link Destination} creation or retrieval instruction.
     * @param routeSpec   Route specification.
     * @return {@link SuppliedExecutorRoute} which will be returned by the routing method.
     */
    public static Instruction createSuppliedExecutorRoute(List<VariableRef> variables,
                                                          Instruction invocation,
                                                          Instruction destination,
                                                          RouteSpec routeSpec) {
        return InvocationFactory.invokeConstructor(
                SuppliedExecutorRoute.class,
                Factories.constructorTypeSpec(Origin.class, Destination.class, Supplier.class),
                Collections3.listOf(
                        GenerationUtil.createMethodTypeSpecOrigin(routeSpec),
                        destination,
                        RouteGenerationUtil.generateSupplier(variables, invocation)
                )
        );
    }

    /**
     * Creates the {@link Supplier} which creates the future that evaluates to {@link Result} of route {@code invocation}
     * execution.
     *
     * @param variables  Variables which lambda should have access to.
     * @param invocation Route invocation instruction.
     * @return {@link Supplier} which creates the future that evaluates to {@link Result} of route {@code invocation} execution.
     */
    public static Instruction generateSupplier(List<VariableRef> variables, Instruction invocation) {
        return RouteGenerationUtil.generateSupplierWithVariableAccess(
                CompletableFuture.class,
                RouteGenerationUtil.completableFutureSupplier(variables, invocation),
                variables
        );
    }

    public static LocalCode completableFutureSupplier(List<VariableRef> variables, Instruction invocation) {
        return RouteGenerationUtil.localCode(RouteGenerationUtil.completableFutureSupplierDeclaration(variables, invocation));
    }

    /**
     * Creates the {@link MethodDeclaration method declaration} of the body of completable future supplier, example: <br/>
     * <pre>
     *     {@code
     *     public CompletableFuture<Result<T, RoutingException>> $completableFutureFactory([variables]) {
     *      ...
     *     }
     *     }
     * </pre>
     *
     * @param variables  Variables which lambda should have access to.
     * @param invocation Route invocation instruction.
     * @return {@link MethodDeclaration method declaration} of the body of completable future supplier.
     */
    public static MethodDeclaration completableFutureSupplierDeclaration(List<VariableRef> variables, Instruction invocation) {
        return RouteGenerationUtil.routePartSupplierDeclaration(
                CompletableFuture.class,
                "$completableFutureFactory",
                variables,
                Instructions.fromPart(Factories.returnValue(CompletableFuture.class,
                        RouteGenerationUtil.completableFutureSupplyAsyncResult(variables, invocation)))
        );
    }

    /**
     * Creates the instruction which constructs the future which executes the route {@code invocation} instruction, example:
     * <br/>
     * <pre>
     *     {@code
     *     CompletableFuture.supplyAsync(...)
     *     }
     * </pre>
     *
     * @param variables  Variables which lambda should have access to.
     * @param invocation Route invocation instruction.
     * @return Instruction which constructs the future which executes the route {@code invocation} instruction.
     */
    public static Instruction completableFutureSupplyAsyncResult(List<VariableRef> variables, Instruction invocation) {
        return InvocationFactory.invokeStatic(CompletableFuture.class,
                "supplyAsync",
                Factories.typeSpec(CompletableFuture.class, Supplier.class),
                Collections.singletonList(RouteGenerationUtil.asyncSupplyLambda(variables, invocation))
        );
    }

    /**
     * Creates lambda of {@link CompletableFuture#supplyAsync(Supplier)}, example: <br/>
     * <pre>
     *     {@code
     *     () -> ...
     *     }
     * </pre>
     *
     * @param variables  Variables which lambda should have access to.
     * @param invocation Route invocation instruction.
     * @return lambda of {@link CompletableFuture#supplyAsync(Supplier)}.
     */
    public static Instruction asyncSupplyLambda(List<VariableRef> variables, Instruction invocation) {
        return RouteGenerationUtil.generateSupplierWithVariableAccess(
                Result.class,
                RouteGenerationUtil.resultSupplier(variables, invocation),
                variables
        );
    }

    /**
     * Creates {@link LocalCode local code} of {@link com.github.jonathanxd.interoute.route.Route route} {@link Result result}
     * supplier.
     *
     * @param variables  Variable which the method should have access to.
     * @param invocation Route invocation instruction.
     * @return {@link LocalCode local code} of {@link com.github.jonathanxd.interoute.route.Route route} {@link Result result}
     * supplier.
     */
    public static LocalCode resultSupplier(List<VariableRef> variables, Instruction invocation) {
        return RouteGenerationUtil.localCode(RouteGenerationUtil.resultSupplierDeclaration(variables, invocation));
    }

    /**
     * Creates {@link com.github.jonathanxd.interoute.route.Route route} {@link Result result} supplier method declaration,
     * example: <br/>
     * <pre>
     *     {@code
     *     public Result<T, RoutingException> $resultHandler([variables]) {
     *      ...
     *     }
     *     }
     * </pre>
     *
     * @param variables  Variable which the method should have access to.
     * @param invocation Route invocation instruction.
     * @return Declaration of route result supplier method.
     */
    public static MethodDeclaration resultSupplierDeclaration(List<VariableRef> variables, Instruction invocation) {
        return RouteGenerationUtil.routePartSupplierDeclaration(
                Result.class,
                "$resultHandler",
                variables,
                Instructions.fromPart(RouteGenerationUtil.resultFactoryExpression(invocation))
        );
    }

    /**
     * Handle {@code invocation} exceptions and create {@link com.github.jonathanxd.interoute.route.Route route} {@link Result
     * result}: <br/>
     * <pre>
     *     {@code
     *     try {
     *         Result.ok([invocation]);
     *     } catch(Throwable t) {
     *         Result.error(new RoutingException(t));
     *     }
     *     }
     * </pre>
     */
    public static Instruction resultFactoryExpression(Instruction invocation) {
        VariableRef exceptionVariable = new VariableRef(Throwable.class, "exception");
        return TryStatement.Builder.builder()
                .body(RouteGenerationUtil.execute(invocation))
                .catchStatements(CatchStatement.Builder.builder()
                        .exceptionTypes(Throwable.class)
                        .variable(VariableFactory.variable(exceptionVariable.getType(), exceptionVariable.getName()))
                        .body(Instructions.fromPart(
                                Factories.returnValue(Result.class,
                                        RouteGenerationUtil.resultRoutingException(Factories.accessVariable(exceptionVariable)))
                        ))
                        .build()
                ).build();
    }

    /**
     * Creates execution of {@code instruction}.
     */
    private static Instructions execute(Instruction instruction) {
        Type typeOrNull = KoresPartKt.getTypeOrNull(instruction);

        if (typeOrNull != null && ImplicitKoresType.is(typeOrNull, Types.VOID)) {
            return Instructions.fromVarArgs(
                    instruction,
                    Factories.returnValue(Result.class,
                            RouteGenerationUtil.resultOk(Factories.accessStaticField(Unit.class, Unit.class, "INSTANCE")))

            );
        } else {
            return Instructions.fromPart(Factories.returnValue(Result.class, RouteGenerationUtil.resultOk(instruction)));
        }
    }

    /**
     * Creates invocation to {@link Result#ok(Object)}, example: <br/>
     * <pre>
     *     {@code
     *     Result.ok([instruction]);
     *     }
     * </pre>
     */
    private static Instruction resultOk(Instruction instruction) {
        return InvocationFactory.invokeStatic(
                Result.class,
                "ok",
                Factories.typeSpec(Result.Ok.class, Generic.type("R")),
                Collections.singletonList(instruction)
        );
    }

    /**
     * Creates invocation to {@link Result#error(Object)}, example: <br/>
     * <pre>
     *     {@code
     *     Result.error([instruction]);
     *     }
     * </pre>
     */
    private static Instruction resultError(Instruction instruction) {
        return InvocationFactory.invokeStatic(
                Result.class,
                "error",
                Factories.typeSpec(Result.Err.class, Generic.type("E")),
                Collections.singletonList(
                        instruction
                )
        );
    }

    /**
     * Creates invocation to {@link RoutingException#RoutingException(Throwable)} and then to {@link Result#error(Object)},
     * example: <br/>
     * <pre>
     *     {@code
     *     new RoutingException(Result.error([instruction]));
     *     }
     * </pre>
     */
    private static Instruction resultRoutingException(Instruction instruction) {
        return RouteGenerationUtil.resultError(RouteGenerationUtil.createRoutingException(instruction));
    }

    /**
     * Creates invocation to {@link RoutingException#RoutingException(Throwable)} , example: <br/>
     * <pre>
     *     {@code
     *     new RoutingException([exception]);
     *     }
     * </pre>
     */
    private static Instruction createRoutingException(Instruction exception) {
        return InvocationFactory.invokeConstructor(
                RoutingException.class,
                Factories.constructorTypeSpec(Throwable.class),
                Collections.singletonList(exception)
        );
    }

    /**
     * Generates a {@link Supplier} that returns {@code expectedReturnType} with access to {@code variables} with {@code
     * supplierBodyMethod} as body, example: <br/>
     * <pre>
     *     {@code
     *     () -> a + b;
     *     }
     * </pre>
     *
     * @param expectedReturnType Expected return type of {@link Supplier supplier}.
     * @param supplierBodyMethod Body of the {@link Supplier supplier}.
     * @param variables          Variables which {@link Supplier supplier} should have access to.
     * @return {@link Supplier} that returns {@code expectedReturnType} with access to {@code variables} with {@code
     * supplierBodyMethod} as body.
     */
    private static Instruction generateSupplierWithVariableAccess(Type expectedReturnType, LocalCode supplierBodyMethod,
                                                                  List<VariableRef> variables) {
        return RouteGenerationUtil
                .generateSupplier(expectedReturnType, supplierBodyMethod, RouteGenerationUtil.access(variables));
    }

    /**
     * Generates a dynamic invocation of a {@link Supplier} that returns {@code expectedReturnType}, receives {@code arguments} in
     * invocation, and with {@code supplierBodyMethod} as body, example: <br/>
     * <pre>
     *     {@code
     *     () -> a + b;
     *     }
     * </pre>
     *
     * @param expectedReturnType Expected return type of {@link Supplier supplier}.
     * @param supplierBodyMethod Body of the {@link Supplier supplier}.
     * @param arguments          Arguments to provide to invocation of the {@link Supplier supplier}.
     * @return {@link Supplier} that returns {@code expectedReturnType} and receives {@code arguments} as parameters and with
     * {@code supplierBodyMethod} as body.
     */
    private static Instruction generateSupplier(Type expectedReturnType, LocalCode supplierBodyMethod,
                                                List<Instruction> arguments) {
        return DynamicInvocationFactory.invokeDynamicLambdaCode(
                new MethodTypeSpec(Supplier.class, "get", Factories.typeSpec(Generic.type("T"))),
                Factories.typeSpec(expectedReturnType),
                supplierBodyMethod,
                arguments
        );
    }

    /**
     * Creates access to {@link VariableRef variableRefs}.
     *
     * @param variables Variables.
     * @return Access to {@code variables}.
     */
    private static List<Instruction> access(List<VariableRef> variables) {
        return variables.stream().map(VariableRef::access).collect(Collectors.toList());
    }

    /**
     * Creates a {@link LocalCode} declared in current type, invoked virtually, and with {@code methodDeclaration} as the method
     * to execute.
     *
     * @param methodDeclaration Declaration of {@link LocalCode local code} method to execute.
     * @return {@link LocalCode} declared in current type, invoked virtually, and with {@code methodDeclaration} as the method to
     * execute.
     */
    public static LocalCode localCode(MethodDeclaration methodDeclaration) {
        return LocalCode.Builder.builder()
                .declaringType(Alias.THIS.INSTANCE)
                .invokeType(InvokeType.INVOKE_VIRTUAL)
                .declaration(methodDeclaration)
                .build();
    }

    /**
     * Creates a {@link MethodDeclaration method declaration} which is intended to be used in local code declaration.
     *
     * @param returnType Return type of method.
     * @param name       Name of method.
     * @param variables  Variables which method would have access to.
     * @param body       Body of the method.
     * @return {@link MethodDeclaration method declaration} which is intended to be used in local code declaration.
     */
    public static MethodDeclaration routePartSupplierDeclaration(Type returnType,
                                                                 String name,
                                                                 List<VariableRef> variables,
                                                                 Instructions body) {
        return MethodDeclaration.Builder.builder()
                .modifiers(RouteGenerationUtil.modifiers())
                .returnType(returnType)
                .name(name + (ROUTE_NAME++))
                .parameters(GenerationUtil.parameters(variables))
                .body(body)
                .build();
    }

    /**
     * Returns {@code final} and {@code synthetic} modifiers.
     *
     * @return {@code final} and {@code synthetic} modifiers.
     */
    private static Set<KoresModifier> modifiers() {
        return Collections3.setOf(KoresModifier.FINAL, KoresModifier.SYNTHETIC);
    }

}
