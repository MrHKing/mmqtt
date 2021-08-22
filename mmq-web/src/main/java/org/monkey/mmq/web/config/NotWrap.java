package org.monkey.mmq.web.config;

import java.lang.annotation.*;

/**
 * 对spring mvc controller返回的结果不进行封装，直接以原生结果返回
 * @author solley
 * @date 2018年4月3日 上午9:10:41 123123123
 * @version 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NotWrap {
    String value() default "";
}
