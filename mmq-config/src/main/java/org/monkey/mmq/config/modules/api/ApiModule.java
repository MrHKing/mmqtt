package org.monkey.mmq.config.modules.api;

import org.monkey.mmq.config.matedata.ModelEnum;
import org.monkey.mmq.config.modules.ModelMateData;
import org.monkey.mmq.config.modules.BaseModule;
import org.monkey.mmq.core.exception.MmqException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassNameApiModule
 * @Description
 * @Author Solley
 * @Date2022/1/18 22:53
 * @Version V1.0
 **/
@Component
public class ApiModule extends BaseModule<ApiParam> {

    private static final String APP_ID = "mmq";

    private static final String APP_SECRET = "aaaaaa";

    @PostConstruct
    private void init() {
        modelMateData = new ModelMateData();
        modelMateData.setKey("API-MODULE");
        modelMateData.setModelEnum(ModelEnum.API);
        Map<String, Object> map = new HashMap<>();
        map.put("appId", APP_ID);
        map.put("appSecret", APP_SECRET);
        modelMateData.setConfigs(map);
        modelMateData.setDescription("HTTP API 使用Basic认证方式，id 和 password 须分别填写 AppID 和 AppSecret。 默认的 AppID 和 AppSecret 是：mmq/aaaaaa");
        modelMateData.setEnable(false);
        modelMateData.setIcon("api-icon");
        modelMateData.setModuleName("HTTP API");
    }

    @Override
    public boolean handle(ApiParam apiParam) throws MmqException {
        if (!this.modelMateData.getEnable()) return false;

        if (apiParam == null) return false;

        if (!modelMateData.getConfigs().get("appId").equals(apiParam.getAppId())) return false;

        if (!modelMateData.getConfigs().get("appSecret").equals(apiParam.getAppSecret())) return false;

        return true;
    }
}
