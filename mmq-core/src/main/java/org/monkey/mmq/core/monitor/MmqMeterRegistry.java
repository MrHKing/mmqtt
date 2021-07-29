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

package org.monkey.mmq.core.monitor;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Metrics unified usage center.
 *
 * @author solley
 */
@SuppressWarnings("all")
public final class MmqMeterRegistry {
    
    private static final CompositeMeterRegistry METER_REGISTRY = new CompositeMeterRegistry();
    
    public static DistributionSummary summary(String module, String name) {
        ImmutableTag moduleTag = new ImmutableTag("module", module);
        List<Tag> tags = new ArrayList<>();
        tags.add(moduleTag);
        tags.add(new ImmutableTag("name", name));
        return METER_REGISTRY.summary("mmq_monitor", tags);
    }
    
    public static Timer timer(String module, String name) {
        ImmutableTag moduleTag = new ImmutableTag("module", module);
        List<Tag> tags = new ArrayList<>();
        tags.add(moduleTag);
        tags.add(new ImmutableTag("name", name));
        return METER_REGISTRY.timer("mmq_monitor", tags);
    }
    
}
