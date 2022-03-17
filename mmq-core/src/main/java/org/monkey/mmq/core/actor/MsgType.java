package org.monkey.mmq.core.actor;

import org.monkey.mmq.core.actor.message.ClientPutMessage;
import org.monkey.mmq.core.actor.message.SystemMessage;

/**
 * @ClassNameMsgType
 * @Description
 * @Author Solley
 * @Date2022/3/16 22:14
 * @Version V1.0
 **/
public enum MsgType {
    /**
     * ADD events for client
     * See {@link ClientPutMessage}
     */
    CLIENT_PUT,

    /**
     * REMOVE events for client
     * See {@link org.monkey.mmq.core.actor.message.ClientRemoveMessage}
     */
    CLIENT_REMOVE,

    /**
     * PUBLISH events for client send message
     * See {@link org.monkey.mmq.core.actor.message.PublishMessage}
     */
    PUBLISH_MSG,

    /**
     * REJECT events for client
     * See {@link org.monkey.mmq.core.actor.message.RejectMessage}
     */
    REJECT_CLIENT,

    /**
     * RULE_ENGINE events for publish message
     * See {@link org.monkey.mmq.core.actor.message.RuleEngineMessage}
     */
    RULE_ENGINE,

    /**
     * RULE_ENGINE events for publish message
     * See {@link org.monkey.mmq.core.actor.message.RuleEngineMessage}
     */
    UPDATE_RULE_ENGINE,

    /**
     * DRIVER_BRIDGE events for message bridge io
     * See {@link org.monkey.mmq.core.actor.message.DriverMessage}
     */
    DRIVER_BRIDGE,

    /**
     * SYSTEM_INFO events for system message
     * See {@link SystemMessage}
     */
    SYSTEM_INFO,
}
