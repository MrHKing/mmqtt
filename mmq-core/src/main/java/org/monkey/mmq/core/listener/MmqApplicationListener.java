/*
 * Copyright 2021-2021 Monkey Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.monkey.mmq.core.listener;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * MMQ Application Listener, execute init process.
 *
 * @author solley
 */
public interface MmqApplicationListener {
    
    /**
     *
     */
    void starting();
    
    /**
     *
     * @param environment environment
     */
    void environmentPrepared(ConfigurableEnvironment environment);
    
    /**
     *
     * @param context context
     */
    void contextPrepared(ConfigurableApplicationContext context);
    
    /**
     *
     * @param context context
     */
    void contextLoaded(ConfigurableApplicationContext context);
    
    /**
     *
     * @param context context
     */
    void started(ConfigurableApplicationContext context);
    
    /**
     *
     * @param context context
     */
    void running(ConfigurableApplicationContext context);
    
    /**
     *
     * @param context   context
     * @param exception exception
     */
    void failed(ConfigurableApplicationContext context, Throwable exception);
}
