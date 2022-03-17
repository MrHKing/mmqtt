package org.monkey.mmq.config.modules;

import org.monkey.mmq.config.matedata.ModelEnum;
import org.monkey.mmq.core.consistency.matedata.Record;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassNameBaseModelMateData
 * @Description
 * @Author Solley
 * @Date2021/12/31 11:58
 * @Version V1.0
 **/
public class ModelMateData implements Record, Serializable {

    /**
     * key
     */
    private String key;

    /**
     * is enable
     */
    private Boolean enable;

    /**
     * module name
     */
    private String moduleName;

    /**
     * module description
     */
    private String description;

    /**
     * module icon
     */
    private String icon;

    /**
     * modules type
     */
    private ModelEnum modelEnum;

    /**
     * module config
     */
    private Map configs;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Map getConfigs() {
        return configs;
    }

    public void setConfigs(Map configs) {
        this.configs = configs;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public ModelEnum getModelEnum() {
        return modelEnum;
    }

    public void setModelEnum(ModelEnum modelEnum) {
        this.modelEnum = modelEnum;
    }
}
