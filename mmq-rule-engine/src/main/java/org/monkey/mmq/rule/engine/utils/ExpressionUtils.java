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

package org.monkey.mmq.rule.engine.utils;

import net.sf.jsqlparser.expression.*;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ExpressionUtils {

    public static Optional<Object> getSimpleValue(Expression expr) {
        AtomicReference<Object> ref = new AtomicReference<>();
        expr.accept(new ExpressionVisitorAdapter() {
            @Override
            public void visit(LongValue longValue) {
                ref.set(longValue.getValue());
            }

            @Override
            public void visit(DoubleValue doubleValue) {
                ref.set(doubleValue.getValue());
            }

            @Override
            public void visit(SignedExpression signedExpression) {
                Expression expr = signedExpression.getExpression();
                Number val = getSimpleValue(expr)
                        .map(CastUtils::castNumber)
                        .orElseThrow(() -> new UnsupportedOperationException("unsupported simple expression:" + signedExpression));

                switch (signedExpression.getSign()) {
                    case '-':
                        ref.set(CastUtils.castNumber(val
                                , i -> -i
                                , l -> -l
                                , d -> -d
                                , f -> -f
                                , d -> -d.doubleValue()
                        ));
                        break;
                    case '~':
                        ref.set(~val.longValue());
                        break;
                    default:
                        ref.set(val);
                        break;
                }
            }

            @Override
            public void visit(DateValue dateValue) {
                ref.set(dateValue.getValue());
            }

            @Override
            public void visit(TimeValue timeValue) {
                ref.set(timeValue.getValue());
            }

            @Override
            public void visit(StringValue function) {
                ref.set(function.getValue());
            }

        });

        return Optional.ofNullable(ref.get());
    }

}
