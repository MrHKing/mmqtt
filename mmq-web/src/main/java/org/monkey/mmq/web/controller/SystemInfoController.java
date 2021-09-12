/*
 * Copyright 2021-2021 Monkey Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.monkey.mmq.web.controller;

import com.google.protobuf.ByteString;
import org.monkey.mmq.core.cluster.Member;
import org.monkey.mmq.core.cluster.ServerMemberManager;
import org.monkey.mmq.core.entity.InternalMessage;
import org.monkey.mmq.core.entity.RejectClient;
import org.monkey.mmq.core.notify.NotifyCenter;
import org.monkey.mmq.core.utils.StringUtils;
import org.monkey.mmq.metadata.message.ClientMateData;
import org.monkey.mmq.metadata.message.SessionMateData;
import org.monkey.mmq.metadata.subscribe.SubscribeMateData;
import org.monkey.mmq.metadata.system.SystemInfoMateData;
import org.monkey.mmq.notifier.BroadcastManager;
import org.monkey.mmq.notifier.PublicEventType;
import org.monkey.mmq.notifier.PublishEvent;
import org.monkey.mmq.service.SessionStoreService;
import org.monkey.mmq.service.SubscribeStoreService;
import org.monkey.mmq.service.SystemInfoStoreService;
import org.monkey.mmq.core.consistency.model.ResponsePage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author solley
 */
@RestController
@RequestMapping({"/v1/system"})
public class SystemInfoController {

    @Autowired
    SystemInfoStoreService systemInfoStoreService;

    @Autowired
    SessionStoreService sessionStoreService;

    @Autowired
    SubscribeStoreService subscribeStoreService;

    @Autowired
    @Qualifier("serverMemberManager")
    ServerMemberManager memberManager;

    /**
     * Get system info.
     *
     * @return Current system info
     */
    @GetMapping("/info")
    public Object getSystemInfo() {
        SystemInfoMateData systemInfoMateData = systemInfoStoreService.getSystemInfo();
        long contextTime = System.currentTimeMillis() - systemInfoMateData.getSystemRunTime();
        systemInfoMateData.setSystemRunTime(contextTime);
        return systemInfoMateData;
    }

    /**
     * Get system connect clients.
     *
     * @return system connect clients
     */
    @GetMapping("/clients")
    public ResponsePage<ClientMateData> getClients(@RequestParam int pageNo, @RequestParam int pageSize,
                                                   @RequestParam(required = false, defaultValue = "") String clientId,
                                                   @RequestParam(required = false, defaultValue = "") String address) {
        Collection<ClientMateData> datas = sessionStoreService.getClients();
        return new ResponsePage<>(pageSize, pageNo,
                datas.size() / pageSize,
                datas.size(),
                datas.stream().filter(x -> x.getClientId().contains(clientId) && x.getAddress().contains(address))
                        .skip(pageNo - 1).limit(pageSize).collect(Collectors.toList()));
    }

    /**
     * Get system connect clients.
     *
     * @return system connect clients
     */
    @GetMapping("/subscribes")
    public ResponsePage<SubscribeMateData> getSubscribes(@RequestParam int pageNo, @RequestParam int pageSize,
                                                   @RequestParam(required = false, defaultValue = "") String clientId,
                                                   @RequestParam(required = false, defaultValue = "") String topic) {
        List<SubscribeMateData> subscribes = subscribeStoreService.getSubscribes();
        return new ResponsePage<>(pageSize, pageNo,
                subscribes.size() / pageSize,
                subscribes.size(),
                subscribes.stream().filter(x -> x.getClientId().contains(clientId) && x.getTopicFilter().contains(topic))
                        .skip(pageNo - 1).limit(pageSize).collect(Collectors.toList()));
    }

    @GetMapping("/rejectClient")
    public void rejectClient(@RequestParam String clinetId) {
        sessionStoreService.rejectClient(clinetId);
    }

    /**
     * Get system nodes.
     *
     * @return system connect nodes
     */
    @GetMapping("/nodes")
    public Object getNodes() {
        return memberManager.getServerList().values();
    }
}
