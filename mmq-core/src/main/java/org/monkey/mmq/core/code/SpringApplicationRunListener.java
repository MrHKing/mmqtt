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

package org.monkey.mmq.core.code;

import org.monkey.mmq.core.listener.LoggingApplicationListener;
import org.monkey.mmq.core.listener.MmqApplicationListener;
import org.monkey.mmq.core.listener.StartingApplicationListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.EventPublishingRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link org.springframework.boot.SpringApplicationRunListener} before {@link EventPublishingRunListener} execution.
 *
 * @author solley
 */
public class SpringApplicationRunListener implements org.springframework.boot.SpringApplicationRunListener, Ordered {
    
    private final SpringApplication application;
    
    private final String[] args;
    
    private List<MmqApplicationListener> mmqApplicationListeners = new ArrayList<>();
    
    {
        mmqApplicationListeners.add(new LoggingApplicationListener());
        mmqApplicationListeners.add(new StartingApplicationListener());
    }
    
    public SpringApplicationRunListener(SpringApplication application, String[] args) {
        this.application = application;
        this.args = args;
    }
    
    @Override
    public void starting() {
        for (MmqApplicationListener mmqApplicationListener : mmqApplicationListeners) {
            mmqApplicationListener.starting();
        }
    }
    
    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {
        for (MmqApplicationListener mmqApplicationListener : mmqApplicationListeners) {
            mmqApplicationListener.environmentPrepared(environment);
        }
    }
    
    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        for (MmqApplicationListener mmqApplicationListener : mmqApplicationListeners) {
            mmqApplicationListener.contextPrepared(context);
        }
    }
    
    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        for (MmqApplicationListener mmqApplicationListener : mmqApplicationListeners) {
            mmqApplicationListener.contextLoaded(context);
        }
    }
    
    @Override
    public void started(ConfigurableApplicationContext context) {
        for (MmqApplicationListener mmqApplicationListener : mmqApplicationListeners) {
            mmqApplicationListener.started(context);
        }
    }
    
    @Override
    public void running(ConfigurableApplicationContext context) {
        for (MmqApplicationListener mmqApplicationListener : mmqApplicationListeners) {
            mmqApplicationListener.running(context);
        }
    }
    
    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        for (MmqApplicationListener mmqApplicationListener : mmqApplicationListeners) {
            mmqApplicationListener.failed(context, exception);
        }
    }
    
    /**
     * Before {@link EventPublishingRunListener}.
     *
     * @return HIGHEST_PRECEDENCE
     */
    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
