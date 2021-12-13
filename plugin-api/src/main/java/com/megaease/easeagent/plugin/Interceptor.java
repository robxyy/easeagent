/*
 * Copyright (c) 2021 MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.plugin;

import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.enums.Order;

public interface Interceptor extends Ordered {
    /**
     * @param methodInfo instrumented method info
     * @param context    Interceptor can pass data, method `after` of interceptor can receive context data
     */
    void before(MethodInfo methodInfo, Context context);

    /**
     * @param methodInfo instrumented method info
     * @param context    Interceptor can pass data, method `after` of interceptor can receive context data
     */
    default void after(MethodInfo methodInfo, Context context) {
    };

    /**
     * Interceptor can get interceptor config thought Config API :
     * EaseAgent.configFactory.getConfig
     * Config API require 3 params: domain, nameSpace, name
     * domain and namespace are defined by plugin, the third param, name is defined here
     *
     * @return name, eg. tracing, metric, etc.
     */
    default String getType() {
        return Order.TRACING.getName();
    }

    /**
     * Initialization method for the interceptor,
     * This method will be called and only be called once for every method which is injected by this interceptor,
     * which means this method may be called several times, when there are several methods matched
     *
     * @param config interceptor configuration
     * @param className injected method's class name
     * @param methodName injected method name
     * @param methodDescriptor injected method descriptor
     */
    default void init(Config config, String className, String methodName, String methodDescriptor) {
    }

    /**
     * Initialization method for the interceptor,
     * This method will be called and only be called once for every method which is injected by this interceptor,
     * which means this method may be called several times, when there are several methods matched
     *
     * @param config    interceptor configuration
     * @param uniqueIndex an Integer unique index generated by agent
     *                    for each combination of className, method and methodDescriptor
     */
    default void init(Config config, int uniqueIndex) {
    }
}
