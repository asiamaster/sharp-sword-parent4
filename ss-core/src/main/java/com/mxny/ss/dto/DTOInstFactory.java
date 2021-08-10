package com.mxny.ss.dto;

import javassist.ClassPath;
import javassist.CtClass;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Set;

/**
 * DTO实例工厂
 */
public class DTOInstFactory implements IDTOFactory {

    private static DTOInstFactory instance = new DTOInstFactory();


    private DTOInstFactory() {
    }

    public static DTOInstFactory getInstance() {
        return instance;
    }

    @Override
    public void insertClassPath(ClassPath classPath){
    }

    @Override
    public void importPackage(String packageName){
    }

    @Override
    public void registerDTOInstance(AnnotationMetadata importingClassMetadata) {
    }

    @Override
    public void registerDTOInstanceFromPackages(Set<String> packages, String file) {

    }

    @Override
    public <T extends IDTO> CtClass createCtClass(Class<T> clazz) throws Exception {
        return null;
    }


}