package org.monkey.mmq.config.modules.acl;

/**
 * @ClassName:AclParam
 * @Auther: Solley
 * @Description: Acl controller info
 * @Date: 2022/7/28 20:05
 * @Version: v1.0
 */

public class AclParam {
    /**
     * id
     */
    private String id;
    /**
     * deny（0），allow（1）
     */
    private Integer allow;
    /**
     * ip address
     */
    private String ipaddr;
    /**
     * connect to the client username, if set $all indicates that the rule applies to all users
     */
    private String username;
    /**
     * connect to the client clientId
     */
    private String clientId;
    /**
     * allow operation: subscribe(1), publish(2), subscribe and publish (3)
     */
    private Integer access;
    /**
     * controller topic, wildcard character can by used
     */
    private String topic;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getAllow() {
        return allow;
    }

    public void setAllow(Integer allow) {
        this.allow = allow;
    }

    public String getIpaddr() {
        return ipaddr;
    }

    public void setIpaddr(String ipaddr) {
        this.ipaddr = ipaddr;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Integer getAccess() {
        return access;
    }

    public void setAccess(Integer access) {
        this.access = access;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
