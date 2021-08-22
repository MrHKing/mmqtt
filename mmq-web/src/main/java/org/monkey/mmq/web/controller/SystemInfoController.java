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

import org.monkey.mmq.metadata.message.ClientMateData;
import org.monkey.mmq.metadata.system.SystemInfoMateData;
import org.monkey.mmq.service.SessionStoreService;
import org.monkey.mmq.service.SystemInfoStoreService;
import org.monkey.mmq.core.consistency.model.ResponsePage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Health Controller.
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
    public ResponsePage<ClientMateData> getClients(@RequestParam int pageNo, @RequestParam int pageSize) {
        Collection<ClientMateData> datas = sessionStoreService.getClients();
        return new ResponsePage<>(pageSize, pageNo,
                datas.size() / pageSize,
                datas.size(),
                datas.stream().skip(pageNo - 1).limit(pageSize).collect(Collectors.toList()));
    }
}
