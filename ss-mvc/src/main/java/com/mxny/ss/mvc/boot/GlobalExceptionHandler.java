package com.mxny.ss.mvc.boot;

import com.alibaba.fastjson.JSON;
import com.mxny.ss.domain.BaseOutput;
import com.mxny.ss.exception.InternalException;
import com.mxny.ss.util.SpringUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 统一异常处理
 */
@ControllerAdvice
@ConditionalOnExpression("'${globalExceptionHandler.enable}'=='true'")
public class GlobalExceptionHandler {

    @ExceptionHandler(InternalException.class)
    public String internalExceptionHandler(HttpServletRequest request, HttpServletResponse response, InternalException e) throws IOException {
        e.printStackTrace();
        //判断请求类型是json
        if(request.getHeader("content-type").equals("application/json")){
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSON.toJSONString(BaseOutput.failure(e.getCode(), e.getMessage())));
            return null;
        }
        //判断是否是ajax访问，不是则跳到默认错误页面
        if (request.getHeader("X-Requested-With") == null) {
            request.setAttribute("exception", e);
            request.setAttribute("exMsg", e.getMessage());
            return SpringUtil.getProperty("error.page.default", "error/default");
        }
        response.setContentType("application/json;charset=UTF-8");
        return JSON.toJSONString(BaseOutput.failure(e.getCode(), e.getMessage()));
    }

    /**
     * 全局异常处理
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    public String defultExcepitonHandler(HttpServletRequest request, HttpServletResponse response, Exception e) throws IOException {
        Exception exception = e;
        e.printStackTrace();
        String exMsg = exception.getMessage();
        //Sentinel限流引发的异常
        if(e.getCause() != null && "com.alibaba.csp.sentinel.slots.block.flow.FlowException".equals(e.getCause().toString())){
            exception = (Exception)e.getCause();
            exMsg = "服务开启限流保护,请稍后再试!";
        }
        //判断请求类型是json
        if(request.getHeader("content-type").equals("application/json")){
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSON.toJSONString(BaseOutput.failure(exMsg)));
            return null;
        }
        //判断是否是ajax访问，不是则跳到默认错误页面
        if (request.getHeader("X-Requested-With") == null) {
            request.setAttribute("exception", exception);
            request.setAttribute("exMsg", exMsg);
//            response.sendRedirect(basePath + SpringUtil.getProperty("error.page.default", "error/default"));
            return SpringUtil.getProperty("error.page.default", "error/default");
        }
        response.setContentType("application/json;charset=UTF-8");
//        response.getWriter().write(JSON.toJSONString(BaseOutput.failure(exMsg)));
        return JSON.toJSONString(BaseOutput.failure(e.getMessage()));
    }

}