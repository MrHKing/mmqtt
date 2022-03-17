package org.monkey.mmq.config;

import akka.actor.ActorSystem;
import com.alipay.remoting.rpc.RpcClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassNameBrokerConfig
 * @Description
 * @Author Solley
 * @Date2022/3/17 0:50
 * @Version V1.0
 **/
@Configuration
public class BrokerConfig {
    @Bean
    public ActorSystem actorSystem() {
        return ActorSystem.create("mmq");
    }

    @Bean
    public RpcClient rpcClient() {
        RpcClient client = new RpcClient();
        client.startup();
        return client;
    }
}
