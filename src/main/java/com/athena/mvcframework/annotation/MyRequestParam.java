package com.athena.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @Author xiangxz
 * @Description 请求参数名称注解
 * @Date 9:49 PM 2020/1/7
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestParam {

    String value()default "";
}
