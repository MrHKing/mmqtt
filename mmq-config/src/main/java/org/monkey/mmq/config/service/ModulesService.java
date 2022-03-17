package org.monkey.mmq.config.service;

import org.monkey.mmq.config.config.Loggers;
import org.monkey.mmq.config.matedata.*;
import org.monkey.mmq.config.modules.IModule;
import org.monkey.mmq.config.modules.ModuleFactory;
import org.monkey.mmq.config.modules.ModelMateData;
import org.monkey.mmq.core.consistency.matedata.RecordListener;
import org.monkey.mmq.core.consistency.persistent.ConsistencyService;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.core.utils.ApplicationUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassNameModulesService
 * @Description
 * @Author Solley
 * @Date2022/1/13 21:27
 * @Version V1.0
 **/
@Service
public class ModulesService implements RecordListener<ModelMateData> {

    Map<String, ModelMateData> modelMateDataHashMap = new HashMap<>();

    @Resource(name = "configPersistentConsistencyServiceDelegate")
    private ConsistencyService consistencyService;

    /**
     * Init
     */
    @PostConstruct
    public void init() {
        try {
            consistencyService.listen(KeyBuilder.getModulesKey(), this);

            // 初始化所有模块
            Map<String, IModule> result = ApplicationUtils.getApplicationContext().getBeansOfType(IModule.class);
            result.entrySet().forEach(x -> {
                modelMateDataHashMap.put(UtilsAndCommons.MODULES_STORE + x.getValue().getConfig().getKey(), x.getValue().getConfig());
            });
        } catch (MmqException e) {
            Loggers.CONFIG_SERVER.error("listen modules service failed.", e);
        }
    }

    public void update(ModelMateData modelMateData) {
        try {
            consistencyService.put(UtilsAndCommons.MODULES_STORE + modelMateData.getKey(), modelMateData);
        } catch (MmqException e) {
            Loggers.CONFIG_SERVER.error("save resources failed.", e);
        }
    }

    public Collection<ModelMateData> listModelMateData() {
        return modelMateDataHashMap.values();
    }

    @Override
    public boolean interests(String key) {
        return KeyBuilder.matchModulesKey(key);
    }

    @Override
    public boolean matchUnlistenKey(String key) {
        return KeyBuilder.matchModulesKey(key);
    }

    @Override
    public void onChange(String key, ModelMateData value) throws Exception {
        modelMateDataHashMap.put(key, value);

        // 更新配置
        ModuleFactory.getResourceDriverByEnum(value.getModelEnum()).update(value);
    }

    @Override
    public void onDelete(String key) throws Exception {

    }
}
