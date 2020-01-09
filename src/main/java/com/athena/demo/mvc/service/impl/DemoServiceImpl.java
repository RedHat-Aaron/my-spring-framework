package com.athena.demo.mvc.service.impl;

import com.athena.demo.mvc.service.IDemoService;
import com.athena.mvcframework.annotation.MyService;

/**
 * @Author: xiaoxiang.zhang
 * @Description:测试接口实现类
 * @Date: Create in 8:28 PM 2020/1/6
 */
@MyService
public class DemoServiceImpl implements IDemoService {

    @Override
    public String get(String name) {
        return "My name is" + name;
    }
}
