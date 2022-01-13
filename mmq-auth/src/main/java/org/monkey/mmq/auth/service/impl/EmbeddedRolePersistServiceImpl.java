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
import org.monkey.mmq.auth.model.RoleInfo;
import org.monkey.mmq.auth.service.RolePersistService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 嵌入角色管理
 *
 * @author solley
 */
@Service
public class EmbeddedRolePersistServiceImpl implements RolePersistService {
    @Override
    public Page<RoleInfo> getRoles(int pageNo, int pageSize) {
        return null;
    }

    @Override
    public Page<RoleInfo> getRolesByUserName(String username, int pageNo, int pageSize) {
        return null;
    }

    @Override
    public void addRole(String role, String userName) {

    }

    @Override
    public void deleteRole(String role) {

    }

    @Override
    public void deleteRole(String role, String username) {

    }

    @Override
    public List<String> findRolesLikeRoleName(String role) {
        return null;
    }
}
