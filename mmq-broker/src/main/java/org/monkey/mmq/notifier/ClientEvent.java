package org.monkey.mmq.notifier;

import org.monkey.mmq.core.notify.Event;
import org.monkey.mmq.metadata.message.ClientMateData;

/**
 * @ClassNameClientEvent
 * @Description
 * @Author Solley
 * @Date2021/12/15 21:48
 * @Version V1.0
 **/
public class ClientEvent extends Event {

    public ClientMateData getClientMateData() {
        return clientMateData;
    }

    public void setClientMateData(ClientMateData clientMateData) {
        this.clientMateData = clientMateData;
    }

    public ClientMateData clientMateData;
}
