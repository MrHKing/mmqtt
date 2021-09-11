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


package org.monkey.mmq.rule.engine.supports.map;

import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.Expression;
import org.monkey.mmq.rule.engine.ReactorQLMetadata;
import org.monkey.mmq.rule.engine.ReactorQLRecord;
import org.monkey.mmq.rule.engine.feature.FeatureId;
import org.monkey.mmq.rule.engine.feature.ValueMapFeature;
import org.monkey.mmq.rule.engine.utils.CastUtils;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.function.Function;


public class CastFeature implements ValueMapFeature {

    private final static String ID = FeatureId.ValueMap.of("cast").getId();

    @Override
    public Function<ReactorQLRecord, Publisher<?>> createMapper(Expression expression, ReactorQLMetadata metadata) {
        CastExpression cast = ((CastExpression) expression);

        Expression left = cast.getLeftExpression();

        String type = cast.getType().getDataType().toLowerCase();

        Function<ReactorQLRecord, Publisher<?>> mapper = ValueMapFeature.createMapperNow(left, metadata);

        return ctx -> Mono.from(mapper.apply(ctx)).map(value -> castValue(value, type));
    }

    public static Object castValue(Object val, String type) {

        switch (type) {
            case "string":
            case "varchar":
                return CastUtils.castString(val);
            case "number":
            case "decimal":
                return new BigDecimal(CastUtils.castString(val));
            case "int":
            case "integer":
                return CastUtils.castNumber(val).intValue();
            case "long":
                return CastUtils.castNumber(val).longValue();
            case "double":
                return CastUtils.castNumber(val).doubleValue();
            case "bool":
            case "boolean":
                return CastUtils.castBoolean(val);
            case "byte":
                return CastUtils.castNumber(val).byteValue();
            case "float":
                return CastUtils.castNumber(val).floatValue();
            case "date":
                return CastUtils.castDate(val);
            default:
                return val;
        }
    }

    @Override
    public String getId() {
        return ID;
    }
}
