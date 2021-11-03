/*
 * Copyright (c) 2021, MegaEase
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

package com.megaease.easeagent.core.plugin.interceptor;

import com.megaease.easeagent.plugin.AgentPlugin;
import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.StateInterceptor;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.InitializeContext;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.ConfigChangeListener;
import com.megaease.easeagent.plugin.asm.Modifier;
import com.megaease.easeagent.plugin.bridge.EaseAgent;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public class InterceptorPluginDecorator implements Interceptor, ConfigChangeListener {
    private Interceptor interceptor;
    private AgentPlugin plugin;
    private Config config;

    public InterceptorPluginDecorator(Interceptor interceptor, AgentPlugin plugin) {
        this.interceptor = interceptor;
        this.plugin = plugin;
        this.config = EaseAgent.configFactory.getConfig(plugin.getDomain(), plugin.getName(), interceptor.getName());
        this.config.addChangeListener(this);
    }

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        Config cfg = this.config;
        ((InitializeContext) context).pushConfig(cfg);
        if (cfg != null && cfg.enable()) {
            this.interceptor.before(methodInfo, context);
        }
    }

    @Override
    public void after(MethodInfo methodInfo, Context context) {
        Config cfg = ((InitializeContext) context).getConfig();
        if (cfg != null && cfg.enable()) {
            this.interceptor.after(methodInfo, context);
        }
        ((InitializeContext) context).popConfig();
    }

    @Override
    public String getName() {
        return this.interceptor.getName();
    }

    @Override
    public int order() {
        int pluginOrder = this.plugin.order();
        int interceptorOrder = this.interceptor.order();
        int current = interceptorOrder << 8 + pluginOrder;
        return current;
    }

    public static Supplier<Interceptor> getInterceptorSupplier(final AgentPlugin plugin, final Supplier<Interceptor> supplier) {
        Supplier<Interceptor> decoratorSupplier = new Supplier<Interceptor>() {
            @Override
            public Interceptor get() {
                Interceptor interceptor = supplier.get();
                Field[] fs = interceptor.getClass().getDeclaredFields();
                for (Field f : fs) {
                    // has non-static field
                    if ((f.getModifiers() & Modifier.ACC_STATIC) == 0) {
                        interceptor = new StateInterceptor(supplier);
                    }
                }
                return new InterceptorPluginDecorator(interceptor, plugin);
            }
        };
        return decoratorSupplier;
    }

    @Override
    public void onChange(Config oldConfig, Config newConfig) {
        this.config = newConfig;
        this.config.addChangeListener(this);
    }
}