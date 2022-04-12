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


package org.monkey.mmq.auth.service;

import org.monkey.mmq.auth.exception.AccessException;
import org.monkey.mmq.auth.model.Permission;
import org.monkey.mmq.auth.model.User;
import org.monkey.mmq.core.exception.MmqException;

/**
 * 用户和密码认证服务接口
 * @author Solley
 */
public interface IMqttAuthService {

    /**
     * 验证用户名和密码是否正确
     */
    boolean checkValid(String username, String password) throws MmqException;

    /**
     * Authentication of request, identify the user who request the resource.
     *
     * @param request where we can find the user information
     * @return user related to this request, null if no user info is found.
     * @throws AccessException if authentication is failed
     */
    User login(Object request) throws AccessException;

    /**
     * Authentication of request, identify the user who request the resource.
     *
     * @param request where we can find the user information
     * @return user related to this request, null if no user info is found.
     * @throws AccessException if authentication is failed
     */
    User loginRemote(Object request) throws AccessException;

    /**
     * Authorization of request, constituted with resource and user.
     *
     * @param permission permission to auth
     * @param user       user who wants to access the resource.
     * @throws AccessException if authorization is failed
     */
    void auth(Permission permission, User user) throws AccessException;
}
