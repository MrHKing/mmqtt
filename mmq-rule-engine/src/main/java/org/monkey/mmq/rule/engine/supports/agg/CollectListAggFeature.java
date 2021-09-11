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

package org.monkey.mmq.rule.engine.supports.agg;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;
import org.apache.commons.collections.CollectionUtils;
import org.monkey.mmq.rule.engine.ReactorQLContext;
import org.monkey.mmq.rule.engine.ReactorQLMetadata;
import org.monkey.mmq.rule.engine.ReactorQLRecord;
import org.monkey.mmq.rule.engine.feature.FeatureId;
import org.monkey.mmq.rule.engine.feature.FromFeature;
import org.monkey.mmq.rule.engine.feature.ValueAggMapFeature;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CollectListAggFeature implements ValueAggMapFeature {

    public static final String ID = FeatureId.ValueAggMap.of("collect_list").getId();


    @Override
    public Function<Flux<ReactorQLRecord>, Flux<Object>> createMapper(Expression expression, ReactorQLMetadata metadata) {

        net.sf.jsqlparser.expression.Function function = ((net.sf.jsqlparser.expression.Function) expression);

        if (function.getParameters() == null || CollectionUtils.isEmpty(function.getParameters().getExpressions())) {
            return flux -> flux.map(ReactorQLRecord::getRecord).collectList().cast(Object.class).flux();
        }
        {
            Expression expr = function.getParameters().getExpressions().get(0);
            if (expr instanceof SubSelect) {
                Function<ReactorQLContext, Flux<ReactorQLRecord>> mapper = FromFeature.createFromMapperByFrom(((SubSelect) expr), metadata);
                return flux -> mapper.apply(ReactorQLContext.ofDatasource((r) -> flux))
                                     .map(ReactorQLRecord::getRecord)
                                     .collectList()
                                     .cast(Object.class)
                                     .flux();
            }
            List<String> columns = function
                    .getParameters()
                    .getExpressions()
                    .stream()
                    .map(c -> {
                        if (c instanceof StringValue) {
                            return ((StringValue) c).getValue();
                        }
                        if (c instanceof Column) {
                            return ((Column) c).getColumnName();
                        }
                        throw new UnsupportedOperationException("不支持的表达式:" + expression);
                    })
                    .collect(Collectors.toList());

            return flux -> flux
                    .map(record -> {
                        Map<String, Object> values = new HashMap<>();
                        for (String column : columns) {
                            Optional.ofNullable(record.asMap())
                                    .map(map -> map.get(column))
                                    .ifPresent(val -> values.put(column, val));
                        }
                        return values;
                    })
                    .collectList()
                    .cast(Object.class).flux()
                    ;
        }

    }

    @Override
    public String getId() {
        return ID;
    }
}
