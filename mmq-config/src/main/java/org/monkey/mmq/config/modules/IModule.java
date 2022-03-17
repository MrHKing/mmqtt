package org.monkey.mmq.config.modules;

import org.monkey.mmq.core.exception.MmqException;

/**
 * @ClassNameIModule
 * @Description
 * @Author Solley
 * @Date2022/1/13 20:48
 * @Version V1.0
 **/
public interface IModule<Param> {

    void update(ModelMateData config);

    boolean handle(Param param) throws MmqException;

    ModelMateData getConfig();

    boolean getEnable();
}
