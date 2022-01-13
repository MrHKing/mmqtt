package org.monkey.mmq.config.modules;

import org.monkey.mmq.config.matedata.ModelMateData;

/**
 * @ClassNameIModule
 * @Description
 * @Author Solley
 * @Date2022/1/13 20:48
 * @Version V1.0
 **/
public interface IModule<Param> {

    void update(ModelMateData config);

    boolean handle(Param param) throws Exception;

    ModelMateData getConfig();

    boolean getEnable();
}
