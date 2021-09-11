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

package org.monkey.mmq.rule.engine.supports.distinct;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.*;
import org.monkey.mmq.rule.engine.ReactorQLMetadata;
import org.monkey.mmq.rule.engine.ReactorQLRecord;
import org.monkey.mmq.rule.engine.feature.DistinctFeature;
import org.monkey.mmq.rule.engine.feature.FeatureId;
import org.monkey.mmq.rule.engine.feature.ValueMapFeature;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DefaultDistinctFeature implements DistinctFeature {
    @Override
    public Function<Flux<ReactorQLRecord>, Flux<ReactorQLRecord>> createDistinctMapper(Distinct distinct, ReactorQLMetadata metadata) {

        List<SelectItem> items = distinct.getOnSelectItems();
        if (items == null) {
            return flux -> flux.distinct(ReactorQLRecord::getRecord);
        }
        List<Function<ReactorQLRecord, Mono<Object>>> keySelector = new ArrayList<>();
        for (SelectItem item : items) {
            item.accept(new SelectItemVisitor() {
                @Override
                public void visit(AllColumns allColumns) {
                    keySelector.add(record -> Mono.justOrEmpty(record.getRecord()));
                }

                @Override
                public void visit(AllTableColumns allTableColumns) {
                    String tname = allTableColumns.getTable().getAlias() != null ? allTableColumns.getTable().getAlias().getName() : allTableColumns.getTable().getName();
                    keySelector.add(record -> Mono.justOrEmpty(record.getRecord(tname)));
                }

                @Override
                public void visit(SelectExpressionItem selectExpressionItem) {
                    Expression expr = selectExpressionItem.getExpression();
                    Function<ReactorQLRecord, Publisher<?>> mapper = ValueMapFeature.createMapperNow(expr, metadata);
                    keySelector.add(record -> Mono.from(mapper.apply(record)));
                }
            });
        }
        return createDistinct(keySelector);
    }

    protected Function<Flux<ReactorQLRecord>, Flux<ReactorQLRecord>> createDistinct(List<Function<ReactorQLRecord, Mono<Object>>> keySelector) {
        return flux -> flux
                .flatMap(record -> Flux.fromIterable(keySelector)
                        .flatMap(mapper -> mapper.apply(record))
                        .collectList()
                        .map(list -> Tuples.of(list, record)))
                .distinct(Tuple2::getT1)
                .map(Tuple2::getT2);
    }

    @Override
    public String getId() {
        return FeatureId.Distinct.defaultId.getId();
    }
}
