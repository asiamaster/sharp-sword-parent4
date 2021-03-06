package com.mxny.ss.mbg;

import com.mxny.ss.domain.BaseOutput;
import com.mxny.ss.dto.IBaseDomain;
import com.mxny.ss.dto.IDTO;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.JavaFormatter;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.internal.util.StringUtility;
import org.mybatis.generator.internal.util.messages.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by asiam on 2017/4/10 0007.
 */
public class MyControllerPlugin extends PluginAdapter {

    private final String LINE_SEPARATOR = System.getProperty("line.separator");

    public MyControllerPlugin() {
    }

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
    }

    @Override
    public boolean validate(List<String> warnings) {
        boolean valid = true;
        if(!StringUtility.stringHasValue(this.properties.getProperty("targetProject"))) {
            warnings.add(Messages.getString("ValidationError.18", "MyControllerPlugin", "targetProject"));
            valid = false;
        }
        if(!StringUtility.stringHasValue(this.properties.getProperty("targetPackage"))) {
            warnings.add(Messages.getString("ValidationError.18", "MyControllerPlugin", "targetPackage"));
            valid = false;
        }
        return valid;
    }

    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        String controllerSuperClass = properties.getProperty("controllerSuperClass");
        String controllerSuperInterface = properties.getProperty("controllerSuperInterface");
        String controllerTargetDir = properties.getProperty("targetProject");
        String controllerTargetPackage = properties.getProperty("targetPackage");

        JavaFormatter javaFormatter = context.getJavaFormatter();
        List<GeneratedJavaFile> mapperJavaFiles = new ArrayList<GeneratedJavaFile>();
        for (GeneratedJavaFile javaFile : introspectedTable.getGeneratedJavaFiles()) {
            CompilationUnit unit = javaFile.getCompilationUnit();
            //??????Bean java??????
            FullyQualifiedJavaType baseModelJavaType = unit.getType();
            String shortName = baseModelJavaType.getShortName();
            if (shortName.endsWith("Mapper")) {
                continue;
            }
            GeneratedJavaFile controllerJavafile = null;
            if (!shortName.endsWith("Example")) {
                //??????controller???
                TopLevelClass clazz = new TopLevelClass(controllerTargetPackage + "." + shortName + "Controller");
                clazz.setVisibility(JavaVisibility.PUBLIC);
                addJavaDocLine(clazz);
                addSuperInterface(clazz, controllerSuperInterface);
                addSuperClass(clazz, controllerSuperClass);
                addAutowiredService(clazz, controllerTargetPackage, shortName);

                //?????????????????????
//                clazz.addImportedType(Api.class.getName());
//                clazz.addAnnotation("@Api(\"/"+StringUtils.uncapitalize(shortName)+"\")");
                clazz.addImportedType("org.springframework.stereotype.Controller");
                clazz.addAnnotation("@Controller");
                clazz.addImportedType("org.springframework.web.bind.annotation.RequestMapping");
                clazz.addAnnotation("@RequestMapping(\"/"+StringUtils.uncapitalize(shortName)+"\")");

                //import?????????
                clazz.addImportedType(unit.getType());

                addCRUDMethod(clazz, unit);

                //????????????controller???
                controllerJavafile = new GeneratedJavaFile(clazz, controllerTargetDir, javaFormatter);
                mapperJavaFiles.add(controllerJavafile);
            }
        }
        return mapperJavaFiles;
    }

    private void addSuperInterface(TopLevelClass clazz, String controllerSuperInterface){
        //???????????????controllerSuperInterface??????????????????????????????
        if (StringUtility.stringHasValue(controllerSuperInterface)) {
            //??????controllerSuperInterface??????
            FullyQualifiedJavaType controllerSuperInterfaceType = new FullyQualifiedJavaType(controllerSuperInterface);
            //import controllerSuperInterface??????
            clazz.addImportedType(controllerSuperInterfaceType);
            //??????controllerSuperInterface??????
            clazz.addSuperInterface(controllerSuperInterfaceType);
        }
    }

    private void addSuperClass(TopLevelClass clazz, String controllerSuperClass){
        //???????????????controllerSuperClass??????????????????????????????
        if (StringUtility.stringHasValue(controllerSuperClass)) {
            FullyQualifiedJavaType controllerSuperType = new FullyQualifiedJavaType(controllerSuperClass);
            //import controllerSuperType???
            clazz.addImportedType(controllerSuperType);
            clazz.setSuperClass(controllerSuperType);
        }
    }

    private void addAutowiredService(TopLevelClass clazz, String controllerTargetPackage, String shortName){
        String servicePackage = controllerTargetPackage.substring(0, controllerTargetPackage.lastIndexOf('.'))+".service";
        String serviceClass = servicePackage+"."+shortName+"Service";
        clazz.addImportedType(serviceClass);
        FullyQualifiedJavaType serviceType = new FullyQualifiedJavaType(serviceClass);
        Field field = new Field(StringUtils.uncapitalize(shortName)+"Service", serviceType);
        field.addAnnotation("@Autowired");
        clazz.addField(field);
        clazz.addImportedType(Autowired.class.getName());
    }

    private void addJavaDocLine(TopLevelClass clazz){
        clazz.addJavaDocLine("/**");
        clazz.addJavaDocLine(" * ???MyBatis Generator??????????????????");
        StringBuilder sb = new StringBuilder();
        sb.append(" * This file was generated on ");
        sb.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        sb.append('.');
        clazz.addJavaDocLine(sb.toString());
        clazz.addJavaDocLine(" */");
    }

    //????????????????????????
    private void addCRUDMethod(TopLevelClass clazz, CompilationUnit unit){
        if(!isDTO(unit)){
            clazz.addImportedType(ModelAttribute.class.getName());
        }
        clazz.addImportedType(ModelMap.class.getName());
        clazz.addImportedType(BaseOutput.class.getName());
        clazz.addImportedType(ResponseBody.class.getName());
        clazz.addImportedType(List.class.getName());
        clazz.addImportedType(RequestMethod.class.getName());
//        clazz.addImportedType(ApiOperation.class.getName());
//        clazz.addImportedType(ApiImplicitParams.class.getName());
//        clazz.addImportedType(ApiImplicitParam.class.getName());

        addIndexMethod(clazz, unit);
//        addListMethod(clazz, unit);
        addListPageMethod(clazz, unit);
        addInsertMethod(clazz, unit);
        addUpdateMethod(clazz, unit);
        addDeleteMethod(clazz, unit);
    }

    //??????index??????
    private void addIndexMethod(TopLevelClass clazz, CompilationUnit unit){
        Method listMethod = new Method("index");
        FullyQualifiedJavaType modelMapType = new FullyQualifiedJavaType("org.springframework.ui.ModelMap");
        listMethod.addParameter(0, new Parameter(modelMapType, "modelMap"));
        listMethod.setReturnType(new FullyQualifiedJavaType("java.lang.String"));
        listMethod.setVisibility(JavaVisibility.PUBLIC);
        List<String> bodyLines = new ArrayList<>();
        String returnLine = "return \""+StringUtils.uncapitalize(unit.getType().getShortName())+"/index\";";
        bodyLines.add(returnLine);
        listMethod.addBodyLines(bodyLines);

//        listMethod.addAnnotation("@ApiOperation(\"?????????" + unit.getType().getShortName() + "??????\")");
        listMethod.addAnnotation("@RequestMapping(value=\"/index.html\", method = RequestMethod.GET)");

        listMethod.addJavaDocLine("/**");
        listMethod.addJavaDocLine(" * ?????????" + unit.getType().getShortName() + "??????");
        listMethod.addJavaDocLine(" * @param modelMap");
        listMethod.addJavaDocLine(" * @return String");
        listMethod.addJavaDocLine(" */");
        clazz.addMethod(listMethod);
    }

    //??????list??????
    private void addListMethod(TopLevelClass clazz, CompilationUnit unit){
        Method listMethod = new Method("list");
        FullyQualifiedJavaType baseModelJavaType = unit.getType();
        Parameter entityParameter = new Parameter(baseModelJavaType, StringUtils.uncapitalize(baseModelJavaType.getShortName()));
        if(!isDTO(unit)){
            entityParameter.addAnnotation("@ModelAttribute");
        }
        listMethod.addParameter(0, entityParameter);
        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType("@ResponseBody List");
        returnType.addTypeArgument(baseModelJavaType);
        listMethod.setReturnType(returnType);
        listMethod.setVisibility(JavaVisibility.PUBLIC);
        List<String> bodyLines = new ArrayList<>();
//        String modelPutLines = "modelMap.put(\"list\", "+StringUtils.uncapitalize(baseModelJavaType.getShortName())+"Service.list("+entityParameter.getName()+"));";
//        bodyLines.add(modelPutLines);
        String returnLine = "return "+StringUtils.uncapitalize(baseModelJavaType.getShortName())+"Service.list("+entityParameter.getName()+");";
        bodyLines.add(returnLine);
        listMethod.addBodyLines(bodyLines);

//        listMethod.addAnnotation("@ApiOperation(value=\"??????" + baseModelJavaType.getShortName() + "\", notes = \"??????"+baseModelJavaType.getShortName() + "?????????????????????\")");
//        StringBuilder sb = new StringBuilder();
//        sb.append("@ApiImplicitParams({"+LINE_SEPARATOR);
//        sb.append("\t\t@ApiImplicitParam(name=\"" + baseModelJavaType.getShortName() + "\", paramType=\"form\", value = \""+ baseModelJavaType.getShortName() +"???form??????\", required = false, dataType = \"string\")"+LINE_SEPARATOR);
//        sb.append("\t})");
//        listMethod.addAnnotation(sb.toString());
        listMethod.addAnnotation("@RequestMapping(value=\"/list.action\", method = {RequestMethod.GET, RequestMethod.POST})");

        listMethod.addJavaDocLine("/**");
        listMethod.addJavaDocLine(" * ??????"+baseModelJavaType.getShortName() + "?????????????????????");
        listMethod.addJavaDocLine(" * @param " + StringUtils.uncapitalize(baseModelJavaType.getShortName()));
        listMethod.addJavaDocLine(" * @return List<"+baseModelJavaType.getShortName()+">");
        listMethod.addJavaDocLine(" */");
        clazz.addMethod(listMethod);
    }

    //??????listPage??????
    private void addListPageMethod(TopLevelClass clazz, CompilationUnit unit){
        FullyQualifiedJavaType baseModelJavaType = unit.getType();
        Method listPageMethod = new Method("listPage");
        Parameter entityParameter = new Parameter(baseModelJavaType, StringUtils.uncapitalize(baseModelJavaType.getShortName()));
        if(!isDTO(unit)){
            entityParameter.addAnnotation("@ModelAttribute");
        }
        listPageMethod.addParameter(0, entityParameter);
        FullyQualifiedJavaType exception = new FullyQualifiedJavaType("Exception");
        listPageMethod.addException(exception);
        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType("@ResponseBody String");
        listPageMethod.setReturnType(returnType);
        listPageMethod.setVisibility(JavaVisibility.PUBLIC);
        List<String> bodyLines = new ArrayList<>();
        String returnLine = "return "+StringUtils.uncapitalize(baseModelJavaType.getShortName())+"Service.listEasyuiPageByExample("+entityParameter.getName()+", true).toString();";
        bodyLines.add(returnLine);
        listPageMethod.addBodyLines(bodyLines);

//        listPageMethod.addAnnotation("@ApiOperation(value=\"????????????" + baseModelJavaType.getShortName() + "\", notes = \"????????????"+baseModelJavaType.getShortName() + "?????????easyui????????????\")");
//        StringBuilder sb = new StringBuilder();
//        sb.append("@ApiImplicitParams({"+LINE_SEPARATOR);
//        sb.append("\t\t@ApiImplicitParam(name=\"" + baseModelJavaType.getShortName() + "\", paramType=\"form\", value = \""+ baseModelJavaType.getShortName() +"???form??????\", required = false, dataType = \"string\")"+LINE_SEPARATOR);
//        sb.append("\t})");
//        listPageMethod.addAnnotation(sb.toString());
        listPageMethod.addAnnotation("@RequestMapping(value=\"/listPage.action\", method = {RequestMethod.GET, RequestMethod.POST})");

        listPageMethod.addJavaDocLine("/**");
        listPageMethod.addJavaDocLine(" * ????????????"+baseModelJavaType.getShortName() + "?????????easyui????????????");
        listPageMethod.addJavaDocLine(" * @param " + StringUtils.uncapitalize(baseModelJavaType.getShortName()));
        listPageMethod.addJavaDocLine(" * @return String");
        listPageMethod.addJavaDocLine(" * @throws Exception");
        listPageMethod.addJavaDocLine(" */");
        clazz.addMethod(listPageMethod);
    }

    //??????insert??????
    private void addInsertMethod(TopLevelClass clazz, CompilationUnit unit){
        FullyQualifiedJavaType baseModelJavaType = unit.getType();
        Method method = new Method("insert");
        Parameter entityParameter = new Parameter(baseModelJavaType, StringUtils.uncapitalize(baseModelJavaType.getShortName()));
        if(!isDTO(unit)){
            entityParameter.addAnnotation("@ModelAttribute");
        }
        method.addParameter(0, entityParameter);
        method.setReturnType(new FullyQualifiedJavaType("@ResponseBody BaseOutput"));
        method.setVisibility(JavaVisibility.PUBLIC);
        List<String> bodyLines = new ArrayList<>();
        String contentLine1 = StringUtils.uncapitalize(baseModelJavaType.getShortName())+"Service.insertSelective("+entityParameter.getName()+");";
        String returnLine = "return BaseOutput.success(\"????????????\");";
        bodyLines.add(contentLine1);
        bodyLines.add(returnLine);
        method.addBodyLines(bodyLines);

//        method.addAnnotation("@ApiOperation(\"??????" + baseModelJavaType.getShortName() + "\")");
//        StringBuilder sb = new StringBuilder();
//        sb.append("@ApiImplicitParams({"+LINE_SEPARATOR);
//        sb.append("\t\t@ApiImplicitParam(name=\"" + baseModelJavaType.getShortName() + "\", paramType=\"form\", value = \""+ baseModelJavaType.getShortName() +"???form??????\", required = true, dataType = \"string\")"+LINE_SEPARATOR);
//        sb.append("\t})");
//        method.addAnnotation(sb.toString());
        method.addAnnotation("@RequestMapping(value=\"/insert.action\", method = {RequestMethod.GET, RequestMethod.POST})");

        method.addJavaDocLine("/**");
        method.addJavaDocLine(" * ??????"+baseModelJavaType.getShortName() );
        method.addJavaDocLine(" * @param " + StringUtils.uncapitalize(baseModelJavaType.getShortName()));
        method.addJavaDocLine(" * @return BaseOutput");
        method.addJavaDocLine(" */");
        clazz.addMethod(method);
    }

    //??????update??????
    private void addUpdateMethod(TopLevelClass clazz, CompilationUnit unit){
        FullyQualifiedJavaType baseModelJavaType = unit.getType();
        Method method = new Method("update");
        Parameter entityParameter = new Parameter(baseModelJavaType, StringUtils.uncapitalize(baseModelJavaType.getShortName()));
        if(!isDTO(unit)){
            entityParameter.addAnnotation("@ModelAttribute");
        }
        method.addParameter(0, entityParameter);
        method.setReturnType(new FullyQualifiedJavaType("@ResponseBody BaseOutput"));
        method.setVisibility(JavaVisibility.PUBLIC);
        List<String> bodyLines = new ArrayList<>();
        String contentLine1 = StringUtils.uncapitalize(baseModelJavaType.getShortName())+"Service.updateSelective("+entityParameter.getName()+");";
        String returnLine = "return BaseOutput.success(\"????????????\");";
        bodyLines.add(contentLine1);
        bodyLines.add(returnLine);
        method.addBodyLines(bodyLines);

//        method.addAnnotation("@ApiOperation(\"??????" + baseModelJavaType.getShortName() + "\")");
//        StringBuilder sb = new StringBuilder();
//        sb.append("@ApiImplicitParams({"+LINE_SEPARATOR);
//        sb.append("\t\t@ApiImplicitParam(name=\"" + baseModelJavaType.getShortName() + "\", paramType=\"form\", value = \""+ baseModelJavaType.getShortName() +"???form??????\", required = true, dataType = \"string\")"+LINE_SEPARATOR);
//        sb.append("\t})");
//        method.addAnnotation(sb.toString());
        method.addAnnotation("@RequestMapping(value=\"/update.action\", method = {RequestMethod.GET, RequestMethod.POST})");

        method.addJavaDocLine("/**");
        method.addJavaDocLine(" * ??????"+baseModelJavaType.getShortName() );
        method.addJavaDocLine(" * @param " + StringUtils.uncapitalize(baseModelJavaType.getShortName()));
        method.addJavaDocLine(" * @return BaseOutput");
        method.addJavaDocLine(" */");
        clazz.addMethod(method);
    }

    //??????delete??????
    private void addDeleteMethod(TopLevelClass clazz, CompilationUnit unit){
        FullyQualifiedJavaType baseModelJavaType = unit.getType();
        Method method = new Method("delete");
        Parameter entityParameter = new Parameter(new FullyQualifiedJavaType("Long"), "id");
        method.addParameter(0, entityParameter);
        method.setReturnType(new FullyQualifiedJavaType("@ResponseBody BaseOutput"));
        method.setVisibility(JavaVisibility.PUBLIC);
        List<String> bodyLines = new ArrayList<>();
        String contentLine1 = StringUtils.uncapitalize(baseModelJavaType.getShortName())+"Service.delete("+entityParameter.getName()+");";
        String returnLine = "return BaseOutput.success(\"????????????\");";
        bodyLines.add(contentLine1);
        bodyLines.add(returnLine);
        method.addBodyLines(bodyLines);

//        method.addAnnotation("@ApiOperation(\"??????" + baseModelJavaType.getShortName() + "\")");
//        StringBuilder sb = new StringBuilder();
//        sb.append("@ApiImplicitParams({"+LINE_SEPARATOR);
//        sb.append("\t\t@ApiImplicitParam(name=\"id\", paramType=\"form\", value = \""+ baseModelJavaType.getShortName() +"?????????\", required = true, dataType = \"long\")"+LINE_SEPARATOR);
//        sb.append("\t})");
//        method.addAnnotation(sb.toString());
        method.addAnnotation("@RequestMapping(value=\"/delete.action\", method = {RequestMethod.GET, RequestMethod.POST})");

        method.addJavaDocLine("/**");
        method.addJavaDocLine(" * ??????"+baseModelJavaType.getShortName() );
        method.addJavaDocLine(" * @param id");
        method.addJavaDocLine(" * @return BaseOutput");
        method.addJavaDocLine(" */");
        clazz.addMethod(method);
    }

    //???????????????DTO??????
    private boolean isDTO(CompilationUnit unit){
        Set<FullyQualifiedJavaType> fullyQualifiedJavaTypes = unit.getSuperInterfaceTypes();
        if(fullyQualifiedJavaTypes.isEmpty()) {
            return false;
        }
        for(FullyQualifiedJavaType fullyQualifiedJavaType : fullyQualifiedJavaTypes) {
            if (fullyQualifiedJavaType.getFullyQualifiedName().equals(IBaseDomain.class.getName()) || fullyQualifiedJavaType.getFullyQualifiedName().equals(IDTO.class.getName())){
                return true;
            }
        }
        return false;
    }

}
