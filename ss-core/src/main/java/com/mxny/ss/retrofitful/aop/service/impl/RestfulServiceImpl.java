package com.mxny.ss.retrofitful.aop.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.mxny.ss.constant.ResultCode;
import com.mxny.ss.domain.BaseOutput;
import com.mxny.ss.dto.DTOUtils;
import com.mxny.ss.dto.IDTO;
import com.mxny.ss.exception.ParamErrorException;
import com.mxny.ss.exception.RemoteException;
import com.mxny.ss.retrofitful.RestfulAnnotation;
import com.mxny.ss.retrofitful.annotation.*;
import com.mxny.ss.retrofitful.aop.invocation.Invocation;
import com.mxny.ss.retrofitful.aop.service.RestfulService;
import com.mxny.ss.retrofitful.cache.RestfulCache;
import com.mxny.ss.util.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.*;

/**
 * ezrestful 服务实现
 * name of "restfulService"
 */
public class RestfulServiceImpl implements RestfulService {

    protected static final Logger logger = LoggerFactory.getLogger(RestfulServiceImpl.class);

    @Override
    @SuppressWarnings(value={"unchecked", "deprecation"})
    public Object invoke(Invocation invocation){
        Restful restful = invocation.getProxyClazz().getAnnotation(Restful.class);
        String restfulUrl = StringUtils.isBlank(restful.baseUrl()) ? restful.value().trim() : restful.baseUrl().trim();
        String baseUrl = getBaseUrl(restfulUrl);
        if(StringUtils.isBlank(baseUrl)){
            throw new ParamErrorException("Restful注解参数值["+restfulUrl+"]为空或属性不存在!");
        }
        return doHttp(invocation.getMethod(), invocation.getArgs(), baseUrl);
    }

    @SuppressWarnings(value={"unchecked", "deprecation"})
    private Object doHttp(Method method, Object[] args, String baseUrl){
        RestfulAnnotation restfulAnnotation = null;
        try {
            restfulAnnotation = getRestfulAnnotation(method, args);
        } catch (Exception e) {
            if(BaseOutput.class.isAssignableFrom(method.getReturnType())){
                return BaseOutput.failure(e.getMessage()).setCode(ResultCode.PARAMS_ERROR);
            }
            throw e;
        }
        String httpMethod = StringUtils.isBlank(restfulAnnotation.getPost()) ? "GET" : "POST";
        String url = "POST".equals(httpMethod) ? baseUrl + restfulAnnotation.getPost() : baseUrl + restfulAnnotation.getGet();
        if (!restfulAnnotation.getPathParams().isEmpty()) {
            url = RegUtils.replaceBraceParams(url, restfulAnnotation.getPathParams());
        }
        return DelegateService.doHttp(url, method.getGenericReturnType(), restfulAnnotation, httpMethod);
    }
    @SuppressWarnings(value={"unchecked", "deprecation"})
    private String getBaseUrl(String restfulUrl){
        if(restfulUrl.startsWith("${") && restfulUrl.endsWith("}")){
            String key = restfulUrl.substring(2, restfulUrl.length()-1).trim();
            return SpringUtil.getProperty(key);
        }else{
            return restfulUrl;
        }
    }

    /**
     * 根据方法获取所有注解内容
     * 可能会抛出参数校验异常
     * @param method
     * @return
     */
    @SuppressWarnings(value={"unchecked", "deprecation"})
    private static RestfulAnnotation getRestfulAnnotation(Method method, Object[] args){
        RestfulAnnotation restfulAnnotation = new RestfulAnnotation();
        Annotation[][] ass = method.getParameterAnnotations();
        Parameter[] parameters = method.getParameters();
        POST post = method.getAnnotation(POST.class);
        GET get = method.getAnnotation(GET.class);
        if(post != null){
            if(StringUtils.isBlank(post.value())){
                throw new ParamErrorException("@POST参数不能为空");
            }
            restfulAnnotation.setPost(post.value());
        }else if(get != null){
            if(StringUtils.isBlank(get.value())){
                throw new ParamErrorException("@GET参数不能为空");
            }
            restfulAnnotation.setGet(get.value());
        }else{
            throw new ParamErrorException("@GET和@POST注解不存在");
        }
        for(int i=0; i<ass.length; i++){
            for(int j=0; j<ass[i].length; j++){
                Annotation annotation = ass[i][j];
                //处理http body中的json字段
                if(VOField.class.equals(annotation.annotationType())) {
                    Object param = args[i];
                    VOField reqParam = (VOField)annotation;
                    if(reqParam.required() && param == null){
                        throw new ParamErrorException("@VOField必填参数为空");
                    }
                    restfulAnnotation.getVoFields().put(((VOField) annotation).value(), args[i]);
                }
                //处理http body中的对象
                else if(VOBody.class.equals(annotation.annotationType())){
                    Object param = args[i];
                    VOBody reqParam = (VOBody)annotation;
                    if(reqParam.required() && param == null){
                        throw new ParamErrorException("@VOBody必填参数为空");
                    }
                    restfulAnnotation.setVoBody(args[i]);
                }
                //处理http header
                else if(ReqHeader.class.equals(annotation.annotationType())){
                    Object param = args[i];
                    ReqHeader reqParam = (ReqHeader)annotation;
                    if(reqParam.required() && param == null){
                        throw new ParamErrorException("@ReqHeader必填参数为空");
                    }
                    restfulAnnotation.setHeaders((Map)args[i]);
                }
                //处理单个form参数
                else if(ReqParam.class.equals(annotation.annotationType())){
                    Object param = args[i];
                    ReqParam reqParam = (ReqParam)annotation;
                    if(reqParam.required() && param == null){
                        throw new ParamErrorException("@ReqParam必填参数为空");
                    }
                    if(param == null){
                        //当ReqParam为null时，POST传参在okhttputil会空指针，这里处理为空串
                        restfulAnnotation.getReqParams().put(reqParam.value(), "");
                        continue;
                    }
                    if(Date.class.equals(parameters[i].getType())){
                        param = DateUtils.format((Date) param);
                    }else if(List.class.equals(parameters[i].getType())){
                        param = JSONArray.toJSONString(param, SerializerFeature.WriteDateUseDateFormat, SerializerFeature.IgnoreErrorGetter);
                    }
                    //IDTO接口
                    else if(DTOUtils.isProxy(parameters[i].getType())){
                        param = JSONObject.toJSONString(DTOUtils.go(param), SerializerFeature.WriteDateUseDateFormat, SerializerFeature.IgnoreErrorGetter);
                    }
                    //实现了IDTO的实体Bean
                    else if(IDTO.class.isAssignableFrom(parameters[i].getType())){
                        param = JSONObject.toJSONString(param, SerializerFeature.WriteDateUseDateFormat, SerializerFeature.IgnoreErrorGetter);
                    }
                    //单独处理Map
                    if(Map.class.isAssignableFrom(parameters[i].getType())){
                        restfulAnnotation.getReqParams().putAll((Map)param);
                    }else{
                        restfulAnnotation.getReqParams().put(reqParam.value(), param.toString());
                    }
                }else if(PathParam.class.equals(annotation.annotationType())){
                    Object param = args[i];
                    PathParam pathParam = (PathParam)annotation;
                    if(pathParam.required() && param == null){
                        throw new ParamErrorException("@PathParam必填参数为空");
                    }
                    if(param == null){
                        //当ReqParam为null时，POST传参在okhttputil会空指针，这里处理为空串
                        restfulAnnotation.getPathParams().put(pathParam.value(), "");
                        continue;
                    }
                    restfulAnnotation.getPathParams().put(pathParam.value(), param.toString());
                }
                //处理对象form参数
                else if(ReqVOParam.class.equals(annotation.annotationType())){
                    Object param = args[i];
                    ReqVOParam reqParam = (ReqVOParam)annotation;
                    if(reqParam.required() && param == null){
                        throw new ParamErrorException("@ReqVOParam必填参数为空");
                    }
                    if(param == null){
                        continue;
                    }
                    try {
                        restfulAnnotation.getReqParams().putAll((Map)BeanConver.transformObjectToMap(param));
                    } catch (Exception e) {
                        continue;
                    }
                }
            }
        }
        //从当前线程缓存中获取header，但以入参为主
        Map<String, String> headerMap = RestfulCache.RESTFUL_HEADER_THREAD_LOCAL.get();
        if(headerMap  != null) {
            if (restfulAnnotation.getHeaders() == null) {
                restfulAnnotation.setHeaders(headerMap);
            } else {
                Map<String, String> headers = restfulAnnotation.getHeaders();
                //以restfulAnnotation.getHeaders()中的为主
                Set<Map.Entry<String, String>> entries = headerMap.entrySet();
                entries.forEach(entry -> headers.putIfAbsent(entry.getKey(), entry.getValue()));
            }
            RestfulCache.RESTFUL_HEADER_THREAD_LOCAL.remove();
        }
        return restfulAnnotation;
    }

    /**
     * 委托服务
     */
    private static class DelegateService {
        protected Logger LOGGER = LoggerFactory.getLogger(this.getClass());

        @SuppressWarnings(value={"unchecked", "deprecation"})
        DelegateService(){
        }
        @SuppressWarnings(value={"unchecked", "deprecation"})
        private static Object doHttp(String url, Type retType, RestfulAnnotation restfulAnnotation, String httpMethod){
            Object body = restfulAnnotation.getVoBody();
            Map<String, Object> fields = restfulAnnotation.getVoFields();
            //如果body和fields都不为空，并且body又是Map或DTO，将fields put到body中
            if(body != null) {
                if(!fields.isEmpty()) {
                    if (Map.class.isAssignableFrom(body.getClass())) {
                        ((Map) body).putAll(fields);
                    }else if(DTOUtils.isProxy(body)){
                        DTOUtils.go(body).putAll(fields);
                    }
                }
                return executeBody(url, body, restfulAnnotation.getHeaders(), retType, httpMethod);
            }else if(!fields.isEmpty()){
                return executeBody(url, fields, restfulAnnotation.getHeaders(), retType, httpMethod);
            }else if(!restfulAnnotation.getReqParams().isEmpty()){
                return executeParam(url, restfulAnnotation.getReqParams(), restfulAnnotation.getHeaders(), retType, httpMethod);
            }else{
                return executeBody(url, null, restfulAnnotation.getHeaders(), retType, httpMethod);
            }
        }

        /**
         * 以request body请求方式发送参数
         * @param url
         * @param paramObj
         * @param type
         * @param httpMethod
         * @return
         */
        @SuppressWarnings(value={"unchecked", "deprecation"})
        private static Object executeBody(String url, Object paramObj, Map<String, String> headers, Type type, String httpMethod){
            if(headers == null){
                headers = new HashMap<>(1);
            }
            if(!headers.containsKey("Content-Type")){
                headers.put("Content-Type", "application/json;charset=utf-8");
            }
            if(!(type instanceof Class) ) {
                //远程调用失败，构建一个BaseOutput
                BaseOutput output = new BaseOutput();
                try{
                    String json = paramObj instanceof String ? (String) paramObj : JSON.toJSONString(paramObj, SerializerFeature.WriteDateUseDateFormat, SerializerFeature.IgnoreErrorGetter);
                    String result =  "POST".equalsIgnoreCase(httpMethod) ?
                            OkHttpUtils.postBodyString(url, json, headers, null) :
                            OkHttpUtils.get(url, (Map) JSON.toJSON(paramObj), headers, null);
                    return JSON.parseObject(result, type);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    output.setCode(ResultCode.REMOTE_ERROR);
                    output.setMessage(e.getMessage());
                    //解决：java.lang.ClassCastException:BaseOutput cannot be cast to PageOutput
                    return JSON.parseObject(JSON.toJSONString(output, SerializerFeature.WriteDateUseDateFormat , SerializerFeature.IgnoreErrorGetter), type);
                }
            }else{
                logger.warn("远程调用返回值建议使用BaseOutput!");
                try {
                    String json = paramObj instanceof String ? (String) paramObj : JSON.toJSONString(paramObj, SerializerFeature.WriteDateUseDateFormat, SerializerFeature.IgnoreErrorGetter);
                    String result =  "POST".equalsIgnoreCase(httpMethod) ?
                            OkHttpUtils.postBodyString(url, json, headers, null) :
                            OkHttpUtils.get(url, (Map) JSON.toJSON(paramObj), headers, null);
                    Class retClazz = (Class) type;
                    if(Void.TYPE.equals(retClazz)){
                        return null;
                    }
                    if (retClazz.isPrimitive()) {
                        return POJOUtils.getPrimitiveValue(retClazz, result);
                    }
                    return JSON.parseObject(result, type);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    throw new RemoteException(e);
                }
            }
        }

        /**
         * 以request param请求方式发送参数
         * @param url
         * @param params
         * @param retType
         * @param httpMethod
         * @return
         */
        @SuppressWarnings(value={"unchecked", "deprecation"})
        private static Object executeParam(String url, Map<String, String> params, Map<String, String> headers, Type retType, String httpMethod){
            if(!(retType instanceof Class) ) {
                try{
                    String result = "POST".equalsIgnoreCase(httpMethod) ?
                            OkHttpUtils.postFormParameters(url, params, headers, null) :
                            OkHttpUtils.get(url, params, headers, null);
                    return JSON.parseObject(result, retType);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    //远程调用失败，构建一个BaseOutput
                    BaseOutput output = new BaseOutput();
                    output.setCode(ResultCode.REMOTE_ERROR);
                    output.setMessage(e.getMessage());
                    //解决：java.lang.ClassCastException:BaseOutput cannot be cast to PageOutput
                    return JSON.parseObject(JSON.toJSONString(output, SerializerFeature.WriteDateUseDateFormat , SerializerFeature.IgnoreErrorGetter), retType);
                }
            }else{
                logger.warn("远程调用返回值建议使用BaseOutput!");
                try {
                    String result = "POST".equalsIgnoreCase(httpMethod) ?
                            OkHttpUtils.postFormParameters(url, params, headers, null) :
                            OkHttpUtils.get(url, params, headers, null);
                    Class retClazz = (Class) retType;
                    if(Void.TYPE.equals(retClazz)){
                        return null;
                    }
                    if (retClazz.isPrimitive()) {
                        return POJOUtils.getPrimitiveValue(retClazz, result);
                    }
                    return JSON.parseObject(result, retType);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    throw new RemoteException(e);
                }
            }
        }
    }
}