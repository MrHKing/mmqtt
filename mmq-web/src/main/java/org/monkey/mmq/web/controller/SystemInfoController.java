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

import io.netty.util.internal.StringUtil;
import org.monkey.mmq.core.cluster.ServerMemberManager;
import org.monkey.mmq.core.actor.metadata.message.ClientMateData;
import org.monkey.mmq.core.actor.metadata.subscribe.SubscribeMateData;
import org.monkey.mmq.core.actor.metadata.system.SystemInfoMateData;
import org.monkey.mmq.service.SessionStoreService;
import org.monkey.mmq.service.SubscribeStoreService;
import org.monkey.mmq.service.SystemInfoStoreService;
import org.monkey.mmq.core.consistency.model.ResponsePage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
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
    public Object getClients(@RequestParam int pageNo, @RequestParam int pageSize,
                             @RequestParam(required = false, defaultValue = "") String clientId,
                             @RequestParam(required = false, defaultValue = "") String address,
                             @RequestParam(required = false, defaultValue = "") String user,
                             @RequestParam(required = false, defaultValue = "") String topic,
                             HttpServletRequest request) {
        Collection<ClientMateData> datas = sessionStoreService.getClients();
        if (!StringUtil.isNullOrEmpty(topic)) {
            List<SubscribeMateData> subscribeMateDataList = subscribeStoreService.getSubscribes();
            Map<String, ClientMateData> clientMateDataMap = new HashMap<>();
            Collection<ClientMateData> finalDatas = datas;
            subscribeMateDataList.stream().filter(x -> x.getTopicFilter().contains(topic)).forEach(sub -> {
                Optional<ClientMateData> optionalClientMateData = finalDatas.stream().filter(clinet->clinet.getClientId().equals(sub.getClientId())).findFirst();
                if (optionalClientMateData.isPresent()) {
                    clientMateDataMap.put(sub.getClientId(), optionalClientMateData.get());
                }

            });

            datas = clientMateDataMap.values();
        }

        return new ResponsePage<>(pageSize, pageNo,
                datas.size(),
                datas.size() / pageSize,
                datas.stream().filter(x -> x.getClientId().contains(clientId)
                        && x.getAddress().contains(address) && (x.getUser() == null || x.getUser().contains(user)))
                        .skip((pageNo - 1) * pageSize).limit(pageSize).collect(Collectors.toList()));
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
                subscribes.size(),
                subscribes.size() / pageSize,
                subscribes.stream().filter(x -> x.getClientId().contains(clientId) && x.getTopicFilter().contains(topic))
                        .skip((pageNo - 1) * pageSize).limit(pageSize).collect(Collectors.toList()));
    }

    /**
     * Get system connect clients.
     *
     * @return system connect clients
     */
    @GetMapping("/search/subscribes")
    public List<SubscribeMateData> searchSubscribes(@RequestParam String topic) {
        List<SubscribeMateData> subscribes = subscribeStoreService.search(topic);
        return subscribes;
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
