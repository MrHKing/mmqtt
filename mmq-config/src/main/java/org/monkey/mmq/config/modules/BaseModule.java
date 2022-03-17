package org.monkey.mmq.config.modules;

import org.monkey.mmq.core.utils.StringUtils;

/**
 * @ClassNameBaseModule
 * @Description
 * @Author Solley
 * @Date2022/1/18 23:00
 * @Version V1.0
 **/
public abstract class BaseModule<Param> implements IModule<Param> {

    protected ModelMateData modelMateData;

    @Override
    public void update(ModelMateData modelMateData) {
        if (modelMateData == null) return;
        if (StringUtils.isEmpty(modelMateData.getDescription())) return;
        if (StringUtils.isEmpty(modelMateData.getIcon())) return;
        if (StringUtils.isEmpty(modelMateData.getModuleName())) return;
        this.modelMateData = modelMateData;
    }

    @Override
    public ModelMateData getConfig() {
        return this.modelMateData;
    }

    @Override
    public boolean getEnable() {
        return this.modelMateData.getEnable();
    }
}
