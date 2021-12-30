package org.monkey.mmq.config.modules;

import java.util.Map;

/**
 * @ClassNameIModules
 * @Description
 * @Author Solley
 * @Date2021/12/27 11:51
 * @Version V1.0
 **/
public interface IModules<Config, Param> {
    /**
     * 启动模块
     */
    Boolean getEnable();

    /**
     * 停止模块
     */
    void setEnable(Boolean enable);

    /**
     * 获得模块名称
     * @return
     */
    String getModuleName();

    /**
     * 获得描述
     * @return
     */
    String getDescription();

    /**
     * 图标
     * @return
     */
    String getIcon();

    /**
     * 写入配置
     * @param config
     */
    void setConfig(Config config);

    /**
     * 处理
     * @param param
     */
    boolean handle(Param param);
}
