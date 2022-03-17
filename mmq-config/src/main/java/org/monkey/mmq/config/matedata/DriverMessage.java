package org.monkey.mmq.config.matedata;

import org.monkey.mmq.core.actor.ActorMsg;
import org.monkey.mmq.core.actor.MsgType;


import java.util.Map;

/**
 * @ClassNameDriverEvent
 * @Description
 * @Author Solley
 * @Date2021/12/16 15:13
 * @Version V1.0
 **/
public class DriverMessage implements ActorMsg {

    public RuleEngineMessage ruleEngineMessage;

    public ResourcesMateData resourcesMateData;

    Map property;

    public RuleEngineMessage getRuleEngineMessage() {
        return ruleEngineMessage;
    }

    public void setRuleEngineMessage(RuleEngineMessage ruleEngineMessage) {
        this.ruleEngineMessage = ruleEngineMessage;
    }

    public ResourcesMateData getResourcesMateData() {
        return resourcesMateData;
    }

    public void setResourcesMateData(ResourcesMateData resourcesMateData) {
        this.resourcesMateData = resourcesMateData;
    }

    public Map getProperty() {
        return property;
    }

    public void setProperty(Map property) {
        this.property = property;
    }

    @Override
    public MsgType getMsgType() {
        return MsgType.DRIVER_BRIDGE;
    }
}
