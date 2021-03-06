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

package org.monkey.mmq.rule.engine.feature;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;
import org.apache.commons.collections.CollectionUtils;
import org.monkey.mmq.rule.engine.ReactorQLMetadata;
import org.monkey.mmq.rule.engine.ReactorQLRecord;
import org.monkey.mmq.rule.engine.utils.CastUtils;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * 值转换支持,用来创建数据转换函数
 *
 * @author solley
 * @since 1.0
 */
public interface ValueMapFeature extends Feature {

    Function<ReactorQLRecord, Publisher<?>> createMapper(Expression expression, ReactorQLMetadata metadata);

    static Function<ReactorQLRecord,   Publisher<?>> createMapperNow(Expression expr, ReactorQLMetadata metadata) {
        return createMapperByExpression(expr, metadata).orElseThrow(() -> new UnsupportedOperationException("不支持的操作:" + expr));
    }

    static Optional<Function<ReactorQLRecord,Publisher<?>>> createMapperByExpression(Expression expr, ReactorQLMetadata metadata) {

        AtomicReference<Function<ReactorQLRecord, Publisher<?>>> ref = new AtomicReference<>();

        expr.accept(new org.monkey.mmq.rule.engine.supports.ExpressionVisitorAdapter() {
            @Override
            public void visit(net.sf.jsqlparser.expression.Function function) {
                metadata.getFeature(FeatureId.ValueMap.of(function.getName()))
                        .ifPresent(feature -> ref.set(feature.createMapper(function, metadata)));
            }

            @Override
            public void visit(SubSelect subSelect) {
                ref.set(metadata.getFeatureNow(FeatureId.ValueMap.select, expr::toString).createMapper(subSelect, metadata));
            }

            @Override
            public void visit(ExistsExpression exists) {
                Function<ReactorQLRecord, Publisher<?>> mapper = createMapperNow(exists.getRightExpression(), metadata);
                boolean not = exists.isNot();
                ref.set((row) -> Flux
                        .from(mapper.apply(row))
                        .any(r -> true)
                        .map(r -> r != not));
            }

            @Override
            public void visit(ArrayExpression arrayExpression) {
                Expression indexExpr = arrayExpression.getIndexExpression();
                Expression objExpr = arrayExpression.getObjExpression();
                Function<ReactorQLRecord, Publisher<?>> objMapper = createMapperNow(objExpr, metadata);
                Function<ReactorQLRecord, Publisher<?>> indexMapper = createMapperNow(indexExpr, metadata);

                ref.set(record ->
                        Mono.zip(
                                Mono.from(indexMapper.apply(record)),
                                Mono.from(objMapper.apply(record))
                        ).flatMap(tp2 ->
                                Mono.justOrEmpty(metadata
                                        .getFeatureNow(PropertyFeature.ID)
                                        .getProperty(tp2.getT1(), tp2.getT2()))));

            }

            @Override
            public void visit(Parenthesis value) {
                createMapperByExpression(value.getExpression(), metadata).ifPresent(ref::set);
            }

            @Override
            public void visit(CaseExpression expr) {
                ref.set(metadata.getFeatureNow(FeatureId.ValueMap.caseWhen, expr::toString).createMapper(expr, metadata));
            }

            @Override
            public void visit(CastExpression expr) {
                ref.set(metadata.getFeatureNow(FeatureId.ValueMap.cast, expr::toString).createMapper(expr, metadata));
            }

            @Override
            public void visit(Column column) {
                ref.set(metadata.getFeatureNow(FeatureId.ValueMap.property, column::toString).createMapper(column, metadata));
            }

            @Override
            public void visit(StringValue value) {
                Mono<Object> val = Mono.just(value.getValue());
                ref.set((v) -> val);
            }

            @Override
            public void visit(LongValue value) {
                Mono<Object> val = Mono.just(value.getValue());
                ref.set((v) -> val);
            }

            @Override
            public void visit(JdbcParameter parameter) {
                int idx = parameter.isUseFixedIndex() ? parameter.getIndex() : parameter.getIndex() - 1;
                ref.set((record) -> Mono.justOrEmpty(record.getContext().getParameter(idx)));
            }

            @Override
            public void visit(NumericBind nullValue) {
                int idx = nullValue.getBindId();
                ref.set((record) -> Mono.justOrEmpty(record.getContext().getParameter(idx)));
            }

            @Override
            public void visit(JdbcNamedParameter parameter) {
                String name = parameter.getName();
                ref.set((record) -> Mono.justOrEmpty(record.getContext().getParameter(name)));
            }

            @Override
            public void visit(DoubleValue value) {
                Mono<Object> val = Mono.just(value.getValue());
                ref.set((v) -> val);
            }

            @Override
            public void visit(DateValue value) {
                Mono<Object> val = Mono.just(value.getValue());
                ref.set((v) -> val);
            }

            @Override
            public void visit(HexValue value) {
                Mono<Object> val = Mono.just(value.getValue());
                ref.set((v) -> val);
            }

            @Override
            public void visit(SignedExpression expr) {
                char sign = expr.getSign();
                Function<ReactorQLRecord, Publisher<?>> mapper = createMapperNow(expr.getExpression(), metadata);
                Function<Number, Number> doSign;
                switch (sign) {
                    case '-':
                        doSign = n -> CastUtils.castNumber(n
                                , i -> -i
                                , l -> -l
                                , d -> -d
                                , f -> -f
                                , d -> -d.doubleValue()
                        );
                        break;
                    case '~':
                        doSign = n -> ~n.longValue();
                        break;
                    default:
                        doSign = Function.identity();
                }
                ref.set(ctx -> Mono.from(mapper.apply(ctx))
                        .map(CastUtils::castNumber)
                        .map(doSign));
            }

            @Override
            public void visit(TimestampValue value) {
                Mono<Object> val = Mono.just(value.getValue());
                ref.set((v) -> val);
            }

            @Override
            public void visit(BinaryExpression jsonExpr) {
                metadata.getFeature(FeatureId.ValueMap.of(jsonExpr.getStringExpression()))
                        .map(feature -> feature.createMapper(expr, metadata))
                        .ifPresent(ref::set);
                if (ref.get() == null) {
                    FilterFeature
                            .createPredicateByExpression(expr, metadata)
                            .<Function<ReactorQLRecord, Publisher<?>>>
                                    map(predicate -> ((ctx) -> predicate.apply(ctx, ctx.getRecord())))
                            .ifPresent(ref::set);
                }
            }
        });

        return Optional.ofNullable(ref.get());
    }

    static Tuple2<Function<ReactorQLRecord, Publisher<?>>, Function<ReactorQLRecord, Publisher<?>>> createBinaryMapper(Expression expression, ReactorQLMetadata metadata) {
        Expression left;
        Expression right;
        if (expression instanceof net.sf.jsqlparser.expression.Function) {
            net.sf.jsqlparser.expression.Function function = ((net.sf.jsqlparser.expression.Function) expression);
            List<Expression> expressions;
            if (function.getParameters() == null || CollectionUtils.isEmpty(expressions = function.getParameters().getExpressions()) || expressions.size() != 2) {
                throw new UnsupportedOperationException("参数数量只能为2:" + expression);
            }
            left = expressions.get(0);
            right = expressions.get(1);
        } else if (expression instanceof BinaryExpression) {
            BinaryExpression bie = ((BinaryExpression) expression);
            left = bie.getLeftExpression();
            right = bie.getRightExpression();
        } else {
            throw new UnsupportedOperationException("不支持的表达式:" + expression);
        }
        Function<ReactorQLRecord, Publisher<?>> leftMapper = createMapperNow(left, metadata);
        Function<ReactorQLRecord, Publisher<?>> rightMapper = createMapperNow(right, metadata);
        return Tuples.of(leftMapper, rightMapper);
    }
}
