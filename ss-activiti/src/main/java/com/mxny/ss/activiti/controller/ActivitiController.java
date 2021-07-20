package com.mxny.ss.activiti.controller;

import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.ActivitiException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;

/**
 * Activiti设计器专用控制器
 *
 * @author Asiamaster
 */
@Controller
public class ActivitiController implements ModelDataJsonConstants {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ActivitiController.class);

    /**
     * 指定modelId模型设计页
     * 此地址被modeler.html页面使用，有较多相对地址引用，不能修改
     * @param modelMap
     * @param request 必填参数modelId
     * @return
     */
    @RequestMapping(value = "/modeler.html", method = {RequestMethod.GET, RequestMethod.POST})
    public String modeler(ModelMap modelMap, HttpServletRequest request) {
        return "modeler";
    }

    /**
     * 获取编辑器组件及配置项信息
     * 该地址由static/editor-app/configuration/url-config.js文件配置
     * @return
     */
    @RequestMapping(value="/editor/stencilset", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    public @ResponseBody
    String getStencilset() {
        InputStream stencilsetStream = this.getClass().getClassLoader().getResourceAsStream("stencilset.json");
        try {
            return IOUtils.toString(stencilsetStream, "utf-8");
        } catch (Exception e) {
            throw new ActivitiException("Error while loading stencil set", e);
        }
    }
}