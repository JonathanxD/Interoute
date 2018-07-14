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
package com.github.jonathanxd.interoute.exception;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GenerationException extends Exception {

    public GenerationException(String message) {
        super(message);
    }

    public GenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public GenerationException(Throwable cause) {
        super(cause);
    }

    public GenerationException() {
    }

    /**
     * Creates a generation exception from a list of generation exceptions. This method will recreate every exception pointing to
     * a new cause.
     *
     * @param exceptions Exception list.
     * @return Single exception carrying all others as cause of the exception.
     */
    @NotNull
    public static GenerationException fromExceptionList(@NotNull List<GenerationException> exceptions) {
        if (exceptions.isEmpty())
            throw new IllegalArgumentException("'errorList' must not be empty.");

        GenerationException last = exceptions.get(exceptions.size() - 1);

        for (int i = exceptions.size() - 2; i >= 0; i--) {
            GenerationException ex = new GenerationException(last.getMessage(), exceptions.get(i));
            ex.setStackTrace(last.getStackTrace());
            last = ex;
        }

        return last;
    }
}
