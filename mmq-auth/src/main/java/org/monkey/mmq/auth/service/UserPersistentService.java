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

import org.monkey.mmq.auth.model.Page;
import org.monkey.mmq.auth.model.User;
import org.monkey.mmq.auth.persistent.KeyBuilder;
import org.monkey.mmq.auth.persistent.UtilsAndCommons;
import org.monkey.mmq.auth.utils.Loggers;
import org.monkey.mmq.core.consistency.matedata.RecordListener;
import org.monkey.mmq.core.consistency.persistent.ConsistencyService;
import org.monkey.mmq.core.exception.MmqException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 嵌入用户管理
 *
 * @author solley
 */
@Service
public class UserPersistentService implements RecordListener<User> {

    @Resource(name = "authPersistentConsistencyServiceDelegateImpl")
    private ConsistencyService consistencyService;

    private ConcurrentHashMap<String, User> userConcurrentHashMap = new ConcurrentHashMap<>();

    public UserPersistentService() {
        User user = new User();
        user.setUsername("mmq");
        user.setPassword("$2a$10$VxvrQ0Omo9ilSFjFwJKE5.7AVg0ug6.dMS.TVQBxbnuNkzuDDQdCS");
        userConcurrentHashMap.put(UtilsAndCommons.AUTH_STORE + user.getUsername(), user);
    }

    /**
     * Init
     */
    @PostConstruct
    public void init() {
        try {
            consistencyService.listen(KeyBuilder.getAuthKey(), this);
        } catch (MmqException e) {
            Loggers.AUTH.error("listen auth service failed.", e);
        }
    }

    public User findUserByUsername(String username) {
        return userConcurrentHashMap.get(UtilsAndCommons.AUTH_STORE + username);
    }

    public void updateUserPassword(String username, String password) {
        User user = userConcurrentHashMap.get(UtilsAndCommons.AUTH_STORE + username);
        user.setPassword(password);
        try {
            consistencyService.put(UtilsAndCommons.AUTH_STORE + username, user);
        } catch (MmqException e) {
            Loggers.AUTH.error(e.getErrMsg());
        }
    }

    public ConcurrentHashMap getUsersFromDatabase() {
        return userConcurrentHashMap;
    }

    @Override
    public boolean interests(String key) {
        return KeyBuilder.matchAuthKey(key);
    }

    @Override
    public boolean matchUnlistenKey(String key) {
        return KeyBuilder.matchAuthKey(key);
    }

    @Override
    public void onChange(String key, User value) throws Exception {
        userConcurrentHashMap.put(key, value);
    }

    @Override
    public void onDelete(String key) throws Exception {
        userConcurrentHashMap.remove(key);
    }
}
