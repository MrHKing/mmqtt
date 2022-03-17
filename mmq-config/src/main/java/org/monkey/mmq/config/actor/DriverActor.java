package org.monkey.mmq.config.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.monkey.mmq.core.actor.message.SystemMessage;
import org.monkey.mmq.config.driver.DriverFactory;
import org.monkey.mmq.config.matedata.DriverMessage;
import org.monkey.mmq.core.exception.MmqException;

import static org.monkey.mmq.core.common.Constants.RULE_ENGINE;

/**
 * @ClassNameDriverEventManager
 * @Description
 * @Author Solley
 * @Date2021/12/16 15:16
 * @Version V1.0
 **/
public class DriverActor extends AbstractActor {

    private ActorSystem actorSystem;

    public DriverActor(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(DriverMessage.class, msg -> {
            try {
                DriverFactory.getResourceDriverByEnum(msg.getResourcesMateData().getType()).handle(msg.getProperty(),
                        msg.getResourcesMateData(),
                        msg.getRuleEngineMessage().getMessage().getTopic(),
                        msg.getRuleEngineMessage().getMessage().getMqttQoS(),
                        msg.getRuleEngineMessage().getMessage().getAddress(),
                        msg.getRuleEngineMessage().getUsername());
            } catch (MmqException e) {
                SystemMessage systemMessage = new SystemMessage();
                systemMessage.setTopic(RULE_ENGINE);
                systemMessage.setPayload(e.getMessage());
                systemMessage.setMqttQoS(MqttQoS.AT_LEAST_ONCE);
                ActorSelection actorRef = actorSystem.actorSelection("/user/driver");
                actorRef.tell(systemMessage, ActorRef.noSender());
            }

        }).build();
    }
}
