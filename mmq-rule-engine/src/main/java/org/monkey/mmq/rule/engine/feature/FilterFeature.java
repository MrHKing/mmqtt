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
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;

import org.monkey.mmq.rule.engine.ReactorQLMetadata;
import org.monkey.mmq.rule.engine.ReactorQLRecord;
import org.monkey.mmq.rule.engine.utils.CastUtils;
import org.monkey.mmq.rule.engine.utils.CompareUtils;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.monkey.mmq.rule.engine.feature.ValueMapFeature.createMapperNow;


/**
 * 过滤器支持,用来根据表达式创建{@link Predicate}
 *
 * @author solley
 * @see org.monkey.mmq.rule.engine.supports.filter.BinaryFilterFeature
 */
public interface FilterFeature extends Feature {

    BiFunction<ReactorQLRecord, Object, Mono<Boolean>> createPredicate(Expression expression, ReactorQLMetadata metadata);

    static Optional<BiFunction<ReactorQLRecord, Object, Mono<Boolean>>> createPredicateByExpression(Expression expression, ReactorQLMetadata metadata) {
        AtomicReference<BiFunction<ReactorQLRecord, Object, Mono<Boolean>>> ref = new AtomicReference<>();
        expression.accept(new org.monkey.mmq.rule.engine.supports.ExpressionVisitorAdapter() {

            @Override
            public void visit(net.sf.jsqlparser.expression.Function function) {
                ref.set(metadata.getFeature(FeatureId.Filter.of(function.getName()))
                        .map(filterFeature -> filterFeature.createPredicate(expression, metadata))
                        .orElseGet(() -> {
                            //尝试使用值转换来判断
                            Function<ReactorQLRecord, Publisher<?>> mapper =
                                    createMapperNow(function, metadata);
                            return (record, o) -> Mono.from(mapper.apply(record))
                                    .map(CastUtils::castBoolean);
                        }));

            }

            @Override
            public void visit(AndExpression expr) {
                metadata.getFeature(FeatureId.Filter.and)
                        .ifPresent(filterFeature -> ref.set(filterFeature.createPredicate(expr, metadata)));
            }

            @Override
            public void visit(CaseExpression expr) {
                Function<ReactorQLRecord, Publisher<?>> mapper = createMapperNow(expr, metadata);
                ref.set((ctx, v) ->
                        Mono.from(mapper.apply(ctx))
                                .map(resp -> CompareUtils.equals(true, resp))
                                .defaultIfEmpty(false));
            }

            @Override
            public void visit(OrExpression expr) {
                metadata.getFeature(FeatureId.Filter.or)
                        .ifPresent(filterFeature -> ref.set(filterFeature.createPredicate(expr, metadata)));
            }

            @Override
            public void visit(Parenthesis value) {
                createPredicateByExpression(value.getExpression(), metadata)
                        .ifPresent(ref::set);
            }

            @Override
            public void visit(Between expr) {
                metadata.getFeature(FeatureId.Filter.between)
                        .ifPresent(filterFeature -> ref.set(filterFeature.createPredicate(expr, metadata)));
            }

            @Override
            public void visit(InExpression expr) {
                metadata.getFeature(FeatureId.Filter.in)
                        .ifPresent(filterFeature -> ref.set(filterFeature.createPredicate(expr, metadata)));
            }

            @Override
            public void visit(LongValue value) {
                long val = value.getValue();
                ref.set((row, column) -> Mono.just(CompareUtils.equals(column, val)));
            }

            @Override
            public void visit(DoubleValue value) {
                double val = value.getValue();
                ref.set((row, column) -> Mono.just(CompareUtils.equals(column, val)));
            }

            @Override
            public void visit(TimestampValue value) {
                Date val = value.getValue();
                ref.set((row, column) -> Mono.just(CompareUtils.equals(column, val)));
            }

            @Override
            public void visit(DateValue value) {
                Date val = value.getValue();
                ref.set((row, column) -> Mono.just(CompareUtils.equals(column, val)));
            }

            @Override
            public void visit(TimeValue value) {
                Date val = value.getValue();
                ref.set((row, column) -> Mono.just(CompareUtils.equals(column, val)));
            }

            @Override
            public void visit(StringValue value) {
                String val = value.getValue();
                ref.set((row, column) -> Mono.just(CompareUtils.equals(column, val)));
            }

            @Override
            public void visit(IsNullExpression value) {
                boolean not = value.isNot();
                Function<ReactorQLRecord, Publisher<?>> expr = createMapperNow(value.getLeftExpression(), metadata);

                ref.set((row, column) -> Flux
                        .from(expr.apply(row))
                        .any(r -> true)
                        .map(r -> not == r)
                );
            }

            @Override
            public void visit(IsBooleanExpression value) {
                boolean not = value.isNot();
                boolean isTrue = value.isTrue();
                Function<ReactorQLRecord, Publisher<?>> mapper = metadata
                        .getFeatureNow(FeatureId.ValueMap.property)
                        .createMapper(value.getLeftExpression(), metadata);
                ref.set((row, column) -> Mono
                        .from(mapper.apply(row))
                        .map(left -> !not == isTrue == CastUtils.castBoolean(left)));
            }

            @Override
            public void visit(Column expr) {
                Function<ReactorQLRecord, Publisher<?>> mapper = metadata.getFeatureNow(FeatureId.ValueMap.property).createMapper(expr, metadata);
                ref.set((row, column) -> Mono.just(CompareUtils.equals(column, mapper.apply(row))));
            }

            @Override
            public void visit(NotExpression notExpression) {
                Function<ReactorQLRecord, Publisher<?>> mapper = createMapperNow(notExpression.getExpression(), metadata);
                ref.set((row, column) -> Mono
                        .from(mapper.apply(row))
                        .cast(Boolean.class)
                        .map(v -> !v));
            }

            @Override
            public void visit(NullValue value) {
                ref.set((row, column) -> Mono.just(column == null));
            }

            @Override
            public void visit(ExistsExpression exists) {
                Function<ReactorQLRecord,  Publisher<?>> mapper = createMapperNow(exists.getRightExpression(), metadata);
                boolean not = exists.isNot();
                ref.set((row, column) -> Flux
                        .from(mapper.apply(row))
                        .any(r -> true)
                        .map(r -> r != not));
            }

            @Override
            public void visit(BinaryExpression expression) {
                metadata.getFeature(FeatureId.Filter.of(expression.getStringExpression()))
                        .ifPresent(filterFeature -> ref.set(filterFeature.createPredicate(expression, metadata)));
                if (ref.get() == null) {
                    metadata.getFeature(FeatureId.ValueMap.of(expression.getStringExpression()))
                            .ifPresent(filterFeature -> {
                                Function<ReactorQLRecord,  Publisher<?>> mapper = filterFeature.createMapper(expression, metadata);
                                ref.set((row, column) -> Mono
                                        .from(mapper.apply(row))
                                        .map(v -> CompareUtils.equals(column, v)));
                            });
                }
            }

            @Override
            public void visit(ComparisonOperator expression) {
                metadata.getFeature(FeatureId.Filter.of(expression.getStringExpression()))
                        .map(feature -> feature.createPredicate(expression, metadata))
                        .ifPresent(ref::set);
            }
        });

        return Optional.ofNullable(ref.get());
    }

    static BiFunction<ReactorQLRecord, Object, Mono<Boolean>> createPredicateNow(Expression whereExpr, ReactorQLMetadata metadata) {
        return createPredicateByExpression(whereExpr, metadata).orElseThrow(() -> new UnsupportedOperationException("不支持的条件:" + whereExpr));
    }
}
