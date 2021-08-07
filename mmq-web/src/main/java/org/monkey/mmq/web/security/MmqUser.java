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

package org.monkey.mmq.web.security;

import org.monkey.mmq.auth.model.User;

/**
 * Mmq User.
 *
 * @author solley
 */
public class MmqUser extends User {
    
    private String token;
    
    private boolean globalAdmin = false;
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public boolean isGlobalAdmin() {
        return globalAdmin;
    }
    
    public void setGlobalAdmin(boolean globalAdmin) {
        this.globalAdmin = globalAdmin;
    }
    
    @Override
    public String toString() {
        return "MmqUser{" + "token='" + token + '\'' + ", globalAdmin=" + globalAdmin + '}';
    }
}
