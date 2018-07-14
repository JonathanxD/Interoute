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

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Configuration of {@link DefaultInterouteBackend}.
 *
 * This configuration store instances alias name and the instance itself. This is used to resolve the methods to route to.
 */
public class DefaultBackendConfiguration implements InterouteBackendConfiguration {

    /**
     * Stores instance alias/type and the instance object. These values are used to resolve method to route. The alias is the root
     * element resolved by {@link com.github.jonathanxd.interoute.route.def.SimpleRouteDestinationNotation}.
     */
    private final Map<String, Object> instances;

    /**
     * Constructs {@link DefaultBackendConfiguration} and register {@code instances} in this {@link #instances} map.
     *
     * @param instances Instances to register.
     */
    private DefaultBackendConfiguration(@NotNull Map<String, Object> instances) {
        this.instances = Collections.unmodifiableMap(Objects.requireNonNull(instances, "Instances map cannot be null."));
    }

    /**
     * Creates a new {@link DefaultBackendConfiguration} with empty instances registry.
     *
     * @return {@link DefaultBackendConfiguration} with empty instances registry.
     */
    public static DefaultBackendConfiguration newConfiguration() {
        return new DefaultBackendConfiguration(Collections.emptyMap());
    }

    /**
     * Creates a new {@link DefaultBackendConfiguration} with {@code instances} in the instance registry.
     *
     * @return {@link DefaultBackendConfiguration} with {@code instances} in the instance registry.
     */
    public static DefaultBackendConfiguration newConfiguration(@NotNull Map<String, Object> instances) {
        return new DefaultBackendConfiguration(instances);
    }

    /**
     * Creates a builder of {@link DefaultBackendConfiguration}.
     *
     * @return Builder of {@link DefaultBackendConfiguration}.
     */
    public static DefaultBackendConfiguration.Builder builder() {
        return DefaultBackendConfiguration.Builder.builder();
    }

    /**
     * Gets the instance registered with {@code name}.
     *
     * @param name Name of registered instance.
     * @return Optional with the instance object if found, or {@link Optional#empty()} otherwise.
     */
    public Optional<Object> getInstance(String name) {
        return Optional.ofNullable(this.getInstances().get(name));
    }

    /**
     * Gets immutable map of registered instances.
     *
     * @return Immutable map of registered instances.
     */
    public Map<String, Object> getInstances() {
        return this.instances;
    }

    /**
     * Convert to a builder.
     *
     * @return Builder with a copy of values of this configuration.
     */
    public Builder toBuilder() {
        return Builder.builder(this.getInstances());
    }

    /**
     * Builder of {@link DefaultBackendConfiguration}.
     */
    public static class Builder {
        /**
         * The {@link DefaultBackendConfiguration#instances instance registry map}.
         */
        private Map<String, Object> instances;

        private Builder(Map<String, Object> instances) {
            this.instances = new HashMap<>(instances);
        }

        static Builder builder() {
            return new Builder(new HashMap<>());
        }

        static Builder builder(Map<String, Object> instances) {
            return new Builder(instances);
        }

        /**
         * Adds an instance to the instance registry.
         *
         * @param name     Name/alias of the instance. This name is used by the backend to lookup for the instance, the query
         *                 value is extracted from {@link com.github.jonathanxd.interoute.route.def.SimpleRouteDestinationNotation
         *                 simple destination notation}.
         * @param instance Instance.
         * @return {@code this} builder.
         */
        public Builder addInstance(@NotNull String name, @NotNull Object instance) {
            Objects.requireNonNull(name, "Name cannot be null.");
            Objects.requireNonNull(instance, "Instance cannot be null.");
            this.instances.put(name, instance);
            return this;
        }

        /**
         * Removes an instance by registered {@code name}/alias.
         *
         * @param name Registered name/alias.
         * @return {@code this} builder.
         */
        public Builder removeInstance(@NotNull String name) {
            Objects.requireNonNull(name, "Name cannot be null.");
            this.instances.remove(name);
            return this;
        }

        /**
         * Build the configuration.
         *
         * @return Configuration.
         */
        public DefaultBackendConfiguration build() {
            return DefaultBackendConfiguration.newConfiguration(this.instances);
        }
    }
}
