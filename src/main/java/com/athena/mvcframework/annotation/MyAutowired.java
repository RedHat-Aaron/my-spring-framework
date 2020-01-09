package com.athena.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @Author xiangxz
 * @Description 自动注入注解
 * @Date 9:42 AM 2020/1/8
 */
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyAutowired {
    String value() default "";
}
