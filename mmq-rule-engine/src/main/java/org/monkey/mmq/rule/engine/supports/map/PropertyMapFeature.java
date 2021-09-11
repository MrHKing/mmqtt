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

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import org.monkey.mmq.rule.engine.ReactorQLMetadata;
import org.monkey.mmq.rule.engine.ReactorQLRecord;
import org.monkey.mmq.rule.engine.feature.FeatureId;
import org.monkey.mmq.rule.engine.feature.PropertyFeature;
import org.monkey.mmq.rule.engine.feature.ValueMapFeature;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class PropertyMapFeature implements ValueMapFeature {

    private static final String ID = FeatureId.ValueMap.property.getId();

    @Override
    public Function<ReactorQLRecord, Publisher<?>> createMapper(Expression expression, ReactorQLMetadata metadata) {
        Column column = ((Column) expression);
        String[] fullName = column.getFullyQualifiedName().split("[.]", 2);

        String name = fullName.length == 2 ? fullName[1] : fullName[0];
        String tableName = fullName.length == 1 ? "this" : fullName[0];

        PropertyFeature feature = metadata.getFeatureNow(PropertyFeature.ID);

        return ctx -> Mono.justOrEmpty(ctx.getRecord(tableName))
                .flatMap(record -> Mono.justOrEmpty(feature.getProperty(name, record)))
                .switchIfEmpty(Mono.fromSupplier(() -> feature.getProperty(name, ctx.asMap()).orElse(null)))
                .switchIfEmpty(Mono.justOrEmpty(ctx.getRecord(name)))
                ;
    }

    @Override
    public String getId() {
        return ID;
    }
}
