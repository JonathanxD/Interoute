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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple notation of route destination. This notation support optional text in the start of notation and a simple text in the
 * end, example: {@code Greeter.hello} and {@code hello}. The text in the start is the root and the text in the end is the
 * target.
 */
public class SimpleRouteDestinationNotation<R, T> {
    private static final Pattern NOTATION_PATTERN = Pattern.compile("((\\w+)\\.)?(\\w+)");

    /**
     * Root notation string.
     */
    @Nullable
    private final String rootString;

    /**
     * Target notation string.
     */
    @NotNull
    private final String targetString;

    /**
     * Root element.
     */
    @Nullable
    private final R root;

    /**
     * Target element.
     */
    @NotNull
    private final T target;

    public SimpleRouteDestinationNotation(@Nullable String rootString,
                                          @NotNull String targetString,
                                          @Nullable R root,
                                          @NotNull T target) {
        this.rootString = rootString;
        this.targetString = targetString;
        this.root = root;
        this.target = target;
    }

    /**
     * Parse the notation string.
     *
     * @param notation Notation.
     * @param resolver Resolver of elements.
     * @param <R>      Root element type.
     * @param <T>      Target element type.
     * @return Result with either route destination notation object or parse exception.
     */
    public static <R, T> Result<SimpleRouteDestinationNotation<R, T>, DestinationParseException> parse(String notation,
                                                                                                       DestinationResolver<R, T> resolver) {
        Matcher matcher = NOTATION_PATTERN.matcher(notation);

        if (matcher.matches()) {
            String rootNotation = matcher.group(2);
            String targetNotation = matcher.group(3);

            Result<R, DestinationParseException> root = rootNotation != null
                    ? resolver.rootResolver().resolve(rootNotation)
                    : Result.ok(null);

            Result<T, DestinationParseException> target = resolver.targetResolver()
                    .resolve(targetNotation, root.successOrNull());

            if (root instanceof Result.Err<?, ?>) {
                return ((Result.Err<R, DestinationParseException>) root).as();
            } else {
                return root.combineSuccess(target, (rootInstance, targetInstance) ->
                        new SimpleRouteDestinationNotation<>(
                                rootNotation,
                                targetNotation,
                                rootInstance,
                                targetInstance
                        )
                );
            }
        } else {
            return Result.error(new DestinationParseException(String.format(
                    "Destination expression '%s' does not match simple route destination notation.",
                    notation
            )));
        }
    }

    /**
     * Gets the root notation string.
     * @return Root notation string.
     */
    @Contract(pure = true)
    @NotNull
    public final Optional<String> getRootString() {
        return Optional.ofNullable(this.rootString);
    }

    /**
     * Gets the target notation string.
     * @return Target notation string.
     */
    @Contract(pure = true)
    @NotNull
    public final String getTargetString() {
        return this.targetString;
    }

    /**
     * Gets the root element.
     * @return Root element.
     */
    @Contract(pure = true)
    @NotNull
    public final Optional<R> getRoot() {
        return Optional.ofNullable(this.root);
    }

    /**
     * Gets the target element.
     * @return Target element.
     */
    @Contract(pure = true)
    @NotNull
    public final T getTarget() {
        return this.target;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        SimpleRouteDestinationNotation<?, ?> that = (SimpleRouteDestinationNotation<?, ?>) o;
        return Objects.equals(this.getRootString(), that.getRootString()) &&
                Objects.equals(this.getTargetString(), that.getTargetString()) &&
                Objects.equals(this.getRoot(), that.getRoot()) &&
                Objects.equals(this.getTarget(), that.getTarget());
    }

    @Contract(pure = true)
    @Override
    public final int hashCode() {
        return Objects.hash(this.getRootString(), this.getTargetString(), this.getRoot(), this.getTarget());
    }

    @Contract(pure = true)
    @Override
    public final String toString() {
        return "SimpleRouteDestinationNotation{" +
                "rootString='" + this.getRootString() + '\'' +
                ", targetString='" + this.getTargetString() + '\'' +
                ", root=" + this.getRoot() +
                ", target=" + this.getTarget() +
                '}';
    }
}
