package com.mxny.ss.processor.processor;

import com.mxny.ss.processor.annotation.GenDTOMethod;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

//@AutoService(Processor.class)
//@SupportedAnnotationTypes({"com.mxny.ss.processor.annotation.Serialize"})
//@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MyProcessor extends AbstractProcessor {

    //类名的前缀、后缀
    public static final String SUFFIX = "AutoGenerate";
    public static final String PREFIX = "My_";

    private Types typeUtils;

    // 元素操作的辅助类
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    /**
     * 如果返回 true，则这些注解由此注解来处理，后续其它的 Processor 无需再处理它们；如果返回 false，则这些注解未在此Processor中处理并，那么后续 Processor 可以继续处理它们。
     * @param annotations
     * @param env
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
//        if (env.processingOver()) {
//            try {
//                CtClass ctClass = JavassistUtils.createCtClass(IBaseDomain.class);
//                ctClass.writeFile("E:/workspace/dili-workspace/dili-uap/uap/target/classes/");
//            } catch (Exception e) {
//                messager.printMessage(Diagnostic.Kind.WARNING, "Exception:" + e.getMessage());
//            }
//            return true;
//        }
//        if (!env.processingOver()) {
//            return false;
//        }
        messager.printMessage(Diagnostic.Kind.WARNING, "env.processingOver():" + env.processingOver());
        messager.printMessage(Diagnostic.Kind.WARNING, "annotations:" + annotations.size());
        for (TypeElement typeElement : annotations) {
            for (Element e : env.getElementsAnnotatedWith(typeElement)) {
                //打印
                messager.printMessage(Diagnostic.Kind.WARNING, "Printing:" + e.toString());
                messager.printMessage(Diagnostic.Kind.WARNING, "Printing:" + e.getSimpleName());
                messager.printMessage(Diagnostic.Kind.WARNING, "Printing:" + e.getEnclosedElements().toString());

                //获取注解
                GenDTOMethod annotation = e.getAnnotation(GenDTOMethod.class);
                //获取元素名并将其首字母大写
                String name = e.getSimpleName().toString();
                char c = Character.toUpperCase(name.charAt(0));
                name = String.valueOf(c + name.substring(1));
                //包裹注解元素的元素, 也就是其父元素, 比如注解了成员变量或者成员函数, 其上层就是该类
                Element enclosingElement = e.getEnclosingElement();
                //获取父元素的全类名,用来生成包名
                String enclosingQualifiedname;
                if (enclosingElement instanceof PackageElement) {
                    enclosingQualifiedname = ((PackageElement) enclosingElement).getQualifiedName().toString();
                } else {
                    enclosingQualifiedname = ((TypeElement) enclosingElement).getQualifiedName().toString();
                }

                processingEnv.getMessager().printMessage(Diagnostic.Kind.MANDATORY_WARNING, "Found " + e);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.MANDATORY_WARNING, "SOURCE VERSION:" + processingEnv.getSourceVersion().name());

                try {
                    FileObject fileObject = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, processingEnv.getElementUtils().getPackageOf(e).toString(), e.getSimpleName() + ".found", e);
                    messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "fileObject.uri: " + fileObject.toUri());
                    String rawPath = fileObject.toUri().getRawPath();
                    String dirPath = rawPath.substring(0, rawPath.lastIndexOf("/target/classes/")+1);
                    messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "dirPath: " + dirPath);
                    gen(new File(dirPath+"src/main/java/"));
                    fileObject.openOutputStream().close();
//                    boolean del = fileObject.delete();
//                    messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "fileObject.del: " + del);
//                    CtClass ctClass = JavassistUtils.createCtClass(IBaseDomain.class);
//                    ctClass.writeFile("E:/workspace/dili-workspace/dili-uap/uap/target/classes/");
                    return true;
                } catch (Exception x) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, x.toString(), e);
                }

//                try {
//                    //生成包名
//                    String generatePackageName = enclosingQualifiedname.substring(0, enclosingQualifiedname.lastIndexOf("."));
//                    // 生成的类名
//                    String genarateClassName = PREFIX + enclosingElement.getSimpleName() + SUFFIX;
//                    //创建Java 文件
//                    JavaFileObject f = processingEnv.getFiler().createClassFile(genarateClassName);
//                    // 在控制台输出文件路径
//                    messager.printMessage(Diagnostic.Kind.WARNING, "Printing f.toUri(): " + f.toUri());
//                    Writer w = f.openWriter();
//                    try {
//                        PrintWriter pw = new PrintWriter(w);
//                        pw.println("package " + generatePackageName + ";");
//                        pw.println("\npublic class " + genarateClassName + " { ");
//                        pw.println("\n    /** 打印值 */");
//                        pw.println("    public static void print" + name + "() {");
//                        pw.println("        // 注解的父元素: " + enclosingElement.toString());
//                        pw.println("        System.out.println(\"代码生成的路径: " + f.toUri() + "\");");
//                        pw.println("        System.out.println(\"注解的元素: " + e.toString() + "\");");
//                        pw.println("        System.out.println(\"注解的版本: " + "1.8" + "\");");
//                        pw.println("        System.out.println(\"注解的作者: " + "wm" + "\");");
//                        pw.println("        System.out.println(\"注解的日期: " + "20190919" + "\");");
//
//                        pw.println("    }");
//                        pw.println("}");
//                        pw.flush();
//                    } finally {
//                        w.close();
//                    }
//                } catch (IOException e1) {
//                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
//                            e1.toString());
//                }
            }
        }
        return true;
    }

    private void gen(File file) throws IOException {
        MethodSpec main = MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args")
                .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
                .build();

        TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(main)
                .build();

        JavaFile javaFile = JavaFile.builder("com.mxny.ss.dto", helloWorld)
                .build();

        javaFile.writeTo(file);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataions = new LinkedHashSet<String>();
        annotataions.add(GenDTOMethod.class.getCanonicalName());
        return annotataions;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }
}