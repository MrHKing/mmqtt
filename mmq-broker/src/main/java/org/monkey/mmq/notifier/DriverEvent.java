package org.monkey.mmq.notifier;

import org.monkey.mmq.config.matedata.ResourcesMateData;
import org.monkey.mmq.core.notify.Event;

import java.util.Map;

/**
 * @ClassNameDriverEvent
 * @Description
 * @Author Solley
 * @Date2021/12/16 15:13
 * @Version V1.0
 **/
public class DriverEvent extends Event {

    public RuleEngineEvent ruleEngineEvent;

    public ResourcesMateData resourcesMateData;

    Map property;

    public RuleEngineEvent getRuleEngineEvent() {
        return ruleEngineEvent;
    }

    public void setRuleEngineEvent(RuleEngineEvent ruleEngineEvent) {
        this.ruleEngineEvent = ruleEngineEvent;
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

}
