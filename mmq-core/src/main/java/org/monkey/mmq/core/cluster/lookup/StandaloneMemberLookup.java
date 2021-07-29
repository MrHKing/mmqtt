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

package org.monkey.mmq.core.cluster.lookup;


import org.monkey.mmq.core.cluster.AbstractMemberLookup;
import org.monkey.mmq.core.cluster.MemberUtil;
import org.monkey.mmq.core.env.EnvUtil;
import org.monkey.mmq.core.utils.InetUtils;

import java.util.Collections;

/**
 * Member node addressing mode in stand-alone mode.
 *
 * @author solley
 */
public class StandaloneMemberLookup extends AbstractMemberLookup {
    
    @Override
    public void doStart() {
        String url = InetUtils.getSelfIP() + ":" + EnvUtil.getPort();
        afterLookup(MemberUtil.readServerConf(Collections.singletonList(url)));
    }
    
    @Override
    public boolean useAddressServer() {
        return false;
    }
}
