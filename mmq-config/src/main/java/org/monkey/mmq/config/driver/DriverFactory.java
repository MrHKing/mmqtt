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

import org.monkey.mmq.config.matedata.ResourceEnum;
import org.monkey.mmq.core.utils.ApplicationUtils;
import org.monkey.mmq.core.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * @author solley
 */
public class DriverFactory {
    public static ResourceDriver getResourceDriverByEnum(ResourceEnum resourceEnum) {
        return (ResourceDriver) ApplicationUtils.getBean(resourceEnum.getName());
    }

    public static void setProperty(Map property, String topic, String username) {
        property.put("uuid", UUID.randomUUID().toString());

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        property.put("date", sdf.format(date));
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        property.put("datetime", sdf.format(date));
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        property.put("utc", sdf.format(date));
        property.put("timestamp", date.getTime());
        if (StringUtils.isNotEmpty(username)) {
            property.put("username", username);
        }

        if (StringUtils.isEmpty(topic)) return;
        property.put("topic", topic);

        String[] topics = topic.split("/");
        if (topics == null) return;

        for (int i = 0; i < topics.length; i++) {
            property.put("topic" + i, topics[i]);
        }
    }
}
