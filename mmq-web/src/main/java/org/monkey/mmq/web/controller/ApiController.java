package org.monkey.mmq.web.controller;

import io.netty.util.internal.StringUtil;
import org.monkey.mmq.core.consistency.model.ResponsePage;
import org.monkey.mmq.core.consistency.model.RestResultUtils;
import org.monkey.mmq.core.actor.metadata.message.ClientMateData;
import org.monkey.mmq.core.actor.metadata.subscribe.SubscribeMateData;
import org.monkey.mmq.service.SessionStoreService;
import org.monkey.mmq.service.SubscribeStoreService;
import org.monkey.mmq.web.config.BasicApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassNameApiController
 * @Description
 * @Author Solley
 * @Date2022/1/19 11:59
 * @Version V1.0
 **/
@RestController
@RequestMapping("/v1/api")
public class ApiController {

    @Autowired
    SessionStoreService sessionStoreService;

    @Autowired
    SubscribeStoreService subscribeStoreService;

    /**
     * Get system connect clients.
     *
     * @return system connect clients
     */
    @BasicApi
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
                        && x.getAddress().contains(address) && x.getUser().contains(user))
                        .skip((pageNo - 1) * pageSize).limit(pageSize).collect(Collectors.toList()));
    }

    /**
     * reject connect client.
     */
    @BasicApi
    @GetMapping("/rejectClient")
    public Object rejectClient(@RequestParam String clientId, HttpServletRequest request) {
        sessionStoreService.rejectClient(clientId);
        return RestResultUtils.success();
    }

}
