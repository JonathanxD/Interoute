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
import com.github.jonathanxd.interoute.route.MethodTypeSpecOrigin;
import com.github.jonathanxd.interoute.route.Route;
import com.github.jonathanxd.interoute.route.Router;
import com.github.jonathanxd.iutils.collection.Collections3;
import com.github.jonathanxd.kores.Instruction;
import com.github.jonathanxd.kores.Instructions;
import com.github.jonathanxd.kores.Types;
import com.github.jonathanxd.kores.base.Access;
import com.github.jonathanxd.kores.base.Alias;
import com.github.jonathanxd.kores.base.ClassDeclaration;
import com.github.jonathanxd.kores.base.ConstructorDeclaration;
import com.github.jonathanxd.kores.base.FieldAccess;
import com.github.jonathanxd.kores.base.FieldDeclaration;
import com.github.jonathanxd.kores.base.FieldDefinition;
import com.github.jonathanxd.kores.base.InvokeType;
import com.github.jonathanxd.kores.base.KoresModifier;
import com.github.jonathanxd.kores.base.KoresParameter;
import com.github.jonathanxd.kores.base.MethodDeclaration;
import com.github.jonathanxd.kores.base.MethodInvocation;
import com.github.jonathanxd.kores.base.TypeSpec;
import com.github.jonathanxd.kores.common.Commons;
import com.github.jonathanxd.kores.common.MethodTypeSpec;
import com.github.jonathanxd.kores.common.VariableRef;
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.kores.factory.InvocationFactory;
import com.github.jonathanxd.kores.literal.Literals;
import com.github.jonathanxd.kores.type.Generic;
import com.github.jonathanxd.kores.type.ImplicitKoresType;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import kotlin.collections.CollectionsKt;
import kotlin.text.StringsKt;

public final class GenerationUtil {

    /**
     * Creates a standard common router class, model:
     * <pre>
     *     {@code
     *     public class StandardRouter implementes Router, [routerInterface] {
     *         private final InterouteBackend backend;
     *         private final InterouteBackendConfiguration backendConfiguration;
     *
     *         public StandardRouter(InterouteBackend backend, InterouteBackendConfiguration backendConfiguration) {
     *             this.backend = backend;
     *             this.backendConfiguration = backendConfiguration;
     *         }
     *
     *         public InterouteBackend getBackend() {
     *             return this.backend;
     *         }
     *
     *         public InterouteBackendConfiguration getBackendConfiguration() {
     *             return this.backendConfiguration;
     *         }
     *     }
     *     }
     * </pre>
     */
    public static ClassDeclaration.Builder generateStandardRouterClass(Type routerInterface) {
        return ClassDeclaration.Builder.builder()
                .modifiers(KoresModifier.PUBLIC, KoresModifier.SYNTHETIC)
                .implementations(routerInterface, Router.class)
                .fields(GenerationUtil.fields())
                .constructors(GenerationUtil.constructor())
                .methods(GenerationUtil.methods());
    }

    /**
     * Gets the name of {@code routeSpec} origin method.
     *
     * @param routeSpec Route specification.
     * @return Name of {@code routeSpec} origin method.
     */
    public static String getRouteOriginName(RouteSpec routeSpec) {
        return routeSpec.getOrigin().getName();
    }

    /**
     * Gets the return type of {@code routeSpec} origin method.
     *
     * @param routeSpec Route specification.
     * @return Return type of {@code routeSpec} origin method.
     */
    public static Type getRouteOriginReturnType(RouteSpec routeSpec) {
        return routeSpec.getOrigin().getReturnType();
    }

    /**
     * Gets the parameters of {@code routeSpec} origin method.
     *
     * @param routeSpec Route specification.
     * @return Parameters of {@code routeSpec} origin method.
     */
    public static List<KoresParameter> getRouteOriginParameters(RouteSpec routeSpec) {
        return routeSpec.getOrigin().getParameters();
    }

    /**
     * Returns whether {@code routeSpec} is eager or not (returns {@code void} or not, void implies eager evaluation).
     *
     * @param routeSpec Route specification.
     * @return Whether {@code routeSpec} is eager or not (returns {@code void} or not, void implies eager evaluation).
     */
    public static boolean isEager(RouteSpec routeSpec) {
        return ImplicitKoresType.is(GenerationUtil.getRouteOriginReturnType(routeSpec), Types.VOID);
    }

    /**
     * Creates an instruction which evaluates {@link CompletableFuture} and wait for result, this could be used in place of a
     * specialized version with eager evaluation.
     *
     * @param route Route {@link CompletableFuture} instruction.
     * @return Instruction which evaluates {@link CompletableFuture} and wait for result.
     */
    public static Instruction executeRouteAndWait(Instruction route) {
        return GenerationUtil.executeFuture(GenerationUtil.executeRoute(route));
    }

    /**
     * Creates an instruction which invokes {@link Route#execute()}.
     *
     * @param route Route instance.
     * @return Instruction which invokes {@link Route#execute()}.
     */
    private static Instruction executeRoute(Instruction route) {
        return InvocationFactory.invokeInterface(Route.class,
                route,
                "execute",
                Factories.typeSpec(CompletableFuture.class),
                Collections.emptyList()
        );
    }

    /**
     * Creates an instruction which invokes {@link Future#get()}.
     *
     * @param future Future instance.
     * @return Instruction which invokes {@link Future#get()}.
     */
    private static Instruction executeFuture(Instruction future) {
        return InvocationFactory.invokeInterface(Future.class,
                future,
                "get",
                Factories.typeSpec(Generic.type("V")),
                Collections.emptyList()
        );
    }

    /**
     * Gets fields of common router class.
     *
     * @return Fields of common router class.
     */
    private static List<FieldDeclaration> fields() {
        return GenerationUtil.mapProperties(GenerationUtil::field);
    }

    /**
     * Gets parameters of constructor of common router class.
     *
     * @return Parameters of constructor of common router class.
     */
    private static List<KoresParameter> parameters() {
        return GenerationUtil.mapProperties(GenerationUtil::parameter);
    }

    /**
     * Creates the constructor of common router class.
     *
     * @return Constructor of common router class.
     */
    private static ConstructorDeclaration constructor() {
        return ConstructorDeclaration.Builder.builder()
                .publicModifier()
                .parameters(GenerationUtil.parameters())
                .body(GenerationUtil.declareFields())
                .build();
    }

    /**
     * Creates the getter methods of common router class.
     *
     * @return Getter methods of common router class.
     */
    private static List<MethodDeclaration> methods() {
        return GenerationUtil.mapProperties(GenerationUtil::getter);
    }

    /**
     * Creates declarations of fields of common router class.
     *
     * @return Declarations of fields of common router class.
     */
    private static Instructions declareFields() {
        return Instructions.fromIterable(GenerationUtil.mapProperties(GenerationUtil::declare));
    }

    /**
     * Creates declaration of field {@code base} value to {@code base} variable value.
     *
     * @param base Base field spec and variable spec.
     * @return Declaration of field {@code base} value to {@code base} variable value.
     */
    private static FieldDefinition declare(VariableRef base) {
        return FieldDefinition.Builder.builder()
                .localization(Alias.THIS.INSTANCE)
                .target(Access.THIS)
                .base(base)
                .value(Factories.accessVariable(base))
                .build();
    }

    /**
     * Creates access to field {@code base}.
     *
     * @param base Base field to access.
     * @return Access to field {@code base}.
     */
    public static FieldAccess access(VariableRef base) {
        return FieldAccess.Builder.builder()
                .localization(Alias.THIS.INSTANCE)
                .target(Access.THIS)
                .type(base.getType())
                .name(base.getName())
                .build();
    }

    /**
     * Creates field declaration based on {@code base}.
     *
     * @param base Field base.
     * @return Field declaration based on {@code base}.
     */
    private static FieldDeclaration field(VariableRef base) {
        return FieldDeclaration.Builder.builder()
                .modifiers(KoresModifier.PRIVATE, KoresModifier.FINAL)
                .base(base)
                .build();
    }

    /**
     * Creates parameters from {@code base} variables.
     *
     * @param base Base variables.
     * @return Parameters from {@code base} variables.
     */
    public static List<KoresParameter> parameters(List<VariableRef> base) {
        return base.stream().map(GenerationUtil::parameter).collect(Collectors.toList());
    }

    /**
     * Creates parameter from {@code base} variable.
     *
     * @param base Base variable.
     * @return Parameter from {@code base} variable.
     */
    public static KoresParameter parameter(VariableRef base) {
        return KoresParameter.Builder.builder()
                .modifiers(KoresModifier.FINAL)
                .type(base.getType())
                .name(base.getName())
                .build();
    }

    /**
     * Map common router class properties.
     *
     * @param mapper Mapper function.
     * @param <R>    Result type.
     * @return Mapped properties.
     */
    private static <R> List<R> mapProperties(Function<VariableRef, R> mapper) {
        return GenerationUtil.getProperties().stream().map(mapper).collect(Collectors.toList());
    }

    /**
     * Gets common router class properties.
     *
     * @return Common router class properties.
     */
    private static List<VariableRef> getProperties() {
        return Collections3.listOf(
                GenerationUtil.backendProperty(),
                GenerationUtil.backendConfigurationProperty()
        );
    }

    /**
     * Gets the backend property.
     *
     * @return Backend property.
     */
    public static VariableRef backendProperty() {
        return new VariableRef(InterouteBackend.class, "backend");
    }

    /**
     * Gets the backend configuration property.
     *
     * @return Backend configuration property.
     */
    public static VariableRef backendConfigurationProperty() {
        return new VariableRef(InterouteBackendConfiguration.class, "backendConfiguration");
    }

    /**
     * Creates declaration of getter of {@code variableRef}
     *
     * @param variableRef Variable to create getter.
     * @return Declaration of getter of {@code variableRef}.
     */
    private static MethodDeclaration getter(VariableRef variableRef) {
        return MethodDeclaration.Builder.builder()
                .annotations(Factories.overrideAnnotation())
                .publicModifier()
                .returnType(Generic.type(Optional.class).of(variableRef.getType()))
                .name("get" + StringsKt.capitalize(variableRef.getName()))
                .body(Instructions.fromPart(
                        Factories.returnValue(Optional.class,
                                GenerationUtil.optionalOf(GenerationUtil.access(variableRef)))
                ))
                .build();
    }

    /**
     * Creates invocation of getter of {@code variableRef}.
     *
     * @param variableRef Variable to create getter invocation.
     * @return Invocation of getter of {@code variableRef}.
     */
    public static MethodInvocation invokeOptionalGetter(VariableRef variableRef) {
        return Factories.invokeFieldGetter(
                InvokeType.INVOKE_VIRTUAL,
                Alias.THIS.INSTANCE,
                Access.THIS,
                Generic.type(Optional.class).of(variableRef.getType()),
                variableRef.getName()
        );
    }

    /**
     * Creates instruction which invokes {@link Optional#of(Object)}.
     *
     * @param value Value to wrap into {@link Optional}.
     * @return Instruction which invokes {@link Optional#of(Object)}.
     */
    public static Instruction optionalOf(Instruction value) {
        return InvocationFactory.invokeStatic(
                Optional.class,
                "of",
                Factories.typeSpec(Optional.class, Generic.type("T")),
                Collections.singletonList(value)
        );
    }

    /**
     * Creates instruction which invokes {@link Optional#get()}.
     *
     * @param optionalInstance {@link Optional optional instance}.
     * @return Instruction which invokes {@link Optional#get()}.
     */
    public static Instruction invokeOptionalGet(Instruction optionalInstance) {
        return InvocationFactory.invokeVirtual(
                Optional.class,
                optionalInstance,
                "get",
                Factories.typeSpec(Generic.type("T")),
                Collections.emptyList()
        );
    }

    /**
     * Creates instruction that constructs {@link TypeSpec} from {@code typeSpec}.
     *
     * @param typeSpec Type specification.
     * @return Instruction that constructs {@link TypeSpec} from {@code typeSpec}.
     */
    public static Instruction createTypeSpec(TypeSpec typeSpec) {
        return InvocationFactory.invokeConstructor(
                TypeSpec.class,
                Factories.constructorTypeSpec(Type.class, List.class),
                Collections3.listOf(Literals.TYPE(typeSpec.getType()),
                        Commons.invokeListOf(
                                typeSpec.getParameterTypes().stream().map(Literals::TYPE).collect(Collectors.toList())))
        );
    }

    /**
     * Creates instruction that constructs {@link MethodTypeSpec} from {@code methodDeclaration}.
     *
     * @param methodDeclaration Method specification.
     * @return Instruction that constructs {@link MethodTypeSpec} from {@code methodDeclaration}.
     */
    public static Instruction createMethodTypeSpec(MethodDeclaration methodDeclaration) {
        return InvocationFactory.invokeConstructor(
                MethodTypeSpec.class,
                Factories.constructorTypeSpec(Type.class, String.class, TypeSpec.class),
                Collections3.listOf(
                        Literals.TYPE(methodDeclaration.getType()),
                        Literals.STRING(methodDeclaration.getName()),
                        createTypeSpec(methodDeclaration.getTypeSpec())
                )
        );
    }

    /**
     * Creates instruction that constructs {@link MethodTypeSpec} from {@code methodTypeSpec}.
     *
     * @param methodTypeSpec Method specification.
     * @return Instruction that constructs {@link MethodTypeSpec} from {@code methodTypeSpec}.
     */
    public static Instruction createMethodTypeSpec(MethodTypeSpec methodTypeSpec) {
        return InvocationFactory.invokeConstructor(
                MethodTypeSpec.class,
                Factories.constructorTypeSpec(Type.class, String.class, TypeSpec.class),
                Collections3.listOf(
                        Literals.TYPE(methodTypeSpec.getType()),
                        Literals.STRING(methodTypeSpec.getMethodName()),
                        createTypeSpec(methodTypeSpec.getTypeSpec())
                )
        );
    }

    /**
     * Creates instruction that constructs {@link MethodTypeSpecOrigin} from {@code routeSpec} origin.
     *
     * @param routeSpec Route spec to retrieve origin to construct.
     * @return Instruction that constructs {@link MethodTypeSpecOrigin} from {@code routeSpec} origin.
     */
    public static Instruction createMethodTypeSpecOrigin(RouteSpec routeSpec) {
        return InvocationFactory.invokeConstructor(
                MethodTypeSpecOrigin.class,
                Factories.constructorTypeSpec(MethodTypeSpec.class),
                Collections.singletonList(
                        createMethodTypeSpec(routeSpec.getOrigin())
                )
        );
    }
}
