package com.mxny.ss.dto;

import com.google.common.collect.Lists;
import com.mxny.ss.exception.ParamErrorException;
import com.mxny.ss.util.POJOUtils;
import javassist.*;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * DTO工厂
 */
public class DTOFactory implements IDTOFactory {

    private static DTOFactory instance = new DTOFactory();
    //基类方法，不需要生成这些方法的字节码
    private static final List<Class> baseClasses ;

//    /**
//     * 静态内部类实现单例模式（线程安全，调用效率高，可以延时加载）
//     */
//    private static class DTOFactoryInstance {
//        private static final DTOFactory instance = new DTOFactory();
//    }

    private DTOFactory() {
    }

    public static DTOFactory getInstance() {
        return instance;
    }

    static ClassPool classPool = ClassPool.getDefault();

    static {
        baseClasses = Lists.newArrayList(IBaseDomain.class, IDTO.class, IStringDomain.class, ITaosDomain.class, IDomain.class);
        classPool.insertClassPath(new ClassClassPath(DTOFactory.class));
        classPool.importPackage("java.util.Map");
        classPool.importPackage("java.util.List");
        classPool.importPackage("java.util.HashMap");
        classPool.importPackage("java.lang.reflect.Field");
    }

    public static void i(){
        try {
            Class.forName("com.mxny.ss.java.Ac");
            Class.forName("com.mxny.ss.java.Power");
        }catch (Exception e){
            return;
        }
        DTOFactoryUtils.idtoFactory = getInstance();
    }

    @Override
    public void insertClassPath(ClassPath classPath){
        classPool.insertClassPath(classPath);
    }

    @Override
    public void importPackage(String packageName){
        classPool.importPackage(packageName);
    }

    @Override
    @SuppressWarnings(value = {"unchecked", "deprecation"})
    public void registerDTOInstance(AnnotationMetadata importingClassMetadata) {
        Map attributes = importingClassMetadata.getAnnotationAttributes(DTOScan.class.getCanonicalName());
        Set<String> packages = getBasePackages(attributes, importingClassMetadata.getClassName());
        String file = (String) attributes.get("file");
        registerDTOInstanceFromPackages(packages, file);
    }

    /**
     * 从指定的包注册DTO实例
     */
    @Override
    @SuppressWarnings(value = {"unchecked", "deprecation"})
    public void registerDTOInstanceFromPackages(Set<String> packages, String file) {
        Reflections reflections = new Reflections(packages);
        Set<Class<? extends IDTO>> classes = reflections.getSubTypesOf(IDTO.class);
        if (classes != null) {
            DTOInstance.useInstance = true;
            for (Class<? extends IDTO> dtoClass : classes) {
                if (!dtoClass.isInterface()) {
                    continue;
                }
                try {
                    CtClass ctClass = createCtClass(dtoClass);
                    if (StringUtils.isNotBlank(file)) {
                        ctClass.writeFile(file);
                    }
                    Class aClass = ctClass.toClass();
                    DTOInstance.CLASS_CACHE.put(dtoClass, (Class) aClass);
                    DTOInstance.INSTANCE_CACHE.put(dtoClass, (IDTO) aClass.newInstance());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 创建DTO接口的CtClass
     *
     * @param clazz
     * @param <T>
     * @return
     * @throws Exception
     */
    @Override
    @SuppressWarnings(value = {"unchecked", "deprecation"})
    public <T extends IDTO> CtClass createCtClass(Class<T> clazz) throws Exception {
        CtClass ctClass = null;
        if (!clazz.isInterface()) {
            throw new ParamErrorException("参数必须是接口");
        }
        if (!IDTO.class.isAssignableFrom(clazz)) {
            throw new ParamErrorException("参数必须实现IDTO接口");
        }
        if (IBaseDomain.class.isAssignableFrom(clazz)) {
            ctClass = createBaseDomainCtClass((Class<IBaseDomain>) clazz);
        } else if (IStringDomain.class.isAssignableFrom(clazz)) {
            ctClass = createStringDomainCtClass((Class<IStringDomain>) clazz);
        } else if (ITaosDomain.class.isAssignableFrom(clazz)) {
            ctClass = createTaosDomainCtClass((Class<ITaosDomain>) clazz);
        } else if (IDomain.class.isAssignableFrom(clazz)) {
            ctClass = createDomainCtClass((Class<IStringDomain>) clazz);
        } else {
            ctClass = createDTOCtClass(clazz);
        }
        return createDynaCtClass(clazz, ctClass);
    }

    /**
     * 动态递归创建CtClass
     *
     * @param clazz
     * @param <T>
     * @return
     * @throws Exception
     */
    @SuppressWarnings(value = {"unchecked", "deprecation"})
    private <T extends IDTO> CtClass createDynaCtClass(Class<T> clazz, CtClass ctClass) throws Exception {
        //IBaseDomain、IStringDomain或IDTO已经加载， IDomain不支持
        if (baseClasses.contains(clazz)) {
            return ctClass;
        }
        //非IDTO子类不加载
        if (!IDTO.class.isAssignableFrom(clazz)) {
            return ctClass;
        }
        //获取类的所有public方法
        Method[] allPublicMethods = clazz.getMethods();
        //先添加Field
        for (Method method : allPublicMethods) {
            Class<?> declaringClass = method.getDeclaringClass();
            //不处理基类接口的方法
            if(baseClasses.contains(declaringClass)){
                continue;
            }
            if (!POJOUtils.isGetMethod(method)) {
                continue;
            }
            String fieldName = POJOUtils.getBeanField(method);
            try {
                //找到已有属性就continue, 找不到属性抛异常,继续添加属性
                ctClass.getDeclaredField(fieldName);
                continue;
            } catch (NotFoundException e) {
            }
            //构建属性
            StringBuilder fieldStringBuilder = new StringBuilder();
            //默认get方法的返回值赋给属性,只方式只支持JDK8
            if(method.isDefault()){
                fieldStringBuilder.append("private ").append(method.getReturnType().getTypeName()).append(" ")
                        .append(fieldName).append(" = ").append("(")
                        .append(method.getReturnType().getTypeName())
                        .append(")com.mxny.ss.util.ReflectionUtils.invokeDefaultMethod(this, this.getClass().getInterfaces()[0].getMethod(\"")
                        .append(method.getName()).append("\", null), null);");
            }else {
                fieldStringBuilder.append("private ").append(method.getReturnType().getTypeName()).append(" ").append(fieldName).append(";").toString();
            }
            CtField ctField = CtField.make(fieldStringBuilder.toString(), ctClass);
            ctClass.addField(ctField);
        }
        // 再构建getter和setter方法
        for (Method method : allPublicMethods) {
            //方法不能重复，不然编译报错，这里无须打印日志，如aset, mget,getRows,getFields等方法都会在这里跳过
            if (ctClass.getDeclaredMethods(method.getName()).length > 0) {
                continue;
            }
            String fieldName = POJOUtils.getBeanField(method);
            if (POJOUtils.isGetMethod(method)) {
                //基础类型无法提供代理对象的值，因为无法区分是0还是null
                if (method.getReturnType().isPrimitive()) {
                    CtMethod method1 = CtMethod.make(new StringBuilder().append("public ").append(method.getReturnType().getTypeName()).append(" ").append(method.getName()).append("(){return this.").append(fieldName).append(";}").toString(), ctClass);
                    ctClass.addMethod(method1);
                }
//                else if (method.isDefault()) {
//                    String content = new StringBuilder().append("public ")
//                            .append(method.getReturnType().getTypeName()).append(" ")
//                            .append(method.getName()).append("(){").append("try {")
//                            .append("return this.").append(fieldName)
//                            .append(" == null ? $delegate.get(\"").append(fieldName)
//                            .append("\") == null ?").append("(")
//                            .append(method.getReturnType().getTypeName())
//                            .append(")com.mxny.ss.util.ReflectionUtils.invokeDefaultMethod(this, this.getClass().getInterfaces()[0].getMethod(\"")
//                            .append(method.getName()).append("\", null), null)")
//                            .append(":(").append(method.getReturnType().getTypeName())
//                            .append(")$delegate.get(\"").append(fieldName).append("\")")
//                            .append(" : this.").append(fieldName).append(";")
//                            .append("}catch (Throwable e){").append("return null;")
//                            .append("}").append("}").toString();
//                    CtMethod method1 = CtMethod.make(content, ctClass);
//                    ctClass.addMethod(method1);
//                }
                else {
                    CtMethod method1 = CtMethod.make(new StringBuilder().append("public ").append(method.getReturnType().getTypeName()).append(" ").append(method.getName()).append("(){return this.").append(fieldName).append(" == null ? (").append(method.getReturnType().getTypeName()).append(")$delegate.get(\"").append(fieldName).append("\") : this.").append(fieldName).append(";}").toString(), ctClass);
                    ctClass.addMethod(method1);
                }
            } else if (POJOUtils.isSetMethod(method)) {
                StringBuilder methodStr = new StringBuilder();
                if (method.getReturnType().equals(Void.TYPE)) {
                    methodStr.append("public void");
                }else if(method.getReturnType().equals(clazz)){
                    methodStr.append("public " + clazz.getName());
                }else{
                    throw new RuntimeException("setter方法返回值无法解析:"+method.getReturnType().getName());
                }
                methodStr.append(" ").append(method.getName()).append("(").append(method.getParameterTypes()[0].getTypeName()).append(" ").append(fieldName);
                //基本类型无法判断null
                if(method.getParameterTypes()[0].isPrimitive()){
                    methodStr.append("){this.");
                }else{
                    methodStr.append("){if(").append(fieldName).append(" == null){this.aset(\"").append(fieldName).append("\", null);}this.");
                }
                methodStr.append(fieldName).append(" = ").append(fieldName).append(";");
                if (method.getReturnType().equals(clazz)) {
                    methodStr.append("return this;");
                }
                methodStr.append("}");
                CtMethod method1 = CtMethod.make(methodStr.toString(), ctClass);
                ctClass.addMethod(method1);
            }
        }
//        Class<?>[] interfaces = clazz.getInterfaces();
//        if (interfaces != null) {
//            for (int i = 0; i < interfaces.length; i++) {
//                createDynaCtClass((Class) interfaces[i], ctClass);
//            }
//        }
        return ctClass;
    }

    @SuppressWarnings(value = {"unchecked", "deprecation"})
    private <T extends IDTO> CtClass createDTOCtClass(Class<T> clazz) throws Exception {
        CtClass intfCtClass = classPool.get(clazz.getName());
        CtClass creativeCtClass = classPool.get(ICreative.class.getName());
        CtClass implCtClass = classPool.makeClass(clazz.getName() + DTOInstance.SUFFIX);
        implCtClass.setInterfaces(new CtClass[]{intfCtClass, creativeCtClass});
        CtField $delegateField = CtField.make("private com.mxny.ss.dto.DTO $delegate;", implCtClass);
        implCtClass.addField($delegateField);
        CtConstructor defaultConstructor = new CtConstructor(null, implCtClass);
        defaultConstructor.setModifiers(Modifier.PUBLIC);
        defaultConstructor.setBody("{this.$delegate = new com.mxny.ss.dto.DTO();}");
        implCtClass.addConstructor(defaultConstructor);

        //添加fields属性，存储当前类和父类所有属性,key为属性名
        implCtClass.addField(CtField.make("private Map fields = null;", implCtClass));
        CtMethod getFieldsMethod = CtMethod.make(new StringBuilder().append("public Map getFields(){").append("if(fields == null){").append("fields = new HashMap();").append("List fieldList = org.apache.commons.lang3.reflect.FieldUtils.getAllFieldsList(getClass());").append("int size = fieldList.size();").append("for(int i=0; i<size; i++){").append("java.lang.reflect.Field field = (java.lang.reflect.Field)fieldList.get(i);").append("fields.put(field.getName(), field);").append("}").append("}").append("return fields;}").toString(), implCtClass);
        implCtClass.addMethod(getFieldsMethod);

        CtMethod agetMethod = CtMethod.make("public Object aget(String property){return $delegate.get(property);}", implCtClass);
        CtMethod agetAllMethod = CtMethod.make("public com.mxny.ss.dto.DTO aget(){return $delegate;}", implCtClass);

        CtMethod asetMethod = CtMethod.make(new StringBuilder().append("public void aset(String property, Object value){").append("Field field = (Field)getFields().get(property);").append("if(field != null){try{field.set(this, value);}catch(Exception e){e.printStackTrace();}}").append("this.$delegate.put(property, value);}").toString(), implCtClass);
        CtMethod asetAllMethod = CtMethod.make("public void aset(com.mxny.ss.dto.DTO dto){this.$delegate = dto;}", implCtClass);
        implCtClass.addMethod(agetMethod);
        implCtClass.addMethod(agetAllMethod);
        implCtClass.addMethod(asetMethod);
        implCtClass.addMethod(asetAllMethod);

        CtMethod mgetMethod = CtMethod.make("public Object mget(String property){return this.$delegate.getMetadata(property);}", implCtClass);
        CtMethod mgetAllMethod = CtMethod.make("public java.util.Map mget(){return this.$delegate.getMetadata();}", implCtClass);
        CtMethod msetMethod = CtMethod.make("public void mset(String property, Object value){this.$delegate.setMetadata(property, value);}", implCtClass);
        CtMethod msetAllMethod = CtMethod.make("public void mset(java.util.Map metadata){this.$delegate.setMetadata(metadata);}", implCtClass);
        implCtClass.addMethod(mgetMethod);
        implCtClass.addMethod(mgetAllMethod);
        implCtClass.addMethod(msetMethod);
        implCtClass.addMethod(msetAllMethod);

        CtMethod createInstanceMethod = CtMethod.make("public com.mxny.ss.dto.IDTO createInstance(){return new "+clazz.getName() + DTOInstance.SUFFIX+"();}", implCtClass);
        implCtClass.addMethod(createInstanceMethod);
        return implCtClass;
    }

    @SuppressWarnings(value = {"unchecked", "deprecation"})
    private <T extends IDomain> CtClass createDomainCtClass(Class<T> clazz) throws Exception {
        CtClass implCtClass = createDTOCtClass(clazz);
        //创建属性
        CtField pageField = CtField.make("private Integer page;", implCtClass);
        CtField rowsField = CtField.make("private Integer rows;", implCtClass);
        CtField sortField = CtField.make("private String sort;", implCtClass);
        CtField orderField = CtField.make("private String order;", implCtClass);

        implCtClass.addField(pageField);
        implCtClass.addField(rowsField);
        implCtClass.addField(sortField);
        implCtClass.addField(orderField);

        CtMethod getPageMethod = CtMethod.make("public Integer getPage(){return page==null?(Integer)aget(\"page\"):page;}", implCtClass);
        CtMethod setPageMethod = CtMethod.make("public void setPage(Integer page){this.page=page;}", implCtClass);

        CtMethod getRowsMethod = CtMethod.make("public Integer getRows(){return rows==null?(Integer)aget(\"rows\"):rows;}", implCtClass);
        CtMethod setRowsMethod = CtMethod.make("public void setRows(Integer rows){this.rows=rows;}", implCtClass);

        CtMethod getSortMethod = CtMethod.make("public String getSort(){return sort==null?(String)aget(\"sort\"):sort;}", implCtClass);
        CtMethod setSortMethod = CtMethod.make("public void setSort(String sort){this.sort=sort;}", implCtClass);

        CtMethod getOrderMethod = CtMethod.make("public String getOrder(){return order==null?(String)aget(\"order\"):order;}", implCtClass);
        CtMethod setOrderMethod = CtMethod.make("public void setOrder(String order){this.order=order;}", implCtClass);

        CtMethod getMetadataMethod = CtMethod.make("public java.util.Map getMetadata(){return mget();}", implCtClass);
        CtMethod setMetadataMethod = CtMethod.make("public void setMetadata(java.util.Map metadata){mset(metadata);}", implCtClass);

        CtMethod getMetadata2Method = CtMethod.make("public Object getMetadata(String key){return mget(key);}", implCtClass);
        CtMethod setMetadata2Method = CtMethod.make("public void setMetadata(String key, Object value){mset(key, value);}", implCtClass);

        CtMethod containsMetadataMethod = CtMethod.make("public Boolean containsMetadata(String key){return new Boolean(mget().containsKey(key));}", implCtClass);

        implCtClass.addMethod(getPageMethod);
        implCtClass.addMethod(setPageMethod);
        implCtClass.addMethod(getRowsMethod);
        implCtClass.addMethod(setRowsMethod);
        implCtClass.addMethod(getSortMethod);
        implCtClass.addMethod(setSortMethod);
        implCtClass.addMethod(getOrderMethod);
        implCtClass.addMethod(setOrderMethod);
        implCtClass.addMethod(getMetadataMethod);
        implCtClass.addMethod(setMetadataMethod);
        implCtClass.addMethod(getMetadata2Method);
        implCtClass.addMethod(setMetadata2Method);
        implCtClass.addMethod(containsMetadataMethod);
        return implCtClass;
    }

    @SuppressWarnings(value = {"unchecked", "deprecation"})
    private <T extends IStringDomain> CtClass createStringDomainCtClass(Class<T> clazz) throws Exception {
        CtClass implCtClass = createDomainCtClass(clazz);
        //创建属性
        CtField idField = CtField.make("private String id;", implCtClass);
        implCtClass.addField(idField);
        CtMethod getIdMethod = CtMethod.make("public String getId(){return id==null?(String)aget(\"id\"):id;}", implCtClass);
        CtMethod setIdMethod = CtMethod.make("public void setId(String id){this.id=id;}", implCtClass);
        implCtClass.addMethod(getIdMethod);
        implCtClass.addMethod(setIdMethod);
        return implCtClass;
    }

    @SuppressWarnings(value = {"unchecked", "deprecation"})
    private <T extends IBaseDomain> CtClass createBaseDomainCtClass(Class<T> clazz) throws Exception {
        CtClass implCtClass = createDomainCtClass(clazz);
        //创建属性
        CtField idField = CtField.make("private Long id;", implCtClass);
        implCtClass.addField(idField);
        CtMethod getIdMethod = CtMethod.make("public Long getId(){return id==null?(Long)aget(\"id\"):id;}", implCtClass);
        CtMethod setIdMethod = CtMethod.make("public void setId(Long id){this.id=id;}", implCtClass);
        implCtClass.addMethod(getIdMethod);
        implCtClass.addMethod(setIdMethod);
        return implCtClass;
    }

    @SuppressWarnings(value = {"unchecked", "deprecation"})
    private <T extends ITaosDomain> CtClass createTaosDomainCtClass(Class<T> clazz) throws Exception {
        CtClass implCtClass = createDomainCtClass(clazz);
        //创建属性
        CtField tsField = CtField.make("private Long ts;", implCtClass);
        implCtClass.addField(tsField);
        CtMethod getTsMethod = CtMethod.make("public Long getTs(){return ts==null?(Long)aget(\"ts\"):ts;}", implCtClass);
        CtMethod setTsMethod = CtMethod.make("public void setTs(Long ts){this.ts=ts;}", implCtClass);
        implCtClass.addMethod(getTsMethod);
        implCtClass.addMethod(setTsMethod);
        return implCtClass;
    }

    @SuppressWarnings(value = {"unchecked", "deprecation"})
    private Set<String> getBasePackages(Map attributes, String className) {
        HashSet basePackages = new HashSet();
        String[] values = (String[]) attributes.get("value");
        int valuesLength = values.length;
        int index;
        String pkg;
        for (index = 0; index < valuesLength; ++index) {
            pkg = values[index];
            if (StringUtils.isNotBlank(pkg)) {
                basePackages.add(pkg);
            }
        }
        if (basePackages.isEmpty()) {
            basePackages.add(ClassUtils.getPackageName(className));
        }
        return basePackages;
    }

}