package org.monkey.mmq.notifier;

import io.netty.handler.codec.mqtt.MqttQoS;
import org.monkey.mmq.config.Loggers;
import org.monkey.mmq.config.driver.DriverFactory;
import org.monkey.mmq.core.notify.Event;
import org.monkey.mmq.core.notify.NotifyCenter;
import org.monkey.mmq.core.notify.listener.Subscriber;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static org.monkey.mmq.core.common.Constants.RULE_ENGINE;

/**
 * @ClassNameDriverEventManager
 * @Description
 * @Author Solley
 * @Date2021/12/16 15:16
 * @Version V1.0
 **/
@Component
public class DriverEventManager extends Subscriber<DriverEvent> {

    private final int queueMaxSize = 65539;

    @PostConstruct
    public void init() {
        NotifyCenter.registerToPublisher(DriverEvent.class, queueMaxSize);
        NotifyCenter.registerSubscriber(this);
    }

    @Override
    public void onEvent(DriverEvent event) {
        try {

            DriverFactory.getResourceDriverByEnum(event.getResourcesMateData().getType()).handle(event.getProperty(),
                    event.getResourcesMateData(),
                    event.getRuleEngineEvent().getMessage().getTopic(),
                    event.getRuleEngineEvent().getMessage().getMqttQoS(),
                    event.getRuleEngineEvent().getMessage().getAddress(),
                    event.getRuleEngineEvent().getUsername());
        } catch (Exception e) {
            Loggers.BROKER_SERVER.error(e.getMessage());
            SysMessageEvent sysMessageEvent = new SysMessageEvent();
            sysMessageEvent.setTopic(RULE_ENGINE);
            sysMessageEvent.setPayload(e.getMessage());
            sysMessageEvent.setMqttQoS(MqttQoS.AT_LEAST_ONCE);
            NotifyCenter.publishEvent(sysMessageEvent);
        }
    }

    @Override
    public Class<? extends Event> subscribeType() {
        return DriverEvent.class;
    }
}
