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

package org.monkey.mmq.rule.engine;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.monkey.mmq.rule.engine.utils.SqlUtils.getCleanStr;

public class DefaultReactorQLContext implements ReactorQLContext {

    private final Function<String, Flux<Object>> supplier;

    private final List<Object> parameter = new ArrayList<>();

    private final Map<String, Object> namedParameter = new HashMap<>();

    private BiFunction<String, Flux<Object>, Flux<Object>> mapper = (s, flux) -> flux;

    public DefaultReactorQLContext(Function<String, ? extends Publisher<?>> supplier) {
        this.supplier = name -> Flux.from(supplier.apply(name));
    }

    @Override
    public Map<String, Object> getParameters() {
        return namedParameter;
    }

    @Override
    public ReactorQLContext bind(Object value) {
        parameter.add(value);
        return this;
    }

    @Override
    public ReactorQLContext bind(int index, Object value) {
        parameter.add(index, value);
        return this;
    }

    @Override
    public ReactorQLContext bind(String name, Object value) {
        if (name != null && value != null) {
            namedParameter.put(name, value);
        }
        return this;
    }

    @Override
    public Flux<Object> getDataSource(String name) {
        name = getCleanStr(name);
        return mapper.apply(name, supplier.apply(name));
    }


    @Override
    public Optional<Object> getParameter(int index) {
        if (parameter.size() <= (index)) {
            return Optional.empty();
        }
        return Optional.ofNullable(parameter.get(index));
    }

    @Override
    public Optional<Object> getParameter(String name) {
        return Optional.ofNullable(namedParameter.get(getCleanStr(name)));
    }

    @Override
    public ReactorQLContext transfer(BiFunction<String, Flux<Object>, Flux<Object>> dataSourceMapper) {
        DefaultReactorQLContext context = new DefaultReactorQLContext(supplier);
        context.mapper = dataSourceMapper;
        return context;
    }
}
