package org.monkey.mmq.notifier;

import org.monkey.mmq.core.consistency.persistent.ConsistencyService;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.core.notify.Event;
import org.monkey.mmq.core.notify.NotifyCenter;
import org.monkey.mmq.core.notify.listener.Subscriber;
import org.monkey.mmq.metadata.UtilsAndCommons;
import org.monkey.mmq.metadata.message.ClientMateData;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import static org.monkey.mmq.metadata.UtilsAndCommons.CLIENT_PUT;
import static org.monkey.mmq.metadata.UtilsAndCommons.CLIENT_REMOVE;

/**
 * @ClassNameClientEventManager
 * @Description
 * @Author Solley
 * @Date2021/12/15 21:51
 * @Version V1.0
 **/
@Component
public class ClientEventManager extends Subscriber<ClientEvent> {



    @Resource(name = "mqttPersistentConsistencyServiceDelegate")
    private ConsistencyService consistencyService;

    private final int queueMaxSize = 65539;

    @PostConstruct
    public void init() {
        NotifyCenter.registerToPublisher(ClientEvent.class, queueMaxSize);
        NotifyCenter.registerSubscriber(this);
    }

    @Override
    public void onEvent(ClientEvent event) {
        try {
            if (CLIENT_PUT == event.getOperationType()) {
                consistencyService.put(UtilsAndCommons.SESSION_STORE + event.getClientMateData().getClientId(),
                        event.getClientMateData());
            } else if (CLIENT_REMOVE == event.getOperationType()){
                consistencyService.remove(UtilsAndCommons.SESSION_STORE + event.getClientMateData().getClientId());
            }

        } catch (MmqException e) {

        }
    }

    @Override
    public Class<? extends Event> subscribeType() {
        return ClientEvent.class;
    }
}
