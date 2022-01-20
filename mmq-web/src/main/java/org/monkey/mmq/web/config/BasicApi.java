package org.monkey.mmq.web.config;

import java.lang.annotation.*;

/**
 * @ClassNameBasicApi
 * @Description HTTP API 使用Basic认证方式，id 和 password 须分别填写 AppID 和 AppSecret
 * @Author Solley
 * @Date2022/1/19 12:43
 * @Version V1.0
 **/
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BasicApi {
}
