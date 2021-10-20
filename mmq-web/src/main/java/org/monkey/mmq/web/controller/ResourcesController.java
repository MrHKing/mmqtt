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

import org.monkey.mmq.config.matedata.ResourcesMateData;
import org.monkey.mmq.config.service.ResourcesService;
import org.monkey.mmq.core.consistency.model.RestResultUtils;
import org.monkey.mmq.core.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * @author solley
 */
@RestController
@RequestMapping({"/v1/resources"})
public class ResourcesController {

    @Autowired
    ResourcesService resourcesService;

    /**
     * Get resources.
     *
     * @return Current resources
     */
    @GetMapping("/resources")
    public Object getResources() {
        Map<String, ResourcesMateData> mateDataMap = resourcesService.getAllResources();
        return mateDataMap.values();
    }

    /**
     * test connect resources.
     *
     * @return Current resources
     */
    @PostMapping("/testConnect")
    public Object testConnect(@RequestBody ResourcesMateData resourcesMateData) {
        boolean connected = resourcesService.testConnect(resourcesMateData);
        if (!connected) {
            return RestResultUtils.failed();
        } else {
            return RestResultUtils.success("connect resource ok!", null);
        }
    }

    /**
     * Delete an existed resources.
     *
     * @param resourceID
     * @return ok if deleted succeed
     */
    @DeleteMapping
    public Object deleteResource(@RequestParam String resourceID) {
        resourcesService.delete(resourceID);
        return RestResultUtils.success("delete resource ok!", null);
    }

    /**
     * Create a new resources.
     *
     * @param resourcesMateData
     * @return ok if create succeed
     */
    @PostMapping
    public Object saveResource(@RequestBody ResourcesMateData resourcesMateData) {
        if (StringUtils.isEmpty(resourcesMateData.getResourceID())) resourcesMateData.setResourceID(getResourceID());
        resourcesService.save(resourcesMateData.getResourceID(), resourcesMateData);
        return RestResultUtils.success("create resource ok!", null);
    }

    /**
     * 获得资源id
     * @return
     */
    private String getResourceID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-","");
    }
}
