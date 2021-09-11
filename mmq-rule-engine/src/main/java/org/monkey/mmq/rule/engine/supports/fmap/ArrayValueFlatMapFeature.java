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

package org.monkey.mmq.rule.engine.supports.fmap;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import org.apache.commons.collections.CollectionUtils;
import org.monkey.mmq.rule.engine.ReactorQLMetadata;
import org.monkey.mmq.rule.engine.ReactorQLRecord;
import org.monkey.mmq.rule.engine.feature.FeatureId;
import org.monkey.mmq.rule.engine.feature.ValueFlatMapFeature;
import org.monkey.mmq.rule.engine.feature.ValueMapFeature;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.function.BiFunction;

/**
 * select flat_array(arr) arrValue
 */
public class ArrayValueFlatMapFeature implements ValueFlatMapFeature {

    static String ID = FeatureId.ValueFlatMap.of("flat_array").getId();

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public BiFunction<String, Flux<ReactorQLRecord>, Flux<ReactorQLRecord>> createMapper(Expression expression, ReactorQLMetadata metadata) {
        Function function = ((Function) expression);

        if (function.getParameters() == null || CollectionUtils.isEmpty(function.getParameters().getExpressions())) {
            throw new IllegalArgumentException("函数[" + expression + "]参数不能为空");
        }

        Expression expr = function.getParameters().getExpressions().get(0);

        java.util.function.Function<ReactorQLRecord, Publisher<?>> valueMap = ValueMapFeature.createMapperNow(expr, metadata);

        return (alias, flux) -> {

            return flux.flatMap(record -> {
                return Flux
                        .from(valueMap.apply(record))
                        .flatMap(result -> {
                            if (result instanceof Iterable) {
                                return Flux.fromIterable((Iterable) result);
                            }
                            if (result instanceof Object[]) {
                                return Flux.just((Object[]) result);
                            }
                            return Flux.just(result);
                        })
                        .map(v -> {
                            return record.setResult(alias, v);
                        });
            });

        };
    }
}
