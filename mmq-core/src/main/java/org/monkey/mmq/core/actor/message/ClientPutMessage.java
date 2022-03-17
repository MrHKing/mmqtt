package org.monkey.mmq.core.actor.message;

import org.monkey.mmq.core.actor.ActorMsg;
import org.monkey.mmq.core.actor.MsgType;
import org.monkey.mmq.core.actor.metadata.message.ClientMateData;

/**
 * @ClassNameClientEvent
 * @Description
 * @Author Solley
 * @Date2021/12/15 21:48
 * @Version V1.0
 **/
public class ClientPutMessage implements ActorMsg {

    public ClientMateData clientMateData;

    public ClientMateData getClientMateData() {
        return clientMateData;
    }

    public void setClientMateData(ClientMateData clientMateData) {
        this.clientMateData = clientMateData;
    }

    @Override
    public MsgType getMsgType() {
        return MsgType.CLIENT_PUT;
    }
}
