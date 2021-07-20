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
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.model.JavacElements;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Spring服务注释检查处理器
 * 先检查接口，如果没有接口则检查当前类
 * @author asiam
 */
@AutoService(Processor.class)
public class ServiceCommentProcessor extends CommentProcessor {

    //服务扫描路径,多个以逗号分隔
    private List<String> serviceScan = null;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        String serviceScanStr = properties.getProperty("service.scan");
        if(StringUtils.isNotBlank(serviceScanStr)){
            serviceScan = Arrays.asList(serviceScanStr.trim().split(","));
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
        for (TypeElement typeElement : annotations) {
            Set<? extends Element> set = env.getElementsAnnotatedWith(typeElement);
            for(Element classElement : set){
                if(serviceScan != null) {
                    //检查元素的包名是否匹配路径,不匹配则直接放过
                    if(!matchPackage(classElement)){
                        return false;
                    }
                }
                com.sun.tools.javac.util.List<Type> interfaces = ((Symbol.ClassSymbol) classElement).getInterfaces();
                //如果没有接口，则判断当前类上的注释
                if(interfaces.isEmpty()){
                    return validClassComment(classElement);
                }
                TypeElement interfaceTypeElement = elementUtils.getTypeElement(interfaces.get(0).toString());
                //这一步是判断当前编译环境缓存的类中是否有接口，如果没有则无法取到注释信息,并且也能说明以前的编译是通过了的
                if(((JavacElements) elementUtils).getTreeAndTopLevel(interfaceTypeElement, null, null) == null) {
                    continue;
                }
                //判断是否有注释
                if (StringUtils.isBlank(elementUtils.getDocComment(interfaceTypeElement))) {
                    error(interfaceTypeElement, "%s需要添加JavaDoc注释！", ((Symbol.ClassSymbol) interfaceTypeElement).fullname);
                    return true;
                }
                List<? extends Element> members = elementUtils.getAllMembers(interfaceTypeElement);
                for(Element member : members){
                    //如果是Object的方法，不扫描
                    if(((Symbol.MethodSymbol) member).owner.getQualifiedName().toString().equals(Object.class.getName())){
                        continue;
                    }
                    //如果是非当前接口(接口的父类)的方法，不扫描
                    if(!((Symbol.MethodSymbol) member).owner.equals(interfaceTypeElement)){
                        continue;
                    }
                    if(StringUtils.isBlank(elementUtils.getDocComment(member))){
                        error(member, "%s %s方法需要添加JavaDoc注释！",
                                ((Symbol.MethodSymbol) member).owner.getQualifiedName().toString(),
                                member.getSimpleName());
                        return true;
                    }
                }

            }
        }
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataionSet = new LinkedHashSet<String>();
        annotataionSet.add(Service.class.getCanonicalName());
        annotataionSet.add(Component.class.getCanonicalName());
        return annotataionSet;
    }


    /**
     * 元素的包名是否匹配路径
     * @param classElement
     * @return
     */
    private boolean matchPackage(Element classElement){
        String pkgFullname = ((Symbol.ClassSymbol) classElement).packge().fullname.toString();
        for(String path : serviceScan){
            //检查包路径是否扫描范围内
            if(pkgFullname.startsWith(path.trim())){
                return true;
            }
        }
        return false;
    }

}