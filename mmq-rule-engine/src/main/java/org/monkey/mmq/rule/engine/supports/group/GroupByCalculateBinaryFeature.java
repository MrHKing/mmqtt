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

package org.monkey.mmq.rule.engine.supports.group;


import org.monkey.mmq.rule.engine.utils.CastUtils;

import java.util.function.BiFunction;

/**
 * 根据数学运算来分组
 *
 * @author solley
 * @since 1.0
 */
public class GroupByCalculateBinaryFeature extends GroupByBinaryFeature {

    public GroupByCalculateBinaryFeature(String type, BiFunction<Number, Number, Object> mapper) {

        super(type, (left, right) -> mapper.apply(CastUtils.castNumber(left), CastUtils.castNumber(right)));
    }


}
