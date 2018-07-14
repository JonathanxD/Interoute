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
package com.github.jonathanxd.interoute.route.def;

import com.github.jonathanxd.interoute.exception.DestinationParseException;
import com.github.jonathanxd.iutils.object.result.Result;
import com.github.jonathanxd.kores.common.MethodTypeSpec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

/**
 * {@link SimpleRouteDestinationNotation} which root elements is a {@link Type} and target element is a {@link MethodTypeSpec}.
 */
public class SimpleTypeMethodDestinationNotation extends SimpleRouteDestinationNotation<Type, MethodTypeSpec> {
    public SimpleTypeMethodDestinationNotation(@Nullable String rootString,
                                               @NotNull String targetString,
                                               @Nullable Type root,
                                               @NotNull MethodTypeSpec target) {
        super(rootString, targetString, root, target);
    }

    /**
     * Parse the notation.
     *
     * @param notation Notation to parse.
     * @param resolver Resolver of elements.
     * @return Result with either parsed notation object or parse exception.
     */
    public static Result<SimpleTypeMethodDestinationNotation, DestinationParseException> parseType(String notation,
                                                                                                   DestinationResolver<Type, MethodTypeSpec> resolver) {
        return SimpleRouteDestinationNotation.parse(notation, resolver)
                .map(parsedNotation -> new SimpleTypeMethodDestinationNotation(
                        parsedNotation.getRootString().orElse(null),
                        parsedNotation.getTargetString(),
                        parsedNotation.getRoot().orElse(null),
                        parsedNotation.getTarget())
                );
    }

}
