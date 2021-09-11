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
import org.apache.commons.collections.CollectionUtils;
import org.monkey.mmq.rule.engine.ReactorQLMetadata;
import org.monkey.mmq.rule.engine.ReactorQLRecord;
import org.monkey.mmq.rule.engine.feature.FeatureId;
import org.monkey.mmq.rule.engine.feature.FilterFeature;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.BiFunction;

public class RangeFilter implements FilterFeature {

    private static final String ID = FeatureId.Filter.of("range").getId();

    @Override
    public BiFunction<ReactorQLRecord, Object, Mono<Boolean>> createPredicate(Expression expression, ReactorQLMetadata metadata) {

        net.sf.jsqlparser.expression.Function function = ((net.sf.jsqlparser.expression.Function) expression);

        List<Expression> expr;
        if (function.getParameters() == null || CollectionUtils.isEmpty(expr = function.getParameters().getExpressions()) || expr.size() != 3) {
            throw new IllegalArgumentException("函数参数数量必须为3:" + function);
        }
        Expression left = expr.get(0);
        Expression between = expr.get(1);
        Expression and = expr.get(2);

        return BetweenFilter.doCreate(left, between, and, metadata, false);
    }

    @Override
    public String getId() {
        return ID;
    }
}
