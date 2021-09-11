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


package org.monkey.mmq.rule.engine.supports;

import org.apache.commons.beanutils.PropertyUtils;
import org.monkey.mmq.rule.engine.feature.PropertyFeature;
import org.monkey.mmq.rule.engine.supports.map.CastFeature;
import org.monkey.mmq.rule.engine.utils.CastUtils;
import org.monkey.mmq.rule.engine.utils.SqlUtils;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class DefaultPropertyFeature implements PropertyFeature {

    public static final DefaultPropertyFeature GLOBAL = new DefaultPropertyFeature();

    @Override
    public Optional<Object> getProperty(Object property, Object value) {
        if (value == null) {
            return Optional.empty();
        }
        if (property instanceof String) {
            property = SqlUtils.getCleanStr((String) property);
        }
        if ("this".equals(property) || "$".equals(property) || "*".equals(property)) {
            return Optional.of(value);
        }

        if (property instanceof Number) {
            int index = ((Number) property).intValue();
            return Optional.ofNullable(CastUtils.castArray(value).get(index));
        }
        Function<Object, Object> mapper = Function.identity();
        String strProperty = String.valueOf(property);
        if (strProperty.contains("::")) {
            String[] cast = strProperty.split("::");

            strProperty = cast[0];
            mapper = v -> CastFeature.castValue(v, cast[1]);
        }

        Object direct = doGetProperty(strProperty, value);
        if (direct != null) {
            return Optional.of(direct).map(mapper);
        }
        Object tmp = value;
        String[] props = strProperty.split("[.]", 2);
        if (props.length <= 1) {
            return Optional.empty();
        }
        while (props.length > 1) {
            tmp = doGetProperty(props[0], tmp);
            if (tmp == null) {
                return Optional.empty();
            }
            Object fast = doGetProperty(props[1], tmp);
            if (fast != null) {
                return Optional.of(fast).map(mapper);
            }
            if (props[1].contains(".")) {
                props = props[1].split("[.]", 2);
            } else {
                return Optional.empty();
            }
        }
        return Optional.of(tmp).map(mapper);
    }

    protected Object doGetProperty(String property, Object value) {
        if ("this".equals(property) || "$".equals(property)) {
            return value;
        }
        if (value instanceof Map) {
            return ((Map<?, ?>) value).get(property);
        }
        try {
            return PropertyUtils.getProperty(value, property);
        } catch (Exception e) {
//            log.warn("get property error", e);
        }
        return null;

    }


}
