//                            _ooOoo_
//                           o8888888o
//                           88" . "88
//                           (| -_- |)
//                            O\ = /O
//                        ____/`---'\____
//                      .   ' \\| |// `.
//                       / \\||| : |||// \
//                     / _||||| -:- |||||- \
//                       | | \\\ - /// | |
//                     | \_| ''\---/'' | |
//                      \ .-\__ `-` ___/-. /
//                   ___`. .' /--.--\ `. . __
//                ."" '< `.___\_<|>_/___.' >'"".
//               | | : `- \`.;`\ _ /`;.`/ - ` : | |
//                 \ \ `-. \_ __\ /__ _/ .-` / /
//         ======`-.____`-.___\_____/___.-`____.-'======
//                            `=---='
//
//         .............................................
//                  佛祖镇楼                  BUG辟易
//          佛曰:
//                  写字楼里写字间，写字间里程序员；
//                  程序人员写程序，又拿程序换酒钱。
//                  酒醒只在网上坐，酒醉还来网下眠；
//                  酒醉酒醒日复日，网上网下年复年。
//                  但愿老死电脑间，不愿鞠躬老板前；
//                  奔驰宝马贵者趣，公交自行程序员。
//                  别人笑我忒疯癫，我笑自己命太贱；
//                  不见满街漂亮妹，哪个归得程序员？
package com.mxny.ss.processor.processor;

import com.google.auto.service.AutoService;
import com.sun.tools.javac.code.Symbol;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Controller注释处理器
 */
@AutoService(Processor.class)
public class ControllerCommentProcessor extends CommentProcessor {

    //Controller扫描路径,多个以逗号分隔
    private List<String> controllerScan = null;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        String serviceScanStr = properties.getProperty("controller.scan");
        if(StringUtils.isNotBlank(serviceScanStr)){
            controllerScan = Arrays.asList(serviceScanStr.trim().split(","));
        }
    }

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
                if(controllerScan != null) {
                    //检查元素的包名是否匹配路径,不匹配则直接放过
                    if(!matchPackage(classElement)){
                        return false;
                    }
                }
                if(StringUtils.isBlank(elementUtils.getDocComment(classElement))){
                    error(classElement, "%s需要添加JavaDoc注释！", ((Symbol.ClassSymbol) classElement).fullname);
                    return true;
                }
                List<? extends Element> enclosedElements = classElement.getEnclosedElements();
                for(Element enclosedElement : enclosedElements){
                    //过滤构造函数、属性和内部类
                    if(enclosedElement.getKind().equals(ElementKind.CONSTRUCTOR)
                            || enclosedElement.getKind().equals(ElementKind.FIELD)
                            || enclosedElement.getKind().equals(ElementKind.CLASS)){
                        continue;
                    }
                    String comment = elementUtils.getDocComment(enclosedElement);
                    if(StringUtils.isBlank(comment)){
                        error(enclosedElement, "%s %s方法需要添加JavaDoc注释！",
                                ((Symbol.MethodSymbol) enclosedElement).owner.getQualifiedName().toString(),
                                enclosedElement.getSimpleName());
                        return true;
                    }
                    //只处理PUBLIC方法，要求要写注解
//                    if(element.getEnclosedElements().get(0).getModifiers().contains(Modifier.PUBLIC)){
//                    }
                }
            }
        }
        return true;
    }

    /**
     * 元素的包名是否匹配路径
     * @param classElement
     * @return
     */
    private boolean matchPackage(Element classElement){
        String pkgFullname = ((Symbol.ClassSymbol) classElement).packge().fullname.toString();
        for(String path : controllerScan){
            //检查包路径是否扫描范围内
            if(pkgFullname.startsWith(path.trim())){
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataions = new LinkedHashSet<String>();
        annotataions.add(Controller.class.getCanonicalName());
        annotataions.add(RestController.class.getCanonicalName());
        return annotataions;
    }

}