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

import org.monkey.mmq.config.modules.ModelMateData;
import org.monkey.mmq.config.service.ModulesService;
import org.monkey.mmq.core.consistency.model.RestResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author solley
 */
@RestController
@RequestMapping({"/v1/modules"})
public class ModulesController {

    @Autowired
    ModulesService modulesService;

    /**
     * Get modules.
     *
     * @return Current modules
     */
    @GetMapping("/modules")
    public Object getModules() {
        return modulesService.listModelMateData();
    }

    /**
     * update module.
     *
     * @param modelMateData
     * @return ok if create succeed
     */
    @PutMapping
    public Object update(@RequestBody ModelMateData modelMateData) {
        if (modelMateData == null) return RestResultUtils.success("modelMateData must nut null", null);
        modulesService.update(modelMateData);
        return RestResultUtils.success("update modules ok!", null);
    }
}
