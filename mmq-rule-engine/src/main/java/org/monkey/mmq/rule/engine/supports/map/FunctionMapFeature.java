package org.monkey.mmq.rule.engine.supports.map;


import net.sf.jsqlparser.expression.Expression;
import org.monkey.mmq.rule.engine.ReactorQLMetadata;
import org.monkey.mmq.rule.engine.ReactorQLRecord;
import org.monkey.mmq.rule.engine.feature.FeatureId;
import org.monkey.mmq.rule.engine.feature.ValueMapFeature;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FunctionMapFeature implements ValueMapFeature {

    int maxParamSize;
    int minParamSize;

    public Function<Flux<Object>, Publisher<?>> mapper;

    @Override
    public String getId() {
        return id;
    }

    private final String id;

    public FunctionMapFeature(String function, int max, int min, Function<Flux<Object>, Publisher<?>> mapper) {
        this.maxParamSize = max;
        this.minParamSize = min;
        this.mapper = mapper;
        this.id = FeatureId.ValueMap.of(function).getId();
    }

    @Override
    public Function<ReactorQLRecord, Publisher<?>> createMapper(Expression expression, ReactorQLMetadata metadata) {

        net.sf.jsqlparser.expression.Function function = ((net.sf.jsqlparser.expression.Function) expression);

        List<Expression> parameters;
        if (function.getParameters() == null && minParamSize != 0) {
            throw new UnsupportedOperationException("函数[" + expression + "]必须传入参数");
        }
        if (function.getParameters() == null) {
            return v -> mapper.apply(Flux.empty());
        }
        parameters = function.getParameters().getExpressions();
        if (parameters.size() > maxParamSize || parameters.size() < minParamSize) {
            throw new UnsupportedOperationException("函数[" + expression + "]参数数量错误");
        }
        List<Function<ReactorQLRecord, Publisher<?>>> mappers = parameters.stream()
                .map(expr -> ValueMapFeature.createMapperNow(expr, metadata))
                .collect(Collectors.toList());

        return v -> mapper.apply(Flux.fromIterable(mappers).flatMap(mp->mp.apply(v)));
    }
}
