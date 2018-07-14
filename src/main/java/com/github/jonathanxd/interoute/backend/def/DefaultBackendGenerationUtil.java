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

import com.github.jonathanxd.interoute.backend.InterouteBackendConfiguration;
import com.github.jonathanxd.interoute.gen.GenerationUtil;
import com.github.jonathanxd.interoute.route.Router;
import com.github.jonathanxd.iutils.collection.Collections3;
import com.github.jonathanxd.kores.Instruction;
import com.github.jonathanxd.kores.base.InvokeType;
import com.github.jonathanxd.kores.common.MethodTypeSpec;
import com.github.jonathanxd.kores.factory.Factories;
import com.github.jonathanxd.kores.factory.InvocationFactory;

import java.util.Collections;
import java.util.Optional;

/**
 * Utility of default backend generation.
 */
public class DefaultBackendGenerationUtil {
    /**
     * Invokes {@link DefaultBackendConfiguration#getInstance(String)}.
     *
     * @param instanceName Instruction of instance name.
     * @return Empty optional or optional with the instance.
     */
    public static Instruction getInstance(Instruction instanceName) {
        return GenerationUtil.invokeOptionalGet(InvocationFactory.invoke(
                InvokeType.INVOKE_VIRTUAL,
                DefaultBackendConfiguration.class,
                DefaultBackendGenerationUtil.invokeGetterAsDefaultBackendConfiguration(),
                "getInstance",
                Factories.typeSpec(Optional.class, String.class),
                Collections.singletonList(instanceName)
        ));
    }

    /**
     * Invokes {@link Router#getBackend()} and cast to {@link DefaultBackendConfiguration}.
     *
     * @return Invocation of {@link Router#getBackend()} casted to {@link DefaultBackendConfiguration}.
     */
    public static Instruction invokeGetterAsDefaultBackendConfiguration() {
        return Factories.cast(
                InterouteBackendConfiguration.class,
                DefaultBackendConfiguration.class,
                GenerationUtil
                        .invokeOptionalGet(GenerationUtil.invokeOptionalGetter(GenerationUtil.backendConfigurationProperty()))
        );
    }

    /**
     * Creates instruction that constructs {@link DefaultBackendDestination}.
     *
     * @param destinationInstance Root destination instance.
     * @param destinationMethod   Destination method.
     * @return Instruction that constructs {@link DefaultBackendDestination}.
     */
    public static Instruction createDestination(Instruction destinationInstance,
                                                MethodTypeSpec destinationMethod) {
        return InvocationFactory.invokeConstructor(
                DefaultBackendDestination.class,
                Factories.constructorTypeSpec(Object.class, MethodTypeSpec.class),
                Collections3.listOf(destinationInstance, GenerationUtil.createMethodTypeSpec(destinationMethod))
        );
    }
}
