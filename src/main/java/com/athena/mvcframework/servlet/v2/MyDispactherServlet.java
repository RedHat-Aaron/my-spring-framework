package com.athena.mvcframework.servlet.v2;

import com.athena.mvcframework.annotation.*;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @Author: xiaoxiang.zhang
 * @Description:请求分发类
 * @Date: Create in 8:07 PM 2020/1/6
 * @Version:2.0
 */
public class MyDispactherServlet extends HttpServlet {

    /**
     * 保存application.properties的配置
     */
    private Properties contextConfig = new Properties();

    /**
     * 保存所有扫描到的类名
     */
    private List<String> classNames = new ArrayList<>();

    /**
     * IOC容器
     **/
    private Map<String, Object> ioc = new HashMap<>();

    /**
     * handlerMapping
     **/
    private Map<String, Method> handlerMapping = new HashMap<>();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //6.调用(属于运行阶段)
        try {
            doDispacther(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception,Detial:" + Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispacther(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //1.拿到请求的绝对路径
        String url = req.getRequestURI();
        //2.拿到当前项目的上下文信息
        String contextPath = req.getContextPath();
        //3.将URL中的上下文信息替换，以便于进行匹配对应的handlerMapping
        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");
        if (!handlerMapping.containsKey(url)) {
            //如果ioc容器中这个url不存在，那么
            resp.getWriter().write("404 NOT FOUND!");
            return;
        }
        //投机取巧的方法
        //通过反射，拿到方法的父类名称
        //这样就可以得到对应的对象
        Method method = handlerMapping.get(url);
        String beanName = toLowerCaseFirstChar(method.getDeclaringClass().getSimpleName());
        //执行对象
        Object obj = ioc.get(beanName);
        //拿到传入的参数列表
        Map<String, String[]> parameterMap = req.getParameterMap();
        //拿到方法的参数列表
        Class<?>[] parameterTypes = method.getParameterTypes();
        //存储对应的参数顺序
        Object[] paramValues = new Object[parameterTypes.length];
        //开始对参数列表进行迭代循环
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i] == HttpServletRequest.class) {
                paramValues[i] = req;
                continue;
            } else if (parameterTypes[i] == HttpServletResponse.class) {
                paramValues[i] = resp;
                continue;
            } /*else if (parameterTypes[i] == String.class) {*/
            //拿到这个参数上的MyRequestParam注解，并取到其名称
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            for (int j = 0; j < parameterAnnotations[i].length; j++) {
                //这里表示第i个参数的第j个注解
                if (parameterAnnotations[i][j] instanceof MyRequestParam) {
                    //那么说明当前注解为MyRequestParam
                    MyRequestParam myRequestParam = (MyRequestParam) parameterAnnotations[i][j];
                    if (parameterMap.containsKey(myRequestParam.value())) {
                        String value = Arrays.toString(parameterMap.get(myRequestParam.value()))
                                .replaceAll("\\[|\\]", "");
                        Object p = convert(parameterTypes[i], value);
                        paramValues[i] = p;
                    }
                }
            }

            /*}*/
        }
        /*method.invoke(obj, new Object[]{req, resp,
                parameterMap.get("name")[0]
        });*/
        method.invoke(obj, paramValues);
    }

    /**
     * @return java.lang.Object
     * @Author xiangxz
     * @Description 对传入的数据类型做转换
     * @Date 12:17 AM 2020/1/9
     * @Param [clazzType, value]
     */
    private Object convert(Class clazzType, String value) {
        if (Integer.class == clazzType) {
            return Integer.valueOf(value);
        } else if (String.class == clazzType) {
            return value;
        }
        return value;
    }

    //属于初始化阶段
    @Override
    public void init(ServletConfig config) throws ServletException {
        //1.加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        //2.扫描相关类
        doScanner(contextConfig.getProperty("scanPackage"));

        //3.初始化扫描到的类，并把他们放入到IOC容器中
        doInstance();

        //4.完成依赖注入
        doAutowired();

        //5.初始化HandlerMapping
        initHandlerMapping();

        System.out.println("My MVC framework has inited!");

    }

    private void initHandlerMapping() {
        //初始化url和method一一对应关系
        //1.判断IOC容器是否为空
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class clazz = entry.getValue().getClass();
            //判断类上是否拥有controller注解
            if (!clazz.isAnnotationPresent(MyController.class)) {
                //说明不是controller类，这时直接跳过
                continue;
            }
            String baseUrl = "";
            if (clazz.isAnnotationPresent(MyRequestMapping.class)) {
                MyRequestMapping clazzAnnotation = (MyRequestMapping) clazz.getAnnotation(MyRequestMapping.class);
                baseUrl = clazzAnnotation.value();
            }
            //2.默认获取当前类中的所有的Public方法
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                //判断当前方法是是否存在RequestMapping注解
                if (!method.isAnnotationPresent(MyRequestMapping.class)) {
                    continue;
                }
                MyRequestMapping methodAnnotation = (MyRequestMapping) method.getAnnotation(MyRequestMapping.class);
                String url = (baseUrl + "/" + methodAnnotation.value()).replaceAll("/+", "/");
                handlerMapping.put(url, method);
                System.out.println("Mapped:" + url + "," + method);
            }
        }
    }

    private void doAutowired() {
        //进行依赖注入
        if (ioc.isEmpty()) {
            return;
        }
        //1.开始遍历IOC容器
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            //1.1拿到每个实例的每个字段
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            //1.2迭代这些字段并进行注入
            for (Field field : fields) {
                if (!field.isAnnotationPresent(MyAutowired.class)) {
                    //如果没有Autowired注解，那么就需要跳过
                    continue;
                }
                MyAutowired myAutowired = field.getAnnotation(MyAutowired.class);
                //这里省去了对类名首字母小写情况的判断，这个作业作为课后作业
                //这里之前是使用当前类的所有接口的接口名称来作为key的
                String beanName = myAutowired.value().trim();
                if (StringUtils.isBlank(beanName)) {
                    beanName = field.getType().getName();
                }
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doInstance() {
        //进行初始化为DI作准备
        //1.判断类名列表是否为空
        if (classNames.isEmpty()) {
            return;
        }
        //2.循环类名列表开始初始化
        try {
            for (String className : classNames) {
                Class clazz = Class.forName(className);
                //那么什么类需要初始化？
                //加了MyController和MyService需要初始化
                //判断当前类是否加上了这两个注解
                if (clazz.isAnnotationPresent(MyController.class)) {
                    //实例化这个对象，并将其存储在IOC容器中
                    Object instance = clazz.newInstance();
                    //Spring默认的key是类名首字母小写
                    String key = toLowerCaseFirstChar(clazz.getSimpleName());
                    ioc.put(key, instance);
                } else if (clazz.isAnnotationPresent(MyService.class)) {
                    //1.自定义的beanName
                    MyService myService = (MyService) clazz.getAnnotation(MyService.class);
                    String beanName = myService.value();
                    if (StringUtils.isBlank(beanName)) {
                        beanName = toLowerCaseFirstChar(clazz.getSimpleName());
                    }
                    //2.根据类型自动赋值
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);
                    //3.将这个类的所有接口统统作为key，这个类的实例作为值存储到ioc容器中
                    for (Class interfaceKey : clazz.getInterfaces()) {
                        if (ioc.containsKey(interfaceKey.getName())) {
                            throw new Exception("IOC 容器中已包含当前key，请更换key值进行存储！");
                        }
                        //把接口的类型直接当做key
                        ioc.put(interfaceKey.getName(), instance);
                    }
                } else {
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return java.lang.String
     * @Author xiangxz
     * @Description 首字母小写
     * @Date 10:26 PM 2020/1/7
     * @Param [className]
     */
    private String toLowerCaseFirstChar(String className) {
        char[] chars = className.toCharArray();
        //大小写字母的ASIIC码相差32
        chars[0] += 32;
        return String.valueOf(chars);
    }

    /**
     * @param scanPackage
     * @return void
     * @Author xiangxz
     * @Description 扫描对应路径下的包
     * @Date 9:10 PM 2020/1/6
     * @Param [scanPackage]
     */
    private void doScanner(String scanPackage) {
        //这个URL就是classPath
        URL resourceURL = Thread.currentThread().getContextClassLoader().getResource("/" + convertPackage(scanPackage));
        File root = new File(resourceURL.getFile());
        for (File child : root.listFiles()) {
            if (child.isDirectory()) {
                //如果当前文件是文件夹，那么需要进行递归继续调用
                doScanner(scanPackage + "." + child.getName());
            } else {
                //如果不是文件夹，那么，判断是否为.class文件
                if (!child.getName().endsWith(".class")) {
                    continue;
                }
                //如果是class那么，就需要放入list队列
                String className = scanPackage + "." + child.getName().replace(".class", "");
                classNames.add(className);
            }
        }
    }

    /**
     * @return java.lang.String
     * @Author xiangxz
     * @Description 将传入的包路径更改为文件路径
     * @Date 9:13 PM 2020/1/6
     * @Param [scanPackage]
     */
    private String convertPackage(String scanPackage) {
        return scanPackage.replace(".", "/");
    }

    /**
     * @return void
     * @Author xiangxz
     * @Description 读取配置文件内容
     * @Date 8:53 PM 2020/1/6
     * @Param [contextConfigLocation]
     */
    private void doLoadConfig(String contextConfigLocation) {
        InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            contextConfig.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != resourceAsStream) {
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
