package org.monkey.mmq.core.actor;

/**
 * @ClassName:StopMessage
 * @Auther: Solley
 * @Description: 停止消息
 * @Date: 2022/7/21 08:30
 * @Version: v1.0
 */

public class StopMessage implements ActorMsg {
    @Override
    public MsgType getMsgType() {
        return MsgType.STOP;
    }
}
