package com.spring.demo.mvc;

import com.spring.demo.IoC.context.GPApplicationContext;
import com.spring.demo.mvc.annotation.GPController;
import com.spring.demo.mvc.annotation.GPRequestMapping;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class GPDispatcherServlet extends HttpServlet {
    private final String LOCATION = "contextConfigLocation";

    //读者可以思考一下这样设计的经典之处
    //GPHandlerMapping最核心的设计，也是最经典的
    //它直接干掉struts，webwork等web框架
    private List<GPHandlerMapping> handlerMappings = new ArrayList<GPHandlerMapping>();
    private Map<GPHandlerMapping, GPHandlerAdapter> handlerAdapters = new
            HashMap<GPHandlerMapping, GPHandlerAdapter>();

    private List<GPViewResolver> viewResolvers = new ArrayList<GPViewResolver>();

    private GPApplicationContext context;

    @Override
    public void init(ServletConfig config) throws ServletException {
        //相当于把IoC容器初始化了
        context = new GPApplicationContext(config.getInitParameter(LOCATION));
        initStrategies(context);
    }

    protected void initStrategies(GPApplicationContext context) {
        //有九种策略
        //针对每个用户请求，都会经过一些处理策略处理，最终才能有结果的输出
        //每种策略可以自定义干预，但是最终的结果都一致
        //=====================这里说的就是传说中的九大组件======================
        initMultipartResolver(context);//文件上传解析，如果请求类型是multipart，将通过multipertResolver进行文件上传解析
        initLocaleResolver(context);//本地化解析
        initThemeResolver(context);//主题解析

        /** 我们自已实现 */
        //GPHandlerMapping用来保存Controller中配置的RequestMapping和Method的对应关系
        initHandlerMappings(context);//通过HandlerMapping将请求映射到处理器
        /** 我们自已实现 */
        //HandlerAdapters 用来动态匹配Method参数，包括类转换，动态赋值
        initHandlerAdapters(context);//通过HandlerAdapter进行多类型的参数动态匹配

        initHandlerExceptionResolvers(context);//如果执行过程中遇到了异常，将交给HandlerExceptionResolver来解析
        initRequestToViewNameTranslator(context);//直接将请求解析到视图

        /** 我们自已实现 */
        //通过ViewResolvers实现动态模板解析
        //自己解析一同模板语言
        initViewResolvers(context);//通过viewResolver将逻辑视图解析到具体试图实现

        initFlashMapManager(context);//flash映射管理器
    }

    private void initFlashMapManager(GPApplicationContext context) {
    }

    private void initRequestToViewNameTranslator(GPApplicationContext context) {
    }

    private void initHandlerExceptionResolvers(GPApplicationContext context) {
    }

    private void initThemeResolver(GPApplicationContext context) {
    }

    private void initLocaleResolver(GPApplicationContext context) {
    }

    private void initMultipartResolver(GPApplicationContext context) {
    }

    //将controller中配置的requestMapping和method进行一一对应
    private void initHandlerMappings(GPApplicationContext context) {
        //按照我们通常的理解应该是一个map
        //Map<String,Method> map;
        //map.put(url,Method)
        //首先从容器中获取所有的实例
        String[] beanNames = context.getBeanDefinitionNames();
        try {
            for (String beanName : beanNames) {
                //到MVC层，对外提供的方法只有一个getBean()方法
                //返回的对象不是BeanWrapper，怎么办？
                Object controller = context.getBean(beanName);
                Class<?> clazz = controller.getClass();
                if (!clazz.isAnnotationPresent(GPController.class)) {
                    continue;
                }
                String baseUrl = "";
                if (clazz.isAnnotationPresent(GPRequestMapping.class)) {
                    GPRequestMapping requestMapping = clazz.getAnnotation(GPRequestMapping.class);
                    baseUrl = requestMapping.value();
                }

                //扫描所有的public类型的方法
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (!method.isAnnotationPresent(GPRequestMapping.class)) {
                        continue;
                    }
                    GPRequestMapping requestMapping = method.getAnnotation(GPRequestMapping.class);
                    String regex = ("/" + baseUrl + requestMapping.value().replaceAll("\\*",
                            "*")).replaceAll("/+", "/");
                    Pattern pattern = Pattern.compile(regex);
                    this.handlerMappings.add(new GPHandlerMapping(pattern, controller, method));
                    log.info("Mapping:" + regex + "," + method);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initHandlerAdapters(GPApplicationContext context) {
        //在初始化阶段，我们能做的就是，将这些参数的名字或类型按一定的顺序保存下来
        //因为后面用的反射调用的时候，传的形参是一个数组
        //可以通过记录这些参数的位置index，逐个从数组中取值，这样就和参数的顺序保存无关了
        for (GPHandlerMapping handlerMapping : this.handlerMappings) {
            //每个方法有一个参数列表，这里保存的是形参列表
            this.handlerAdapters.put(handlerMapping, new GPHandlerAdapter());
        }
    }

    private void initViewResolvers(GPApplicationContext context) {
        //在页面中输入http://localhost/first.html
        //解决页面名称和模板文件的关联问题
        String templateRoot = context.getConfig().getProperty("templateRoot");
        this.viewResolvers.add(new GPViewResolver(templateRoot));
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @SneakyThrows
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
//            resp.setContentType("text/html;charset=UTF-8");
//            resp.getWriter().write("<font size='25' color='blue'>500 Exception</font><br/>"+
//                    ((InvocationTargetException) ((InvocationTargetException) e).getTargetException()).getTargetException().getMessage()+"<br/>Details:" +
//                    "<br/>" + Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]", "")
//                    .replaceAll("\\s", "\r\n") + "<font color='green'<i>Copyright@GupaoEDU" +
//                    "</i></font>");
//            e.printStackTrace();
            Map<String, Object> model = new HashMap<>();
            model.put("detail", ((InvocationTargetException) ((InvocationTargetException) e).getTargetException()).getTargetException().getMessage());
            model.put("stackTrace", Arrays.toString(((InvocationTargetException) ((InvocationTargetException) e).getTargetException()).getTargetException().getStackTrace()));
            processDispatchResult(req, resp, new GPModelAndView("500",model));
        }
    }


    private void doDispatch(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {
        //根据用户请求的Url来获取一个Handler
        GPHandlerMapping handler = getHandler(req);
        if (handler == null) {
            processDispatchResult(req, resp, new GPModelAndView("404"));
            return;
        }

        GPHandlerAdapter ha = getHandlerAdapter(handler);
        //这一步只是调用方法，得到返回值
        GPModelAndView mv = ha.handle(req, resp, handler);

        //这一步才是真的输出
        processDispatchResult(req, resp, mv);
    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp
            , GPModelAndView mv) throws Exception {
        //调用viewResolver的resolverViewName()方法
        if (null == mv) {
            return;
        }
        if (this.viewResolvers.isEmpty()) {
            return;
        }
        if (this.viewResolvers != null) {
            for (GPViewResolver viewResolver : this.viewResolvers) {
                GPView view = viewResolver.resolveViewName(mv.getViewName(), null);
                if (view != null) {
                    view.render(mv.getModel(), req, resp);
                    return;
                }
            }
        }
    }

    private GPHandlerAdapter getHandlerAdapter(GPHandlerMapping handler) {
        if (this.handlerAdapters.isEmpty()) {
            return null;
        }
        GPHandlerAdapter hd = this.handlerAdapters.get(handler);
        if (hd.supports(handler)) {
            return hd;
        }
        return null;
    }

    private GPHandlerMapping getHandler(HttpServletRequest req) {
        if (this.handlerMappings.isEmpty()) {
            return null;
        }
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");

        for (GPHandlerMapping handler : this.handlerMappings) {
            Matcher matcher = handler.getPattern().matcher(url);
            if (!matcher.matches()) {
                continue;
            }
            return handler;
        }
        return null;
    }


}
