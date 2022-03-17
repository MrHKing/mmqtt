package org.monkey.mmq.notifier.processor;

import com.alipay.remoting.rpc.RpcServer;
import org.monkey.mmq.core.cluster.ServerMemberManager;
import org.monkey.mmq.service.DupPublishMessageStoreService;
import org.monkey.mmq.service.SessionStoreService;
import org.monkey.mmq.service.SubscribeStoreService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @ClassNameRpcService
 * @Description
 * @Author Solley
 * @Date2022/3/17 13:08
 * @Version V1.0
 **/
@Service
public class RpcService {

    private final ServerMemberManager memberManager;
    private final SubscribeStoreService subscribeStoreService;
    private final SessionStoreService sessionStoreService;
    private final DupPublishMessageStoreService dupPublishMessageStoreService;
    private RpcServer rpcServer;
    public RpcService(ServerMemberManager memberManager,
                      SubscribeStoreService subscribeStoreService,
                      SessionStoreService sessionStoreService,
                      DupPublishMessageStoreService dupPublishMessageStoreService) {
        this.memberManager = memberManager;
        this.subscribeStoreService = subscribeStoreService;
        this.sessionStoreService = sessionStoreService;
        this.dupPublishMessageStoreService = dupPublishMessageStoreService;
    }

    @PostConstruct
    public void init() {
        rpcServer = new RpcServer(this.memberManager.getSelf().getPort() + 10);
        rpcServer.registerUserProcessor(new PublishRequestProcessor(
                this.memberManager.getSelf(),
                subscribeStoreService,
                sessionStoreService,
                dupPublishMessageStoreService));
        rpcServer.registerUserProcessor(new RejectClientProcessor(sessionStoreService));
        rpcServer.startup();
    }

    @PreDestroy
    public void destroy() {
        if (rpcServer != null) {
            rpcServer.shutdown();
        }
    }
}
