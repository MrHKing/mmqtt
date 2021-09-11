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

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalculateUtils {

    public static long bitAnd(Number left, Number right) {
        return left.longValue() & right.longValue();
    }

    public static long bitOr(Number left, Number right) {
        return left.longValue() | right.longValue();
    }

    public static long bitMutex(Number left, Number right) {
        return left.longValue() ^ right.longValue();
    }

    public static long bitCount(Number left) {
        return Long.bitCount(left.longValue());
    }

    public static long leftShift(Number left, Number right) {
        return left.longValue() << right.longValue();
    }

    public static long unsignedRightShift(Number left, Number right) {
        return left.longValue() >>> right.longValue();
    }

    public static long rightShift(Number left, Number right) {
        return left.longValue() >> right.longValue();
    }

    public static long bitNot(Number left) {
        return ~left.longValue();
    }

    public static Number mod(Number left, Number right) {
        if (left instanceof Double
                || left instanceof Float) {
            return left.doubleValue() % right.doubleValue();
        }

        if (left instanceof BigDecimal && right instanceof BigDecimal) {
            return ((BigDecimal) left).remainder(((BigDecimal) right));
        }

        return left.longValue() % right.longValue();
    }

    public static Number division(Number left, Number right) {
        if (left instanceof Double
                || left instanceof Float) {
            return left.doubleValue() / right.doubleValue();
        }

        if (left instanceof BigDecimal && right instanceof BigDecimal) {
            return ((BigDecimal) left).divide(((BigDecimal) right), RoundingMode.HALF_UP);
        }

        return left.longValue() / right.longValue();
    }

    public static Number multiply(Number left, Number right) {
        if (left instanceof Double
                || left instanceof Float) {
            return left.doubleValue() * right.doubleValue();
        }

        if (left instanceof BigDecimal && right instanceof BigDecimal) {
            return ((BigDecimal) left).multiply(((BigDecimal) right));
        }

        return left.longValue() * right.longValue();
    }

    public static Number add(Number left, Number right) {
        if (left instanceof Double
                || left instanceof Float) {
            return left.doubleValue() + right.doubleValue();
        }

        if (left instanceof BigDecimal && right instanceof BigDecimal) {
            return ((BigDecimal) left).add(((BigDecimal) right));
        }

        return left.longValue() + right.longValue();
    }

    public static Number subtract(Number left, Number right) {
        if (left instanceof Double
                || left instanceof Float) {
            return left.doubleValue() - right.doubleValue();
        }

        if (left instanceof BigDecimal && right instanceof BigDecimal) {
            return ((BigDecimal) left).subtract(((BigDecimal) right));
        }

        return left.longValue() - right.longValue();
    }

}
