package com.athena.demo.mvc.action;

import com.athena.demo.mvc.service.IDemoService;
import com.athena.mvcframework.annotation.MyAutowired;
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
public class TwoAction {

    @MyAutowired
    private IDemoService demoService;

    @MyRequestMapping("/query")
    public void query(HttpServletRequest req, HttpServletResponse resp, @MyRequestParam("name") String name) {
        String result = demoService.get("Tom");
        try {
            resp.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
