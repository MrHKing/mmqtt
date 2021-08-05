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
package org.monkey.mmq.notifier;

import org.monkey.mmq.core.entity.InternalMessage;
import org.monkey.mmq.core.notify.Event;

/**
 * 发布事件
 *
 * @author solley
 */
public class PublishEvent extends Event {

    public InternalMessage message;

    public InternalMessage getInternalMessage() {
        return message;
    }

    public void setInternalMessage(InternalMessage message) {
        this.message = message;
    }
}
