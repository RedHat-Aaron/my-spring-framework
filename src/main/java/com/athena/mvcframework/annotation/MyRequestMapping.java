package com.athena.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @Author xiangxz
 * @Description 请求地址注解
 * @Date 9:49 PM 2020/1/7
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestMapping {

    String value()default "";
}
