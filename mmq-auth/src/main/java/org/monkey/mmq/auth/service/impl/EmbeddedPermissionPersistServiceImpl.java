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
package org.monkey.mmq.auth.service.impl;

import org.monkey.mmq.auth.model.Page;
import org.monkey.mmq.auth.model.PermissionInfo;
import org.monkey.mmq.auth.service.PermissionPersistService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * 嵌入权限管理
 *
 * @author solley
 */
@Service
public class EmbeddedPermissionPersistServiceImpl implements PermissionPersistService {
    @Override
    public Page<PermissionInfo> getPermissions(String role, int pageNo, int pageSize) {
        return null;
    }

    @Override
    public void addPermission(String role, String resource, String action) {

    }

    @Override
    public void deletePermission(String role, String resource, String action) {

    }
}
