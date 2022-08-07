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

import org.apache.commons.lang3.StringUtils;
import org.monkey.mmq.core.env.EnvUtil;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.core.exception.runtime.MmqRuntimeException;
import org.monkey.mmq.core.executor.ExecutorFactory;
import org.monkey.mmq.core.executor.NameThreadFactory;
import org.monkey.mmq.core.executor.ThreadPoolManager;
import org.monkey.mmq.core.file.FileChangeEvent;
import org.monkey.mmq.core.file.FileWatcher;
import org.monkey.mmq.core.file.WatchFileCenter;
import org.monkey.mmq.core.notify.NotifyCenter;
import org.monkey.mmq.core.plugs.PlugsFactory;
import org.monkey.mmq.core.utils.ApplicationUtils;
import org.monkey.mmq.core.utils.DiskUtils;
import org.monkey.mmq.core.utils.InetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * init environment config.
 *
 * @author solley
 */
public class StartingApplicationListener implements MmqApplicationListener {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(StartingApplicationListener.class);
    
    private static final String MODE_PROPERTY_KEY_STAND_MODE = "mmq.mode";
    
    private static final String MODE_PROPERTY_KEY_FUNCTION_MODE = "mmq.function.mode";
    
    private static final String LOCAL_IP_PROPERTY_KEY = "mmq.local.ip";
    
    private static final String MMQ_APPLICATION_CONF = "mmq";
    
    private static final String MMQ_MODE_STAND_ALONE = "stand alone";
    
    private static final String MMQ_MODE_CLUSTER = "cluster";
    
    private static final String DEFAULT_FUNCTION_MODE = "All";
    
    private static final Map<String, Object> SOURCES = new ConcurrentHashMap<>();

    private static ConfigurableApplicationContext context;
    
    private ScheduledExecutorService scheduledExecutorService;
    
    private volatile boolean starting;
    
    @Override
    public void starting() {
        starting = true;
    }
    
    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {
        makeWorkDir();
        
        injectEnvironment(environment);
        
        loadPreProperties(environment);
        
        initSystemProperty();
    }
    
    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        this.context = context;
        readJar(EnvUtil.getPlugsPath());
        logClusterConf();
        
        logStarting();
    }
    
    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {

    }
    
    @Override
    public void started(ConfigurableApplicationContext context) {
        starting = false;
        
        closeExecutor();
        
        ApplicationUtils.setStarted(true);
        judgeStorageMode(context.getEnvironment());
    }
    
    @Override
    public void running(ConfigurableApplicationContext context) {
    }
    
    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        starting = false;
        
        makeWorkDir();
        
        LOGGER.error("Startup errors : ", exception);
        ThreadPoolManager.shutdown();
        WatchFileCenter.shutdown();
        NotifyCenter.shutdown();
        
        closeExecutor();
        
        context.close();
        
        LOGGER.error("Mmq failed to start, please see {} for more details.",
                Paths.get(EnvUtil.getMmqHome(), "logs/mmq.log"));
    }
    
    private void injectEnvironment(ConfigurableEnvironment environment) {
        EnvUtil.setEnvironment(environment);
    }
    
    private void loadPreProperties(ConfigurableEnvironment environment) {
        try {
            SOURCES.putAll(EnvUtil.loadProperties(EnvUtil.getApplicationConfFileResource()));
            environment.getPropertySources()
                    .addLast(new OriginTrackedMapPropertySource(MMQ_APPLICATION_CONF, SOURCES));
            registerWatcher();
        } catch (Exception e) {
            throw new MmqRuntimeException(MmqException.SERVER_ERROR, e);
        }
    }
    
    private void registerWatcher() throws MmqException {
        
        WatchFileCenter.registerWatcher(EnvUtil.getConfPath(), new FileWatcher() {
            @Override
            public void onChange(FileChangeEvent event) {
                try {
                    Map<String, ?> tmp = EnvUtil.loadProperties(EnvUtil.getApplicationConfFileResource());
                    SOURCES.putAll(tmp);
                } catch (IOException ignore) {
                    LOGGER.warn("Failed to monitor file ", ignore);
                }
            }
            
            @Override
            public boolean interest(String context) {
                return StringUtils.contains(context, "application.properties");
            }
        });

    WatchFileCenter.registerWatcher(
        EnvUtil.getPlugsPath(),
        new FileWatcher() {
          @Override
          public void onChange(FileChangeEvent event) {
            try {
              PlugsFactory.createPlug(event.getPaths() + "/" + event.getContext(), context);
            } catch (Exception ignore) {
              LOGGER.warn("Failed to monitor file ", ignore);
            }
          }

          @Override
          public boolean interest(String context) {
            return StringUtils.contains(context, ".jar");
          }
        });
    }

    private void readJar(String paths)  {
        try {
            File file = new File(paths);
            if (!file.exists()) {
                throw new IllegalArgumentException("Must be a file directory : " + paths);
            }
            File fa[] = file.listFiles();
            for (int i = 0; i < fa.length; i++) {
                File fs = fa[i];
                if (!fs.isDirectory() && fs.getName().endsWith(".jar")) {
                    PlugsFactory.createPlug(fs.getPath(), context);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed load file ", e);
        }
    }

    private void initSystemProperty() {
        if (EnvUtil.getStandaloneMode()) {
            System.setProperty(MODE_PROPERTY_KEY_STAND_MODE, MMQ_MODE_STAND_ALONE);
        } else {
            System.setProperty(MODE_PROPERTY_KEY_STAND_MODE, MMQ_MODE_CLUSTER);
        }
        if (EnvUtil.getFunctionMode() == null) {
            System.setProperty(MODE_PROPERTY_KEY_FUNCTION_MODE, DEFAULT_FUNCTION_MODE);
        }

        System.setProperty(LOCAL_IP_PROPERTY_KEY, InetUtils.getSelfIP());
    }
    
    private void logClusterConf() {
        if (!EnvUtil.getStandaloneMode()) {
            try {
                List<String> clusterConf = EnvUtil.readClusterConf();
                LOGGER.info("The server IP list of Mmq is {}", clusterConf);
            } catch (IOException e) {
                LOGGER.error("read cluster conf fail", e);
            }
        }
    }
    
    private void closeExecutor() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
        }
    }
    
    private void makeWorkDir() {
        String[] dirNames = new String[] {"logs", "conf", "data", "plugs"};
        for (String dirName : dirNames) {
            LOGGER.info("Mmq Log files: {}", Paths.get(EnvUtil.getMmqHome(), dirName).toString());
            try {
                DiskUtils.forceMkdir(new File(Paths.get(EnvUtil.getMmqHome(), dirName).toUri()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    private void logStarting() {
        if (!EnvUtil.getStandaloneMode()) {
            
            scheduledExecutorService = ExecutorFactory
                    .newSingleScheduledExecutorService(new NameThreadFactory("org.monkey.mmq.core.mmq-starting"));
            
            scheduledExecutorService.scheduleWithFixedDelay(() -> {
                if (starting) {
                    LOGGER.info("Mmq is starting...");
                }
            }, 1, 1, TimeUnit.SECONDS);
        }
    }
    
    private void judgeStorageMode(ConfigurableEnvironment env) {
        
        LOGGER.info("Mmq started successfully in {} mode",
                System.getProperty(MODE_PROPERTY_KEY_STAND_MODE));
    }
}
