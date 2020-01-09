package com.athena.demo.mvc.action;

import com.athena.demo.mvc.service.IDemoService;
import com.athena.mvcframework.annotation.MyAutowired;
import com.athena.mvcframework.annotation.MyController;
import com.athena.mvcframework.annotation.MyRequestMapping;
import com.athena.mvcframework.annotation.MyRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author: xiaoxiang.zhang
 * @Description:
 * @Date: Create in 7:52 PM 2020/1/7
 */
@MyController
@MyRequestMapping("/demo")
public class DemoAction {

    @MyAutowired
    private IDemoService demoService;

    @MyRequestMapping("/query.*")
    public void query(HttpServletRequest req, HttpServletResponse resp, @MyRequestParam("name") String name) {
        //String result = demoService.get("Tom");
        String result = "My name is " + name;
        try {
            resp.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @MyRequestMapping("/add")
    public void add(HttpServletResponse resp, @MyRequestParam("a") Integer a, @MyRequestParam("b") Integer b) {
        try {
            resp.getWriter().write("a + b = " + (a + b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @MyRequestMapping("/remove")
    public void remove(HttpServletResponse resp, @MyRequestParam("id") String id) {
        try {
            resp.getWriter().write("删除成功！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
