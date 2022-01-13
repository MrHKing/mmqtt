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
import org.monkey.mmq.auth.model.User;
import org.monkey.mmq.auth.service.UserPersistService;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 嵌入用户管理
 *
 * @author solley
 */
@Service
public class EmbeddedUserPersistServiceImpl implements UserPersistService {
    @Override
    public void createUser(String username, String password) {

    }

    @Override
    public void deleteUser(String username) {

    }

    @Override
    public void updateUserPassword(String username, String password) {

    }

    @Override
    public User findUserByUsername(String username) {
        return null;
    }

    @Override
    public Page<User> getUsers(int pageNo, int pageSize) {
        Page<User> userPage = new Page<>();
        List<User> users = new ArrayList<>();
        User user = new User();
        user.setUsername("mmq");
        user.setPassword("$2a$10$VxvrQ0Omo9ilSFjFwJKE5.7AVg0ug6.dMS.TVQBxbnuNkzuDDQdCS");
        users.add(user);
        userPage.setPageItems(users);
        userPage.setTotalCount(1);
        return userPage;
    }

    @Override
    public List<String> findUserLikeUsername(String username) {
        return null;
    }
}
