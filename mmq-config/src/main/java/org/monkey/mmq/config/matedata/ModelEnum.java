package org.monkey.mmq.config.matedata;

/**
 * @ClassNameModelEnum
 * @Description
 * @Author Solley
 * @Date2022/1/13 21:23
 * @Version V1.0
 **/
public enum ModelEnum {
    AUTH(org.monkey.mmq.config.modules.auth.AuthModule.class),
    API(org.monkey.mmq.config.modules.api.ApiModule.class);

    private Class name;

    public Class getName() {
        return name;
    }

    ModelEnum(Class name) {
        this.name = name;
    }
}
