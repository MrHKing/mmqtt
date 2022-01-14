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
package org.monkey.mmq.config.driver;

import org.monkey.mmq.config.matedata.ResourcesMateData;

import java.util.Map;

/**
 * @author solley
 */
public class InfluxDBDriver implements ResourceDriver{
    @Override
    public void addDriver(String resourceId, Map resource) {

    }

    @Override
    public void deleteDriver(String resourceId) {

    }

    @Override
    public Object getDriver(String resourceId) throws Exception {
        return null;
    }

    @Override
    public boolean testConnect(ResourcesMateData resourcesMateData) {
        return false;
    }

    @Override
    public void handle(Map property, ResourcesMateData resourcesMateData,
                       String topic, int qos, String address, String username) {

    }
}
