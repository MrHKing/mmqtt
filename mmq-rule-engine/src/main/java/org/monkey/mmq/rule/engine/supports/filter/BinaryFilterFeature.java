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

package org.monkey.mmq.rule.engine.supports.filter;

import net.sf.jsqlparser.expression.Expression;
import org.monkey.mmq.rule.engine.ReactorQLMetadata;
import org.monkey.mmq.rule.engine.ReactorQLRecord;
import org.monkey.mmq.rule.engine.feature.FeatureId;
import org.monkey.mmq.rule.engine.feature.FilterFeature;
import org.monkey.mmq.rule.engine.feature.ValueMapFeature;
import org.monkey.mmq.rule.engine.utils.CastUtils;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class BinaryFilterFeature implements FilterFeature {

    @Override
    public String getId() {
        return id;
    }

    private final String id;

    public BinaryFilterFeature(String type) {
        this.id = FeatureId.Filter.of(type).getId();
    }

    @Override
    public BiFunction<ReactorQLRecord, Object, Mono<Boolean>> createPredicate(Expression expression, ReactorQLMetadata metadata) {
        Tuple2<Function<ReactorQLRecord,Publisher<?>>,
                Function<ReactorQLRecord,Publisher<?>>> tuple2 = ValueMapFeature.createBinaryMapper(expression, metadata);

        Function<ReactorQLRecord, Publisher<?>> leftMapper = tuple2.getT1();
        Function<ReactorQLRecord,  Publisher<?>> rightMapper = tuple2.getT2();

        return (row, column) -> Mono.zip(Mono.from(leftMapper.apply(row)), Mono.from(rightMapper.apply(row)), this::test).defaultIfEmpty(false);
    }

    protected boolean test(Object left, Object right) {
        try {
            if (left instanceof Map && ((Map<?, ?>) left).size() == 1) {
                left = ((Map<?, ?>) left).values().iterator().next();
            }
            if (right instanceof Map && ((Map<?, ?>) right).size() == 1) {
                right = ((Map<?, ?>) right).values().iterator().next();
            }
            if (left instanceof Date || right instanceof Date || left instanceof LocalDateTime || right instanceof LocalDateTime || left instanceof Instant || right instanceof Instant) {
                return doTest(CastUtils.castDate(left), CastUtils.castDate(right));
            }
            if (left instanceof Number || right instanceof Number) {
                return doTest(CastUtils.castNumber(left), CastUtils.castNumber(right));
            }
            if (left instanceof String || right instanceof String) {
                return doTest(String.valueOf(left), String.valueOf(right));
            }
            return doTest(left, right);
        }catch (Throwable e){
            return false;
        }
    }

    protected abstract boolean doTest(Number left, Number right);

    protected abstract boolean doTest(Date left, Date right);

    protected abstract boolean doTest(String left, String right);

    protected abstract boolean doTest(Object left, Object right);

}
