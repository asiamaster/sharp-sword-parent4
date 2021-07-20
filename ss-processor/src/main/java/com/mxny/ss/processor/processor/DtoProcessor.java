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
import com.mxny.ss.processor.annotation.GenDTOMethod;
import com.mxny.ss.processor.util.ClassUtils;
import com.mxny.ss.util.POJOUtils;
import com.squareup.javapoet.*;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.persistence.*;
import javax.tools.Diagnostic;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * DTO根据属性生成方法的处理器
 * 用于GenDTOMethod注解
 */
@AutoService(Processor.class)
public class DtoProcessor extends BaseProcessor {

    /**
     * 如果返回 true，则这些注解由此注解来处理，后续其它的 Processor 无需再处理它们；如果返回 false，则这些注解未在此Processor中处理并，那么后续 Processor 可以继续处理它们。
     * @param annotations
     * @param env
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        Set<? extends Element> set = env.getElementsAnnotatedWith(GenDTOMethod.class);
        set.forEach(element -> {
            // 检查被注解为@GenDtoMethod的元素是否是一个接口
//            if (element.getKind() != ElementKind.INTERFACE) {
//                error(element, "Only interface can be annotated with @%s",
//                        GenDTOMethod.class.getSimpleName());
//                return;
//            }
            //获取源文件地址
            String sourceFilePath = ((Symbol.ClassSymbol) element).sourcefile.getName();
            //解析生成的源文件目录
            String filePath = sourceFilePath.substring(0, sourceFilePath.indexOf("/src/main/java/")+15);
            //包名
            String packageFullName = ((Symbol.ClassSymbol) element).packge().fullname.toString();
            //构建java源文件
            TypeSpec typeSpec = null;
            try {
                if (element.getKind() == ElementKind.INTERFACE) {
                    typeSpec = buildIntfSource(element);
                }else{
                    typeSpec = buildClassSource(element);
                }

            } catch (ClassNotFoundException e) {
                messager.printMessage(Diagnostic.Kind.WARNING, "构建失败:" + e.getMessage());
                return;
            }
            //演示获取FileObject
//                FileObject fo = filer.getResource(StandardLocation.CLASS_OUTPUT, processingEnv.getElementUtils().getPackageOf(element).toString(), element.getSimpleName()+".java");
            //创建Java文件
            JavaFile javaFile = JavaFile.builder(packageFullName, typeSpec).skipJavaLangImports(true).build();
            try {
                javaFile.writeTo(new File(filePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
            //打印
//            messager.printMessage(Diagnostic.Kind.WARNING, "Printing:" + element.toString());
//            messager.printMessage(Diagnostic.Kind.WARNING, "getSimpleName:" + element.getSimpleName());
//            messager.printMessage(Diagnostic.Kind.WARNING, "getEnclosedElements:" + element.getEnclosedElements().toString());
//            messager.printMessage(Diagnostic.Kind.WARNING, "trees.sourceFile:" + trees.getPath(element).getCompilationUnit().getSourceFile().getName());
        });
        return true;
    }

    /**
     * 根据element类中的属性构建getter/setter方法，原有方法和方法上的注解保留
     * @param element
     */
    private TypeSpec buildClassSource(Element element) throws ClassNotFoundException {
        String simpleName = element.getSimpleName().toString();
        //构建类
        TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(simpleName)
                .addModifiers(Modifier.PUBLIC);
        String doc = elementUtils.getDocComment(element);
        if(StringUtils.isNotBlank(doc)) {
            interfaceBuilder.addJavadoc(doc);
        }
        com.sun.tools.javac.util.List<Attribute.Compound> classAnnotationMirrors = ((Symbol.ClassSymbol) element).getAnnotationMirrors();
        //构建类注解
        List<AnnotationSpec> annotationSpecs = new ArrayList<>(classAnnotationMirrors.size());
        //是否生成JPA注解
        Boolean jpaAnnotation = false;
        //是否链式setter
        Boolean chainSetter = false;
        //是否包括Table注解，没有的话就构建一个
        boolean containsTableAnnotation = false;
        for(int i=0; i<classAnnotationMirrors.size(); i++){
//          获取GenDTOMethod注解， compound为@com.mxny.ss.processor.annotation.GenDTOMethod
            Attribute.Compound compound = classAnnotationMirrors.get(i);
            Map<Symbol.MethodSymbol, Attribute> symbolAttributeMap = compound.getElementValues();
            if (Table.class.getName().equals(compound.type.toString())) {
                containsTableAnnotation = true;
            }
            //只处理GenDTOMethod注解的属性
            if (!GenDTOMethod.class.getName().equals(compound.type.toString())) {
                //构建当前注解(原来有什么注解不变)
                AnnotationSpec.Builder annotationSpecBuilder = AnnotationSpec.builder(Class.forName(compound.getAnnotationType().toString()));
                for(Map.Entry<Symbol.MethodSymbol, Attribute> entry : symbolAttributeMap.entrySet()){
                    Object value = entry.getValue().getValue();
                    if(value instanceof String){
                        annotationSpecBuilder.addMember(entry.getKey().getSimpleName().toString(), CodeBlock.builder().add("$S", value).build());
                    }else if(entry.getValue() instanceof Attribute.Enum){
                        annotationSpecBuilder.addMember(entry.getKey().getSimpleName().toString(), CodeBlock.builder().add("$T.$L", ((Attribute.Enum) entry.getValue()).type, value.toString()).build());
                    }else{
                        annotationSpecBuilder.addMember(entry.getKey().getSimpleName().toString(), CodeBlock.builder().add("$L", value).build());
                    }
                }
                annotationSpecs.add(annotationSpecBuilder.build());
                continue;
            }
            //没有属性，则判断默认值
            if (symbolAttributeMap.isEmpty()) {
                //基于JDK8实现， 获取GenDTOMethod.reuse()的默认值，赋值到break变量
//                Scope.Entry entry = compound.type.tsym.members().elems;
//                Iterator it = entry.scope.getElements().iterator();
//                while (it.hasNext()) {
//                    Symbol.MethodSymbol methodSymbol = (Symbol.MethodSymbol) it.next();
//                    Object reuseValue = methodSymbol.getDefaultValue().getValue();
//                    if ("reuse".equals(entry.sym.getSimpleName().toString())) {
//                        reuse = (boolean) reuseValue;
//                        break;
//                    }
//                }
                //JDK8以上版本实现， 获取GenDTOMethod.reuse()的默认值，赋值到break变量
                List<Symbol.MethodSymbol> methodSymbols = (List) compound.getAnnotationType().asElement().getEnclosedElements();
                for (Symbol.MethodSymbol methodSymbol : methodSymbols) {
                    switch (methodSymbol.getSimpleName().toString()) {
                        case "jpaAnnotation" : jpaAnnotation = (boolean) methodSymbol.defaultValue.getValue();break;
                        case "chainSetter" : chainSetter = (boolean) methodSymbol.defaultValue.getValue();break;
                    }
                }
            } else {
                for (Map.Entry<Symbol.MethodSymbol, Attribute> entry : symbolAttributeMap.entrySet()) {
                    Object annotationValue = entry.getValue().getValue();
                    switch (entry.getKey().getSimpleName().toString()) {
                        case "jpaAnnotation" : jpaAnnotation = (boolean) annotationValue;break;
                        case "chainSetter" : chainSetter = (boolean) annotationValue;break;
                    }
                }
            }
        }
        if(!containsTableAnnotation && jpaAnnotation){
            //添加Table注解
            AnnotationSpec.Builder annotationSpecBuilder = AnnotationSpec.builder(Table.class);
            annotationSpecBuilder.addMember("name", CodeBlock.builder().add("$S", "`"+POJOUtils.humpToLineFast(simpleName)+"`").build());
            annotationSpecs.add(annotationSpecBuilder.build());
        }
        //构建父接口
        com.sun.tools.javac.util.List<Type> interfacesType = ((Symbol.ClassSymbol) element).getInterfaces();
        List<TypeName> typeNames = new ArrayList<>(interfacesType.size());
        for (Type type : interfacesType) {
            typeNames.add(ParameterizedTypeName.get(type));
        }
        //添加类注解
        interfaceBuilder.addAnnotations(annotationSpecs).addSuperinterfaces(typeNames);
        //构建父类
//        Type superclass = ((Symbol.ClassSymbol) element).getSuperclass();
//        builder.addAnnotations(annotationSpecs).addSuperinterfaces(typeNames).superclass(ParameterizedTypeName.get(superclass));
        //构建属性和方法
        List<? extends Element> elements = element.getEnclosedElements();
        for(Element ele : elements) {
            //构建属性
            if (ele.getKind().equals(ElementKind.FIELD)) {
                messager.printMessage(Diagnostic.Kind.OTHER, "根据属性" + ele.getSimpleName() + "构建getter/setter.");
                buildMethodByField(interfaceBuilder, (Symbol.VarSymbol) ele, jpaAnnotation, chainSetter, ((Symbol.ClassSymbol) element).className());
            } else if (ele.getKind().equals(ElementKind.METHOD)) {
                //javabean类的方法不构建
//                buildMethod(builder, (Symbol.MethodSymbol) ele);
            }
        }
        return interfaceBuilder.build();
    }

    /**
     * 根据element接口中的属性构建getter/setter方法，原有方法和方法上的注解保留
     * @param element
     */
    private TypeSpec buildIntfSource(Element element) throws ClassNotFoundException {
        String simpleName = element.getSimpleName().toString();
        //构建类
        TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(simpleName)
                .addModifiers(Modifier.PUBLIC);
        String doc = elementUtils.getDocComment(element);
        if(StringUtils.isNotBlank(doc)) {
            interfaceBuilder.addJavadoc(doc);
        }
        com.sun.tools.javac.util.List<Attribute.Compound> classAnnotationMirrors = ((Symbol.ClassSymbol) element).getAnnotationMirrors();
        //是否生成JPA注解
        Boolean jpaAnnotation = false;
        //是否链式setter
        Boolean chainSetter = false;
        //是否包括Table注解，没有的话就构建一个
        boolean containsTableAnnotation = false;
        //构建注解
        List<AnnotationSpec> annotationSpecs = new ArrayList<>(classAnnotationMirrors.size());
        for(int i=0; i<classAnnotationMirrors.size(); i++){
//          获取GenDTOMethod注解， compound为@com.mxny.ss.processor.annotation.GenDTOMethod
            Attribute.Compound compound = classAnnotationMirrors.get(i);
            Map<Symbol.MethodSymbol, Attribute> symbolAttributeMap = compound.getElementValues();
            if (Table.class.getName().equals(compound.type.toString())) {
                containsTableAnnotation = true;
            }
            //只处理GenDTOMethod注解的属性
            if (!GenDTOMethod.class.getName().equals(compound.type.toString())) {
                //构建其它注解
                AnnotationSpec.Builder annotationSpecBuilder = AnnotationSpec.builder(Class.forName(compound.getAnnotationType().toString()));
                for(Map.Entry<Symbol.MethodSymbol, Attribute> entry : symbolAttributeMap.entrySet()){
                    Object value = entry.getValue().getValue();
                    if(value instanceof String){
                        annotationSpecBuilder.addMember(entry.getKey().getSimpleName().toString(), CodeBlock.builder().add("$S", value).build());
                    }else if(entry.getValue() instanceof Attribute.Enum){
                        annotationSpecBuilder.addMember(entry.getKey().getSimpleName().toString(), CodeBlock.builder().add("$T.$L", ((Attribute.Enum) entry.getValue()).type, value.toString()).build());
                    }else{
                        annotationSpecBuilder.addMember(entry.getKey().getSimpleName().toString(), CodeBlock.builder().add("$L", value).build());
                    }
                }
                annotationSpecs.add(annotationSpecBuilder.build());
                continue;
            }
            //没有属性，则判断默认值
            if(symbolAttributeMap.isEmpty()) {
                //JDK8以上版本实现， 获取GenDTOMethod.reuse()的默认值，赋值到break变量
                List<Symbol.MethodSymbol> methodSymbols = (List)compound.getAnnotationType().asElement().getEnclosedElements();
                for(Symbol.MethodSymbol methodSymbol :  methodSymbols){
                    switch (methodSymbol.getSimpleName().toString()) {
                        case "jpaAnnotation" : jpaAnnotation = (boolean) methodSymbol.defaultValue.getValue();break;
                        case "chainSetter" : chainSetter = (boolean) methodSymbol.defaultValue.getValue();break;
                    }
                }
            }else {
                for (Map.Entry<Symbol.MethodSymbol, Attribute> entry : symbolAttributeMap.entrySet()) {
                    Object annotationValue = entry.getValue().getValue();
                    switch (entry.getKey().getSimpleName().toString()) {
                        case "jpaAnnotation" : jpaAnnotation = (boolean) annotationValue;break;
                        case "chainSetter" : chainSetter = (boolean) annotationValue;break;
                    }
                }
            }
        }
        if(!containsTableAnnotation && jpaAnnotation){
            //添加Table注解
            AnnotationSpec.Builder annotationSpecBuilder = AnnotationSpec.builder(Table.class);
//            annotationSpecBuilder.addMember("name", CodeBlock.builder().add("$S", "`"+POJOUtils.humpToLineFast(simpleName)+"`").build());
            annotationSpecBuilder.addMember("name", "$S", "`"+POJOUtils.humpToLineFast(simpleName)+"`");
            annotationSpecs.add(annotationSpecBuilder.build());
        }
        //构建父接口
        com.sun.tools.javac.util.List<Type> interfacesType = ((Symbol.ClassSymbol) element).getInterfaces();
        List<TypeName> typeNames = new ArrayList<>(interfacesType.size());
        for (Type type : interfacesType) {
            typeNames.add(ParameterizedTypeName.get(type));
        }
        interfaceBuilder.addAnnotations(annotationSpecs).addSuperinterfaces(typeNames);
        //构建属性和方法
        List<? extends Element> elements = element.getEnclosedElements();
        for(Element ele : elements) {
            //构建属性
            if (ele.getKind().equals(ElementKind.FIELD)) {
                messager.printMessage(Diagnostic.Kind.OTHER, "根据属性" + ele.getSimpleName() + "构建getter/setter.");
                buildMethodByField(interfaceBuilder, (Symbol.VarSymbol) ele, jpaAnnotation, chainSetter, ((Symbol.ClassSymbol) element).className());
            } else if (ele.getKind().equals(ElementKind.METHOD)) {
                //构建方法
//                messager.printMessage(Diagnostic.Kind.OTHER, "方法:"+t.getSimpleName());
//                Symbol.MethodSymbol methodSymbol = (Symbol.MethodSymbol) ele;
                //保留接口的其它方法
                buildMethod(interfaceBuilder, (Symbol.MethodSymbol) ele);
            }
        }
        return interfaceBuilder.build();
    }

    /**
     * 构建方法
     * @param builder
     * @param methodSymbol
     * @throws ClassNotFoundException
     */
    private void buildMethod(TypeSpec.Builder builder, Symbol.MethodSymbol methodSymbol) throws ClassNotFoundException {
        String methodSimpleName = methodSymbol.getSimpleName().toString();
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodSimpleName).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
        com.sun.tools.javac.util.List<Type> types = methodSymbol.getReturnType().getTypeArguments();
        //构建返回值
        if(types.isEmpty()) {
            Class<?> retType = ClassUtils.forName(methodSymbol.getReturnType().toString());
            methodBuilder.returns(retType);
        }else{
            //处理泛型返回值类型
            TypeName typeName = buildTypeName(methodSymbol);
            methodBuilder.returns(typeName);
//            com.sun.tools.javac.util.List<Symbol.VarSymbol> parameters = methodSymbol.getParameters();
//            methodBuilder.addParameter(typeName, POJOUtils.getBeanField(methodSimpleName));
        }

        //构建参数
        com.sun.tools.javac.util.List<Symbol.VarSymbol> parameters = methodSymbol.getParameters();
        if(null != parameters && !parameters.isEmpty()){
            for(Symbol.VarSymbol symbol : parameters) {
                methodBuilder.addParameter(buildTypeName(symbol), POJOUtils.getBeanField(methodSimpleName));
            }
        }

        //构建注解
        List<? extends AnnotationMirror> methodAnnotationMirrors = methodSymbol.getAnnotationMirrors();
        for(int i=0; i<methodAnnotationMirrors.size(); i++){
            AnnotationMirror annotationMirror = methodAnnotationMirrors.get(i);
            AnnotationSpec.Builder annotationSpecBuilder = AnnotationSpec.builder(Class.forName(annotationMirror.getAnnotationType().toString()));
            annotationMirror.getElementValues().entrySet().stream().forEach((t)->{
                Object value = t.getValue().getValue();
                if(value instanceof String){
                    annotationSpecBuilder.addMember(t.getKey().getSimpleName().toString(), CodeBlock.builder().add("$S", t.getValue().getValue()).build());
                }else if(t.getValue() instanceof Attribute.Enum){
                    annotationSpecBuilder.addMember(t.getKey().getSimpleName().toString(), CodeBlock.builder().add("$T.$L", ((Attribute.Enum) t.getValue()).type, t.getValue().getValue().toString()).build());
                }else{
                    annotationSpecBuilder.addMember(t.getKey().getSimpleName().toString(), CodeBlock.builder().add("$L", t.getValue().getValue()).build());
                }
            });
            methodBuilder.addAnnotation(annotationSpecBuilder.build());
        }
        String doc = elementUtils.getDocComment(methodSymbol);
        if(StringUtils.isNotBlank(doc)) {
            methodBuilder.addJavadoc(doc);
        }
        builder.addMethod(methodBuilder.build());
    }

    /**
     * 根据属性构建getter/setter方法
     * @param builder
     * @param varSymbol
     * @param jpaAnnotation
     * @param chainSetter
     * @throws ClassNotFoundException
     */
    private void buildMethodByField(TypeSpec.Builder builder, Symbol.VarSymbol varSymbol, Boolean jpaAnnotation, Boolean chainSetter, String typeClassName) throws ClassNotFoundException {
        String fieldSimpleName = varSymbol.getSimpleName().toString();
        //初始化getter
        String getterName = "get" + fieldSimpleName.substring(0, 1).toUpperCase() + fieldSimpleName.substring(1);
        MethodSpec.Builder getMethodBuilder = MethodSpec.methodBuilder(getterName).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
        if(jpaAnnotation) {
            //构建javax.persistence.Column注解
            AnnotationSpec.Builder columnAnnotationSpecBuilder = AnnotationSpec.builder(Column.class);
            StringBuilder fieldSimpleNameStringBuilder = new StringBuilder();
            columnAnnotationSpecBuilder.addMember("name", CodeBlock.builder().add("$S", fieldSimpleNameStringBuilder.append("`").append(POJOUtils.humpToLineFast(fieldSimpleName)).append("`").toString()).build());
            getMethodBuilder.addAnnotation(columnAnnotationSpecBuilder.build());
        }
        if("id".equals(fieldSimpleName) && jpaAnnotation){
            //构建javax.persistence.Id注解
            getMethodBuilder.addAnnotation(Id.class);
            //构建javax.persistence.GeneratedValue注解
            AnnotationSpec.Builder generatedValueAnnotationSpecBuilder = AnnotationSpec.builder(GeneratedValue.class);
            generatedValueAnnotationSpecBuilder.addMember("strategy", CodeBlock.builder().add("$T.$L", GenerationType.class, "IDENTITY").build());
            getMethodBuilder.addAnnotation(generatedValueAnnotationSpecBuilder.build());
        }
        //初始化setter
        String setterName = "set" + fieldSimpleName.substring(0, 1).toUpperCase() + fieldSimpleName.substring(1);

        MethodSpec.Builder setMethodBuilder = MethodSpec.methodBuilder(setterName).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
        if(chainSetter){
            setMethodBuilder.returns(ClassUtils.forName(typeClassName));
        }else{
            setMethodBuilder.returns(void.class);
        }
        com.sun.tools.javac.util.List<Type> types = varSymbol.asType().getTypeArguments();
        //没有泛型
        if(types.isEmpty()){
            //设置getter返回类型
            getMethodBuilder.returns(ClassUtils.forName(varSymbol.asType().tsym.toString()));
            //设置setter参数
            setMethodBuilder.addParameter(ClassUtils.forName(varSymbol.asType().tsym.toString()), fieldSimpleName);
        }else{
            //处理泛型类型
            TypeName typeName = buildTypeName(varSymbol);
            getMethodBuilder.returns(typeName);
            setMethodBuilder.addParameter(typeName, fieldSimpleName);
        }
        String doc = elementUtils.getDocComment(varSymbol);
        if(StringUtils.isNotBlank(doc)) {
            getMethodBuilder.addJavadoc(doc);
        }
        //构建getter和setter
        MethodSpec getterSpec = getMethodBuilder.build();
        builder.addMethod(getterSpec);
        MethodSpec setterSpec = setMethodBuilder.build();
        builder.addMethod(setterSpec);
    }

    /**
     * 构建泛型参数的TypeName对象
     * @param symbol
     * @return
     */
    private TypeName buildTypeName(Symbol symbol) throws ClassNotFoundException {
        boolean isVarSymbol = symbol instanceof Symbol.VarSymbol;
        //处理泛型类型
        String classNameStr = isVarSymbol ? symbol.asType().tsym.toString() : ((Symbol.MethodSymbol) symbol).getReturnType().getOriginalType().tsym.toString();
        ClassName className = ClassName.get(ClassUtils.forName(classNameStr));
        com.sun.tools.javac.util.List<Type> types = isVarSymbol ? symbol.asType().getTypeArguments() : ((Symbol.MethodSymbol) symbol).getReturnType().getTypeArguments();
        if(types.isEmpty()){
            return className;
        }
        List<TypeName> typeNames = new ArrayList<>(types.size());
        for(Type type : types){
            com.sun.tools.javac.util.List<Type> typeList = type.getTypeArguments();
            if(typeList.isEmpty()) {
                typeNames.add(ClassName.get(ClassUtils.forName(type.toString())));
            }else{
                typeNames.add(buildTypeName(type));
            }
        }
        return ParameterizedTypeName.get(className, typeNames.toArray(new TypeName[]{}));
    }

    private TypeName buildTypeName(Type type) throws ClassNotFoundException {
        com.sun.tools.javac.util.List<Type> typeList = type.getTypeArguments();
        if(typeList.isEmpty()) {
            return ClassName.get(ClassUtils.forName(type.toString()));
        }else{
            List<TypeName> typeNames = new ArrayList<>(typeList.size());
            ClassName className = ClassName.get(ClassUtils.forName(type.tsym.toString()));
            for(Type type1 : typeList){
                typeNames.add(buildTypeName(type1));
            }
            return ParameterizedTypeName.get(className, typeNames.toArray(new TypeName[]{}));
        }
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataions = new LinkedHashSet<String>();
        annotataions.add(GenDTOMethod.class.getCanonicalName());
        return annotataions;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return super.getSupportedSourceVersion();
    }
}