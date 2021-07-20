package com.mxny.ss.dto;

import com.mxny.ss.boot.InitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * DTO注册
 * Created by asiamastor on 2017/1/11.
 */
public class DTORegistrar implements ImportBeanDefinitionRegistrar {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        InitConfig.done();
        DTOFactoryUtils.registerDTOInstance(annotationMetadata);
    }

}
