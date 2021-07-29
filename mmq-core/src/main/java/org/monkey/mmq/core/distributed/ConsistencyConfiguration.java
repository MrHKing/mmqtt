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

package org.monkey.mmq.core.distributed;


import org.monkey.mmq.core.cluster.ServerMemberManager;
import org.monkey.mmq.core.consistency.cp.CPProtocol;
import org.monkey.mmq.core.distributed.raft.JRaftProtocol;
import org.monkey.mmq.core.spi.MmqServiceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;

/**
 * consistency configuration.
 *
 * @author solley
 */
@Configuration
public class ConsistencyConfiguration {

    @Order(1)
    @Bean(value = "strongAgreementProtocol")
    public CPProtocol strongAgreementProtocol(ServerMemberManager memberManager) throws Exception {
        final CPProtocol protocol = getProtocol(CPProtocol.class, () -> new JRaftProtocol(memberManager));
        return protocol;
    }

    private <T> T getProtocol(Class<T> cls, Callable<T> builder) throws Exception {
        Collection<T> protocols = MmqServiceLoader.load(cls);

        // Select only the first implementation

        Iterator<T> iterator = protocols.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return builder.call();
        }
    }

}
