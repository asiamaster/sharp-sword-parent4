package com.mxny.ss.processor.processor;

import com.sun.tools.javac.code.Symbol;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Set;

/**
 * 注释检查处理器
 */
public abstract class CommentProcessor extends BaseProcessor {

    /**
     * 如果返回 true，则这些注解由此注解来处理，后续其它的 Processor 无需再处理它们；如果返回 false，则这些注解未在此Processor中处理并，那么后续 Processor 可以继续处理它们。
     * @param annotations
     * @param env
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        //循环所有Controller和RestController注解
        for (TypeElement typeElement : annotations) {
            Set<? extends Element> set = env.getElementsAnnotatedWith(typeElement);
            //循环所有被注解的元素(类)
            for(Element classElement : set){
                if(!validClassComment(classElement)){
                    return true;
                }
                //只处理PUBLIC方法，要求要写注解
//                    if(element.getEnclosedElements().get(0).getModifiers().contains(Modifier.PUBLIC)){
//                    }
            }
        }
        return true;
    }

    /**
     * 验证是否有注释
     * @param classElement
     * @return 不通过返回false
     */
    protected boolean validClassComment(Element classElement){
        //先检查类上的注释
        if(StringUtils.isBlank(elementUtils.getDocComment(classElement))){
            error(classElement, "%s需要添加JavaDoc注释！", ((Symbol.ClassSymbol) classElement).fullname);
            return false;
        }
        List<? extends Element> enclosedElements = classElement.getEnclosedElements();
        for(Element enclosedElement : enclosedElements) {
            //过滤构造函数、属性和内部类
            if (enclosedElement.getKind().equals(ElementKind.CONSTRUCTOR) || enclosedElement.getKind().equals(ElementKind.FIELD) || enclosedElement.getKind().equals(ElementKind.CLASS)) {
                continue;
            }
            //有Override注解不检查注释
            if(enclosedElement.getAnnotation(Override.class) != null){
                continue;
            }
            String comment = elementUtils.getDocComment(enclosedElement);
            if (StringUtils.isBlank(comment)) {
                error(enclosedElement, "%s %s方法需要添加JavaDoc注释！", ((Symbol.MethodSymbol) enclosedElement).owner.getQualifiedName().toString(), enclosedElement.getSimpleName());
                return false;
            }
        }
        return true;
    }

}
