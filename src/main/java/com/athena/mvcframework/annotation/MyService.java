package com.athena.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @Author: xiaoxiang.zhang
 * @Description:服务注解
 * @Date: Create in 10:29 AM 2020/1/7
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyService {

    String value() default "";
}
