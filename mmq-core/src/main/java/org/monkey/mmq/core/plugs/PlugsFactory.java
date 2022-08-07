package org.monkey.mmq.core.plugs;


import org.monkey.mmq.core.utils.ClassUtil;
import org.monkey.mmq.core.utils.Loggers;
import org.monkey.mmq.plugs.AbstractMmqPlugs;
import org.monkey.mmq.plugs.IMmqPlugs;
import org.monkey.mmq.plugs.view.MmqPlugsView;
import org.reflections.Reflections;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName:LicenseFactory
 * @Auther: Solley
 * @Description:
 * @Date: 2022/8/4 08:32
 * @Version: v1.0
 */

public class PlugsFactory {
    private static ConcurrentHashMap<String, IMmqPlugs> plugsList = new ConcurrentHashMap<>();

    public static void createPlug(String path, ConfigurableApplicationContext context) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        ClassUtil.loadJar(path);
        Loggers.CORE.info(path);
        Reflections reflections = new Reflections("org.monkey.mmq.plugs");
        Set<Class<? extends IMmqPlugs>> set = reflections.getSubTypesOf(IMmqPlugs.class);
        for (Class<? extends IMmqPlugs> dict : set) {
            Loggers.CORE.info(dict.toString());
            if (dict != AbstractMmqPlugs.class) {
                IMmqPlugs plugs = dict.newInstance();
                plugs.setLocalMqttPort(Integer.parseInt(Objects.requireNonNull(context.getEnvironment().getProperty("mmq.broker.port"))));
                plugsList.putIfAbsent(plugs.getPlugsCode(), plugs);
            }
        }
    }

    public static void update(MmqPlugsView modal) {
        IMmqPlugs plugs = plugsList.get(modal.getPlugsCode());
        if (plugs == null) return;
         plugs.update(modal);
    }

    public static ConcurrentHashMap<String, IMmqPlugs> getPlugsList() {
        return plugsList;
    }
}
