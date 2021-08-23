package com.mxny.ss.base;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.mxny.ss.dao.ExampleExpand;
import com.mxny.ss.domain.BaseDomain;
import com.mxny.ss.domain.EasyuiPageOutput;
import com.mxny.ss.domain.annotation.Func;
import com.mxny.ss.domain.annotation.Like;
import com.mxny.ss.domain.annotation.Operator;
import com.mxny.ss.domain.annotation.SqlOperator;
import com.mxny.ss.dto.*;
import com.mxny.ss.exception.DataErrorException;
import com.mxny.ss.exception.ParamErrorException;
import com.mxny.ss.metadata.annotation.TaosTag;
import com.mxny.ss.util.CamelTool;
import com.mxny.ss.util.DateUtils;
import com.mxny.ss.util.LambadaTools;
import com.mxny.ss.util.POJOUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.mybatis.spring.MyBatisSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.IDynamicTableName;

import javax.persistence.*;
import java.lang.reflect.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * TDengine数据库基础服务
 * @param <T>
 */
public abstract class BaseTaosService<T extends ITaosDomain> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseTaosService.class);

    @Autowired
    private MyMapper<T> mapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    /**
     * 如果不使用通用mapper，可以自行在子类覆盖getDao方法
     */
    public MyMapper<T> getDao(){
        return this.mapper;
    }

    /**
     * 普通插入, 无值时将插入null
     * 例:INSERT INTO d1001 (ts, current, phase) VALUES ('2021-07-13 14:06:33.196', null, 0.31);
     * @param t
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public int insert(T t) {
        return getDao().insert(t);
    }

    /**
     * 普通选择性插入，表不存在时将抛出异常
     * 例:INSERT INTO d1001 (ts, current, phase) VALUES ('2021-07-13 14:06:33.196', 10.27, 0.31);
     * @param t
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public int insertSelective(T t) {
        return getDao().insertSelective(t);
    }

    /**
     * 支持tag的插入，表不存在时将自动创建
     * 例:INSERT INTO d21003 USING meters (location, groupdId) TAGS ('Beijing.Chaoyang', 2) (ts, current, phase) VALUES ('2021-07-13 14:06:34.255', 10.27, 0.31);
     * @param t
     */
    @Transactional(rollbackFor = Exception.class)
    public void insertSelectiveByTags(T t) {
        jdbcTemplate.execute(buildInsertSql(Lists.newArrayList(t)));
    }

    /**
     * 批量插入
     * 该方法不再支持动态代理DTO
     * 例:
     * INSERT INTO d21001 USING meters TAGS ('Beijing.Chaoyang', 2) VALUES ('2021-07-13 14:06:34.630', 10.2, 219, 0.32) ('2021-07-13 14:06:35.779', 10.15, 217, 0.33)
     *             d21002 USING meters (groupdId) TAGS (2) VALUES ('2021-07-13 14:06:34.255', 10.15, 217, 0.33)
     *             d21003 USING meters (groupdId) TAGS (2) (ts, current, phase) VALUES ('2021-07-13 14:06:34.255', 10.27, 0.31);
     * @param list
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchInsert(List<T> list) {
        if(org.apache.commons.collections.CollectionUtils.isEmpty(list)){
            return;
        }
        jdbcTemplate.execute(buildInsertSql(list));
    }

    public T get(Long key) {
        return getDao().selectByPrimaryKey(key);
    }

    /**
     * 根据实体查询
     * @param condtion 查询条件
     * @return
     */
    
    public List<T> list(T condtion) {
        return getDao().select(condtion);
    }

    //设置默认bean
    private T getDefaultBean(Class tClazz){
        T domain = null;
        if(tClazz.isInterface()){
            domain = DTOUtils.newInstance((Class<T>)tClazz);
        }else{
            try {
                domain = (T)tClazz.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        }
        return domain;
    }

    /**
     * 指定要查询的属性列 - 这里会自动映射到表字段
     *
     * @param domain
     * @param entityClass
     * @return
     */
    public ExampleExpand createExample(T domain, Class<?> entityClass) {
        ExampleExpand exampleExpand = ExampleExpand.of(entityClass);
        if(!(domain instanceof ITaosTableDomain)){
            return exampleExpand;
        }
        Class<?> domainClass = DTOUtils.getDTOClass(domain);
        ITaosTableDomain iTaosTableDomain =((ITaosTableDomain) domain);
        //这里构建Example，并设置selectColumns
        Set<String> columnsSet = iTaosTableDomain.getSelectColumns();
        if(columnsSet == null){
            columnsSet = new HashSet<>();
            Method[] methods = domainClass.getMethods();
            //有Func注解的函数
            List<Method> funcMethods = new ArrayList<>();
            //先找出所有Func注解
            for (Method method : methods) {
                if(!POJOUtils.isGetMethod(method)){
                    continue;
                }
                Func func = method.getAnnotation(Func.class);
                if (func != null) {
                    funcMethods.add(method);
                }
            }
            //select的列中只包含有函数注解的
            if (!funcMethods.isEmpty()) {
                for (Method funcMethod : funcMethods) {
                    String columnName = getColumnName(funcMethod);
                    Func func = funcMethod.getAnnotation(Func.class);
                    String alias = StringUtils.isBlank(func.alias()) ? columnName : func.alias();
                    columnsSet.add(new StringBuilder().append(func.value()).append("(").append(columnName).append(") as ").append(alias).toString());
                }
            }else {
                //先计算出需要get的方法
                for (Method method : methods) {
                    if (!POJOUtils.isGetMethod(method)) {
                        continue;
                    }
                    Boolean containsTag = iTaosTableDomain.getContainsTag();
                    // 默认查询所有字段(包含Tag),当要屏蔽tag时，
                    // 需要设置ITaosTableDomain.containsTag为false, 则会将有@TaosTag注解的字段排除出SelectColumns
                    if (containsTag != null && !containsTag) {
                        if (method.getAnnotation(TaosTag.class) != null) {
                            continue;
                        }
                    }
                    if (method.getAnnotation(Transient.class) != null) {
                        continue;
                    }
                    if (method.getName().equals("getMetadata") || method.getName().equals("getFields") || method.getName().equals("getDynamicTableName")) {
                        continue;
                    }
                    columnsSet.add(getColumnName(method));
                }
            }
        }

        if(domain instanceof IMybatisForceParams){
            IMybatisForceParams iMybatisForceParams =((IMybatisForceParams) domain);
            //设置WhereSuffixSql
            if(StringUtils.isNotBlank(iMybatisForceParams.getWhereSuffixSql())){
                exampleExpand.setWhereSuffixSql(iMybatisForceParams.getWhereSuffixSql());
            }
        }

        //如果不检查，则用反射强制注入
//        if (checkInjection == null || !checkInjection) {
            try {
                Field selectColumnsField = Example.class.getDeclaredField("selectColumns");
                selectColumnsField.setAccessible(true);
                selectColumnsField.set(exampleExpand, columnsSet);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return exampleExpand;
//        } else {//如果要检查字段(防止注入)
//            ExampleExpand exampleExpand1 = ExampleExpand.of(entityClass);
//            //防止SQL注入
//            exampleExpand1.selectProperties(columnsSet.toArray(new String[]{}));
//            if (domain instanceof IMybatisForceParams) {
//                IMybatisForceParams iMybatisForceParams = ((IMybatisForceParams) domain);
//                //设置WhereSuffixSql
//                if (StringUtils.isNotBlank(iMybatisForceParams.getWhereSuffixSql())) {
//                    exampleExpand1.setWhereSuffixSql(iMybatisForceParams.getWhereSuffixSql());
//                }
//            }
//        }
//        return exampleExpand1;
    }

    /**
     * 用于支持like, order by 的查询，支持分页
     * @param domain
     * @return
     */
    public List<T> listByExample(T domain){
        try {
            ExampleExpand exampleExpand = buildExample(domain);
            //设置分页信息
            Integer page = domain.getPage();
            page = (page == null) ? Integer.valueOf(1) : page;
            if(domain.getRows() != null) {
                Integer rows = 0;
                if(domain.getRows() >= 0 ){
                    rows = domain.getRows();
                }
//            String countColumn = ITaosDomain.ID;
                //TAOS数据库使用count(1)不会报错
                String countColumn = "1";
                Boolean isCount = true;
                if(domain instanceof IMybatisForceParams){
                    IMybatisForceParams iMybatisForceParams =((IMybatisForceParams) domain);
                    //设置CountColumn
                    if(StringUtils.isNotBlank(iMybatisForceParams.getCountColumn())){
                        countColumn = iMybatisForceParams.getCountColumn();
                    }
                    //设置是否查总数
                    if(iMybatisForceParams.getIsCount() != null){
                        isCount = iMybatisForceParams.getIsCount();
                    }
                }
                //为了线程安全,请勿改动下面两行代码的顺序
                if(isCount) {
                    PageHelper.startPage(page, rows, isCount).countColumn(countColumn);
                }else{
                    PageHelper.startPage(page, rows, false);
                }
            }
            return getDao().selectByExampleExpand(exampleExpand);
        }catch (MyBatisSystemException e){
            //处理TDEngine在分页查询无数据返回时，会在ExecutorUtil.executeAutoCount返回empty List，导致异常
            //Mysql是正常返回第一个元素为0的List
            if(e.getCause() != null || e.getCause().getCause() instanceof  IndexOutOfBoundsException) {
                return new ArrayList<>();
            }
            throw e;
        }
    }

    /**
     * 用于支持like, order by 的查询，支持分页
     * 支持动态返回类型
     * @param domain
     * @return
     */
    public List listObjectByExample(T domain){
        return (List) listByExample(domain);
    }

    /**
     * 用于支持like, order by 的查询，支持分页
     * @param domain
     * @return
     */
    public T getByExample(T domain){
        return getDao().selectOneByExampleExpand(buildExample(domain));
    }

    /**
     * 用于支持like, order by 的查询，支持分页
     * @param domain
     * @return
     */
    public Object getObjectByExample(T domain){
        return getDao().selectOneByExampleExpand(buildExample(domain));
    }

    /**
     * 用于支持like, order by 的查询，支持分页
     * @param domain
     * @return
     */
    public int countByExample(T domain){
        return getDao().selectCountByExample(buildExample(domain));
    }

    /**
     * 构建example
     * @param domain
     * @return
     */
    public ExampleExpand buildExample(T domain){
        Class tClazz = getSuperClassGenricType(getClass(), 0);
        if(null == domain) {
            domain = getDefaultBean (tClazz);
        }
        ExampleExpand example = createExample(domain, tClazz);
        //接口只取getter方法
        if(tClazz.isInterface()) {
            buildExampleByGetterMethods(domain, example);
        }else {//类取属性
            buildExampleByFields(domain, example);
        }
        //设置动态表名
        if(domain instanceof IDynamicTableName){
            String dynamicTableName = ((IDynamicTableName) domain).getDynamicTableName();
            if(StringUtils.isNotBlank(dynamicTableName)) {
                example.setTableName(dynamicTableName);
            }
        }
        //设置动态返回值类型
        if(domain instanceof IDynamicResultType){
            String resultType = ((IDynamicResultType) domain).getResultType();
            if(StringUtils.isNotBlank(resultType)) {
                example.setResultType(resultType);
            }
        }
        return example;
    }

    
    public List<T> selectByExample(Object example){
        return getDao().selectByExample(example);
    }

    
    public T selectByPrimaryKey(Long key){
        return getDao().selectByPrimaryKey(key);
    }

    
    public boolean existsWithPrimaryKey(Long key){
        return getDao().existsWithPrimaryKey(key);
    }

    
    public int insertExact(T t){
        return getDao().insertExact(t);
    }

    
    public int insertExactSimple(T t){
        try {
            buildExactDomain(t, "insertForceParams");
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
        }
        return getDao().insertExact(t);
    }

    /**
     * 用于支持like, order by 的easyui分页查询
     * @param domain
     * @return
     */
    
    public EasyuiPageOutput listEasyuiPageByExample(T domain) {
        List<T> list = listByExample(domain);
        long total = list instanceof Page ? ( (Page) list).getTotal() : list.size();
        return new EasyuiPageOutput(total, list);
    }

    /**
     * 根据实体查询easyui分页结果
     * @param domain
     * @return
     */
    
    public EasyuiPageOutput listEasyuiPage(T domain, boolean useProvider) {
        if(domain.getRows() != null && domain.getRows() >= 1) {
            //为了线程安全,请勿改动下面两行代码的顺序
            PageHelper.startPage(domain.getPage(), domain.getRows()).setCountColumn(ITaosDomain.ID);
        }
        List<T> list = getDao().select(domain);
        long total = list instanceof Page ? ( (Page) list).getTotal() : list.size();
        return new EasyuiPageOutput(total, list);
    }

    //========================================= 私有方法分界线 =========================================

    /**
     * 设置ITaosDomain中的排序字段
     * @param domain
     * @param example
     */
    private void setOrderBy(T domain, Example example){
        //设置排序信息(domain.getSort()是排序字段名，多个以逗号分隔)
        if(StringUtils.isNotBlank(domain.getSort())) {
            StringBuilder orderByClauseBuilder = new StringBuilder();
            String[] sortFields = domain.getSort().split(",");
            String[] orderByTypes = domain.getOrder().split(",");
            //如果orderByTypes(asc或desc)只定义了一个，则所有都按第一个来处理
            if(sortFields.length > 1 && orderByTypes.length == 1){
                String orderByType = orderByTypes[0];
                orderByTypes = new String[sortFields.length];
                for(int i=0; i<sortFields.length; i++){
                    orderByTypes[i] = orderByType;
                }
            }
            //sortFields和orderTypes的对应顺序一致
            for(int i=0; i < sortFields.length; i++) {
                String sortField = sortFields[i].trim();
                String orderByType = orderByTypes[i].trim();
                orderByType = StringUtils.isBlank(orderByType) ? "asc" : orderByType;
                orderByClauseBuilder.append("," + POJOUtils.humpToLineFast(sortField) + " " + orderByType);
            }
            if(orderByClauseBuilder.length()>1) {
                example.setOrderByClause(orderByClauseBuilder.substring(1));
            }
        }
    }

    /**
     * 根据类的属性构建查询Example
     * @param domain
     */
    protected void buildExampleByFields(T domain, Example example){
        Class tClazz = domain.getClass();
        //不处理接口
        if(tClazz.isInterface()) {
            return;
        }
        Example.Criteria criteria = example.createCriteria();
        //解析空值字段
        Set<String> nullFields = parseNullField(domain, criteria);
        Set<String> notNullFields = parseNotNullField(domain, criteria);
        List<Field> fields = new ArrayList<>();
        getDeclaredField(domain.getClass(), fields);
        //用于在for中判断是否需要添加查询条件
        ITaosTableDomain iTaosTableDomain = ((ITaosTableDomain) domain);
        boolean taosTable = false;
        if (domain instanceof ITaosTableDomain) {
            taosTable = true;
        }
        for(Field field : fields){
            String columnName = getColumnName(field);
            //跳过空值字段
            if(nullFields.contains(columnName) || notNullFields.contains(columnName)){
                continue;
            }
            Transient transient1 = field.getAnnotation(Transient.class);
            if(transient1 != null) {
                continue;
            }
            if(taosTable) {
                Boolean containsTag = iTaosTableDomain.getContainsTag();
                // 默认查询所有字段(包含Tag),当要屏蔽tag时，
                // 需要设置ITaosTableDomain.containsTag为false, 则会将有@TaosTag注解的字段排除出查询条件
                if (containsTag != null && !containsTag) {
                    if(field.getAnnotation(TaosTag.class) != null){
                        continue;
                    }
                }
            }
            Like like = field.getAnnotation(Like.class);
            Operator operator = field.getAnnotation(Operator.class);
            //and/or
            SqlOperator sqlOperator = field.getAnnotation(SqlOperator.class);
            Class<?> fieldType = field.getType();
            Object value = null;
            try {
                field.setAccessible(true);
                value = field.get(domain);
                if(value instanceof Date){
                    value = DateFormatUtils.format((Date)value, "yyyy-MM-dd HH:mm:ss");
                }
            } catch (IllegalAccessException e) {
            }
            //没值就不拼接sql
            if(value == null) {
                continue;
            }
            //防注入
            if(value instanceof String && !checkXss((String)value)){
                throw new ParamErrorException("SQL注入拦截:"+value);
            }

            if(sqlOperator == null || SqlOperator.AND.equals(sqlOperator.value())) {
                if (like != null) {
                    andLike(criteria, columnName, like.value(), value);
                } else if (operator != null) {
                    if (!andOerator(criteria, columnName, fieldType, operator.value(), value)) {
                        continue;
                    }
                } else {
                    andEqual(criteria, columnName, value);
                }
            }else{
                if (like != null) {
                    orLike(criteria, columnName, like.value(), value);
                } else if (operator != null) {
                    if (!orOerator(criteria, columnName, fieldType, operator.value(), value)) {
                        continue;
                    }
                } else {
                    orEqual(criteria, columnName, value);
                }
            }
        }
        //拼接自定义and conditon expr
        if(domain.getMetadata(IDTO.AND_CONDITION_EXPR) != null){
            criteria = criteria.andCondition(domain.getMetadata(IDTO.AND_CONDITION_EXPR).toString());
        }
        //拼接自定义or conditon expr
        if(domain.getMetadata(IDTO.OR_CONDITION_EXPR) != null){
            criteria = criteria.orCondition(domain.getMetadata(IDTO.OR_CONDITION_EXPR).toString());
        }
        //设置@OrderBy注解的排序(会被ITaosDomain中的排序字段覆盖)
        buildFieldsOrderByClause(tClazz.getFields(), example);
        //设置ITaosDomain中的排序字段(会覆盖@OrderBy注解的排序)
        setOrderBy(domain, example);
    }

    private static final String sqlReg = "(?:')|(?:--)|(/\\*(?:.|[\\n\\r])*?\\*/)|"+ "(\\b(select|update|and|or|delete|insert|trancate|char|into|substr|ascii|declare|exec|count|master|into|drop|execute)\\b)";
    private static Pattern sqlPattern = Pattern.compile(sqlReg, Pattern.CASE_INSENSITIVE);
    /**
     * 检测SQL注入
     *
     * @param value
     * @return
     */
    private boolean checkXss(String value) {
        if (value == null || "".equals(value)) {
            return true;
        }
        if (sqlPattern.matcher(value).find()) {
            LOGGER.error("SQL注入拦截:" + value);
            return false;
        }
        return true;
    }

    /**
     * 判断该方法是否要排除，用于buildExampleByGetterMethods
     * 排除非getter，getPage(),getRows(),getMetadata()和getMetadata(String key)等ITaosDomain或BaseDomain上定义的基础方法
     * @param method
     * @return
     */
    private boolean excludeMethod(Method method){
        //只处理getter方法
        if(!POJOUtils.isGetMethod(method)){
            return true;
        }
        if(method.getParameterTypes().length>0){
            return true;
        }
        Class<?> declaringClass = method.getDeclaringClass();
        //排除ITaosDomain或BaseDomain上定义的基础方法
        if (ITaosDomain.class.equals(declaringClass) || BaseDomain.class.equals(declaringClass)){
            return true;
        }
        return false;
    }

    /**
     * 根据类或接口的getter方法构建查询Example
     * @param domain
     */
    protected void buildExampleByGetterMethods(T domain, Example example){
        Class tClazz = DTOUtils.getDTOClass(domain);
        Example.Criteria criteria = example.createCriteria();
        //解析空值字段(where xxx is null)
        Set<String> nullFields = parseNullField(domain, criteria);
        Set<String> notNullFields = parseNotNullField(domain, criteria);
        List<Method> methods = new ArrayList<>();
        //设置子类和所有超类的方法
        getDeclaredMethod(tClazz, methods);
        //用于在for中判断是否需要添加查询条件
        ITaosTableDomain iTaosTableDomain = ((ITaosTableDomain) domain);
        boolean taosTable = false;
        if (domain instanceof ITaosTableDomain) {
            taosTable = true;
        }
        for(Method method : methods){
            if(excludeMethod(method)) {
                continue;
            }
            Column column = method.getAnnotation(Column.class);
            //数据库列名
            String columnName = column == null ? POJOUtils.humpToLineFast(POJOUtils.getBeanField(method)) : column.name();
            //跳过空值字段
            if(nullFields.contains(columnName) || notNullFields.contains(columnName)){
                continue;
            }
            Transient transient1 = method.getAnnotation(Transient.class);
            if(transient1 != null) {
                continue;
            }
            if(taosTable) {
                Boolean containsTag = iTaosTableDomain.getContainsTag();
                // 默认查询所有字段(包含Tag),当要屏蔽tag时，
                // 需要设置ITaosTableDomain.containsTag为false, 则会将有@TaosTag注解的字段排除出查询条件
                if (containsTag != null && !containsTag) {
                    if(method.getAnnotation(TaosTag.class) != null){
                        continue;
                    }
                }
            }
            Like like = method.getAnnotation(Like.class);
            Operator operator = method.getAnnotation(Operator.class);
            //and/or
            SqlOperator sqlOperator = method.getAnnotation(SqlOperator.class);
            Class<?> fieldType = method.getReturnType();
            Object value = getGetterValue(domain, method);
            //没值就不拼接sql
            if(value == null || "".equals(value)) {
                continue;
            }
            //防注入
            if(value instanceof String && !checkXss((String)value)){
                throw new ParamErrorException("SQL注入拦截:"+value);
            }
            if(sqlOperator == null || SqlOperator.AND.equals(sqlOperator.value())) {
                if (like != null) {
                    andLike(criteria, columnName, like.value(), value);
                } else if (operator != null) {
                    if (!andOerator(criteria, columnName, fieldType, operator.value(), value)) {
                        continue;
                    }
                } else {
                    andEqual(criteria, columnName, value);
                }
            }else{
                if (like != null) {
                    orLike(criteria, columnName, like.value(), value);
                } else if (operator != null) {
                    if (!orOerator(criteria, columnName, fieldType, operator.value(), value)) {
                        continue;
                    }
                }else {
                    orEqual(criteria, columnName, value);
                }
            }
        }
        //拼接自定义and conditon expr
        if(domain.mget(IDTO.AND_CONDITION_EXPR) != null){
            criteria = criteria.andCondition(domain.mget(IDTO.AND_CONDITION_EXPR).toString());
        }
        //拼接自定义or conditon expr
        if(domain.mget(IDTO.OR_CONDITION_EXPR) != null){
            criteria = criteria.orCondition(domain.mget(IDTO.OR_CONDITION_EXPR).toString());
        }
        //设置@OrderBy注解的排序(会被ITaosDomain中的排序字段覆盖)
        buildMethodsOrderByClause(methods, example);
        //设置ITaosDomain中的排序字段(会覆盖@OrderBy注解的排序)
        setOrderBy(domain, example);
    }

    /**
     * 拼接or like
     * @param criteria
     * @param columnName
     * @param likeValue
     * @param value
     */
    private void orLike(Example.Criteria criteria, String columnName, String likeValue, Object value){
        switch(likeValue){
            case Like.LEFT:
                criteria = criteria.orCondition(columnName + " like '%" + value + "' ");
                break;
            case Like.RIGHT:
                criteria = criteria.orCondition(columnName + " like '" + value + "%' ");
                break;
            case Like.BOTH:
                criteria = criteria.orCondition(columnName + " like '%" + value + "%' ");
                break;
            default : {
                if(value instanceof Boolean || Number.class.isAssignableFrom(value.getClass())){
                    criteria = criteria.orCondition(columnName + " = " + value + " ");
                }else{
                    criteria = criteria.orCondition(columnName + " = '" + value + "' ");
                }
            }
        }
    }

    /**
     * or 操作符
     * @param criteria
     * @param columnName
     * @param operatorValue
     * @param value
     * @return 当操作符为IN并且为空集合，返回false，需要跳过
     */
    private boolean orOerator(Example.Criteria criteria, String columnName, Class<?> fieldType, String operatorValue, Object value){
        if(operatorValue.equals(Operator.IN) || operatorValue.equals(Operator.NOT_IN)){
            if(value instanceof Collection && CollectionUtils.isEmpty((Collection)value)){
                return false;
            }
            StringBuilder sb = new StringBuilder();
            if(Collection.class.isAssignableFrom(fieldType)){
                for(Object o : (Collection)value){
                    if(o instanceof String){
                        sb.append(", '").append(o).append("'");
                    }else {
                        sb.append(", ").append(o);
                    }
                }
            }else if(fieldType.isArray()){
                for(Object o : ( (Object[])value)){
                    if(o instanceof String){
                        sb.append(", '").append(o).append("'");
                    }else {
                        sb.append(", ").append(o);
                    }
                }
            }else{
                sb.append(", '").append(value).append("'");
            }
            criteria = criteria.orCondition(columnName + " " + operatorValue + "(" + sb.substring(1) + ")");
        }else {
            criteria = criteria.orCondition(columnName + " " + operatorValue + " '" + value + "' ");
        }
        return true;
    }

    /**
     * or equal
     * @param criteria
     * @param columnName
     * @param value
     */
    private void orEqual(Example.Criteria criteria, String columnName, Object value){
        if(value instanceof Boolean || Number.class.isAssignableFrom(value.getClass())){
            criteria = criteria.orCondition(columnName + " = "+ value+" ");
        }else{
            criteria = criteria.orCondition(columnName + " = '"+ value+"' ");
        }
    }

    /**
     * and equal
     * @param criteria
     * @param columnName
     * @param value
     */
    private void andEqual(Example.Criteria criteria, String columnName, Object value){
        if(value instanceof Boolean || Number.class.isAssignableFrom(value.getClass())){
            criteria = criteria.andCondition(columnName + " = "+ value+" ");
        }else{
            criteria = criteria.andCondition(columnName + " = '"+ value+"' ");
        }
    }

    /**
     * 拼接and like
     * @param criteria
     * @param columnName
     * @param likeValue
     * @param value
     */
    private void andLike(Example.Criteria criteria, String columnName, String likeValue, Object value){
        switch(likeValue){
            case Like.LEFT:
                criteria = criteria.andCondition(columnName + " like '%" + value + "' ");
                break;
            case Like.RIGHT:
                criteria = criteria.andCondition(columnName + " like '" + value + "%' ");
                break;
            case Like.BOTH:
                criteria = criteria.andCondition(columnName + " like '%" + value + "%' ");
                break;
            default : {
                if(value instanceof Boolean || Number.class.isAssignableFrom(value.getClass())){
                    criteria = criteria.andCondition(columnName + " = " + value + " ");
                }else{
                    criteria = criteria.andCondition(columnName + " = '" + value + "' ");
                }
            }
        }
    }

    /**
     * and 操作符
     * @param criteria
     * @param columnName
     * @param operatorValue
     * @param value
     * @return 当操作符和类型不匹配(如当操作符为IN并且为空集合)，返回false，需要跳过
     */
    private boolean andOerator(Example.Criteria criteria, String columnName, Class<?> fieldType, String operatorValue, Object value){
        if(operatorValue.equals(Operator.IN) || operatorValue.equals(Operator.NOT_IN)){
            if(value instanceof Collection && CollectionUtils.isEmpty((Collection)value)){
                return false;
            }
            StringBuilder sb = new StringBuilder();
            if(Collection.class.isAssignableFrom(fieldType)){
                for(Object o : (Collection)value){
                    if(o instanceof String){
                        sb.append(", '").append(o).append("'");
                    }else {
                        sb.append(", ").append(o);
                    }
                }
            }else if(fieldType.isArray()){
                for(Object o : ( (Object[])value)){
                    if(o instanceof String){
                        sb.append(", '").append(o).append("'");
                    }else {
                        sb.append(", ").append(o);
                    }
                }
            }else{
                sb.append(", '").append(value).append("'");
            }
            criteria = criteria.andCondition(columnName + " " + operatorValue + "(" + sb.substring(1) + ")");
        }else if(operatorValue.equals(Operator.BETWEEN) || operatorValue.equals(Operator.NOT_BETWEEN)){
            StringBuilder sb = new StringBuilder();
            if(List.class.isAssignableFrom(fieldType)){
                List list = (List)value;
                //只支持长度为2的List
                if((CollectionUtils.isEmpty(list) || list.size() != 2)){
                    return false;
                }
                //将日期类型转变为字符串处理
                convertDatetimeList(list);
                if(list.get(0) instanceof String){
                    sb.append("'").append(list.get(0)).append("' and '").append(list.get(1)).append("'");
                }else {
                    sb.append(list.get(0)).append(" and ").append(list.get(1));
                }
            }else if(fieldType.isArray()){
                Object[] arrays = (Object[])value;
                //只支持长度为2的数组
                if((arrays == null || arrays.length != 2)){
                    return false;
                }
                //将日期类型转变为字符串处理
                arrays = convertDatetimeArray(arrays);
                sb = buildBetweenStringBuilderByArray(arrays);
            }else if(String.class.isAssignableFrom(fieldType)){
                String[] arrays = value.toString().split(",");
                //只支持长度为2的数组
                if((arrays == null || arrays.length != 2)){
                    return false;
                }
                sb = buildBetweenStringBuilderByArray(arrays);
            }else{//不支持其它类型
                return false;
            }
            sb.append(columnName).append(" ").append(operatorValue).append(sb);
            criteria = criteria.andCondition(sb.toString());
        }else {
            criteria = criteria.andCondition(columnName + " " + operatorValue + " '" + value + "' ");
        }
        return true;
    }

    /**
     * 将日期类型的List转成字符串类型，用于between
     * @param list
     * @return
     */
    private void convertDatetimeList(List list){
        String DATE_TIME = "yyyy-MM-dd HH:mm:ss";
        if(list.get(0) instanceof Date){
            list.set(0, DateUtils.format((Date) list.get(0)));
            list.set(1, DateUtils.format((Date) list.get(1)));
        }else if(list.get(0) instanceof LocalDateTime){
            list.set(0, DateUtils.format((LocalDateTime)list.get(0), DATE_TIME));
            list.set(1, DateUtils.format((LocalDateTime)list.get(1), DATE_TIME));
        }else if(list.get(0) instanceof LocalDate){
            list.set(0, DateUtils.format(LocalDateTime.of((LocalDate) list.get(0), LocalTime.ofSecondOfDay(0)), DATE_TIME));
            list.set(1, DateUtils.format(LocalDateTime.of((LocalDate) list.get(1), LocalTime.ofSecondOfDay(0)), DATE_TIME));
        }
    }

    /**
     * 将日期类型的List转成字符串类型，用于between
     * @param objs
     * @return
     */
    private Object[] convertDatetimeArray(Object[] objs){
        String DATE_TIME = "yyyy-MM-dd HH:mm:ss";
        if(objs[0] instanceof Date){
            objs[0] = DateUtils.format((Date) objs[0]);
            objs[1] = DateUtils.format((Date) objs[1]);
        }else if(objs[0] instanceof LocalDateTime){
            objs[0] =  DateUtils.format((LocalDateTime)objs[0], DATE_TIME);
            objs[1] =  DateUtils.format((LocalDateTime)objs[1], DATE_TIME);
        }else if(objs[0] instanceof LocalDate){
            objs[0] = DateUtils.format(LocalDateTime.of((LocalDate) objs[0], LocalTime.ofSecondOfDay(0)), DATE_TIME);
            objs[1] = DateUtils.format(LocalDateTime.of((LocalDate) objs[1], LocalTime.ofSecondOfDay(0)), DATE_TIME);
        }
        return objs;
    }

    /**
     * 根据数组构建between and
     * @param arrays
     * @return
     */
    private StringBuilder buildBetweenStringBuilderByArray(Object[] arrays){
        StringBuilder sb = new StringBuilder(" ");
        if(arrays[0] instanceof String){
            sb.append("'").append(arrays[0]).append("' and '").append(arrays[1]).append("'");
        }else {
            sb.append(arrays[0]).append(" and ").append(arrays[1]);
        }
        return sb;
    }

    /**
     * 设置@OrderBy注解的排序
     */
    private void buildFieldsOrderByClause(Field[] fields, Example example){
        StringBuilder orderByClauseBuilder = new StringBuilder();
        for(Field field : fields) {
            Transient transient1 = field.getAnnotation(Transient.class);
            if(transient1 != null) {
                continue;
            }
            OrderBy orderBy = field.getAnnotation(OrderBy.class);
            if(orderBy == null) {
                continue;
            }
            Column column = field.getAnnotation(Column.class);
            String columnName = column == null ? field.getName() : column.name();
            orderByClauseBuilder.append(","+columnName+" "+orderBy.value());
        }
        if(orderByClauseBuilder.length()>1) {
            example.setOrderByClause(orderByClauseBuilder.substring(1));
        }
    }

    /**
     * 设置@OrderBy注解的排序
     */
    private void buildMethodsOrderByClause(List<Method> methods, Example example){
        StringBuilder orderByClauseBuilder = new StringBuilder();
        for(Method method : methods){
            Transient transient1 = method.getAnnotation(Transient.class);
            if(transient1 != null) {
                continue;
            }
            OrderBy orderBy = method.getAnnotation(OrderBy.class);
            if(orderBy == null) {
                continue;
            }
            Column column = method.getAnnotation(Column.class);
            String columnName = column == null ? POJOUtils.getBeanField(method) : column.name();
            orderByClauseBuilder.append(","+columnName+" "+orderBy.value());
        }
        if(orderByClauseBuilder.length()>1) {
            example.setOrderByClause(orderByClauseBuilder.substring(1));
        }
    }

    /**
     * 获取子类和所有超类的属性<br/>
     * 过滤掉子类和父类同名的属性，以子类为主
     * @param clazz
     * @param fields
     * @return
     */
    protected List<Field> getDeclaredField(Class clazz, List<Field> fields){
        List<Field> clazzFields = Lists.newArrayList(Arrays.copyOf(clazz.getDeclaredFields(), clazz.getDeclaredFields().length));
        //过滤掉子类和父类同名的属性，以子类为主
        for (Iterator<Field> it = clazzFields.iterator(); it.hasNext();) {
            Field clazzField = it.next();
            for(int i=0; i<fields.size(); i++) {
                if (fields.get(i).getName().equals(clazzField.getName())){
                    it.remove();
                }
            }
        }
        fields.addAll(clazzFields);
        if(clazz.getSuperclass() != null){
            getDeclaredField(clazz.getSuperclass(), fields);
        }
        return fields;
    }

    /**
     * 获取子类和所有超类的方法<br/>
     * 过滤掉子类和父类同名的方法，以子类为主
     * @param clazz
     * @param methods
     * @return
     */
    protected List<Method> getDeclaredMethod(Class clazz, List<Method> methods){
        List<Method> clazzMethods = Lists.newArrayList(Arrays.copyOf(clazz.getDeclaredMethods(), clazz.getDeclaredMethods().length));
        //过滤掉子类和父类同名的方法，以子类为主(注意该处只判断了方法名相同，并没判断参数，毕竟javaBean的方法参数都一致)
        for (Iterator<Method> it = clazzMethods.iterator(); it.hasNext();) {
            Method clazzMethod = it.next();
            for(int i=0; i<methods.size(); i++) {
                if (methods.get(i).getName().equals(clazzMethod.getName())){
                    it.remove();
                    break;
                }
            }
        }
        methods.addAll(clazzMethods);
        //clazz是接口，则找所有父接口上的方法
        if(clazz.isInterface()) {
            Class<?>[] interfaces = clazz.getInterfaces();
            if (interfaces != null) {
                for(Class<?> intf : interfaces) {
                    getDeclaredMethod(intf, methods);
                }
            }
        }else {//clazz是类，则找所有父类上的方法，但不找父接口，毕竟类会有接口方法的实现
            if (clazz.getSuperclass() != null) {
                getDeclaredMethod(clazz.getSuperclass(), methods);
            }
        }
        return methods;
    }

    /**
     * 通过反射, 获得定义Class时声明的父类的泛型参数的类型. 如无法找到, 返回Object.class.
     *
     *@param clazz
     *            clazz The class to introspect
     * @param index
     *            the Index of the generic ddeclaration,start from 0.
     * @return the index generic declaration, or Object.class if cannot be
     *         determined
     */
    private Class<Object> getSuperClassGenricType(final Class clazz, final int index) {
        //返回表示此 Class 所表示的实体（类、接口、基本类型或 void）的直接超类的 Type。
        Type genType = clazz.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        }
        //返回表示此类型实际类型参数的 Type 对象的数组。
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        if (index >= params.length || index < 0) {
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            return Object.class;
        }
        return (Class) params[index];
    }

    /**
     * 构建精确更新实体,仅支持DTO
     * @param domain DTO接口
     * @param fieldName setForceParams或insertForceParams
     */
    private void buildExactDomain(T domain, String fieldName) throws Exception {
        //如果不是DTO接口，不构建
        if(!DTOUtils.isProxy(domain) && !DTOUtils.isInstance(domain)){
            return;
        }
        //如果未实现IMybatisForceParams接口不构建
        if(!IMybatisForceParams.class.isAssignableFrom(DTOUtils.getDTOClass(domain))){
            return;
        }
        Map params = new HashMap();
        //构建dto
        Method[] dtoMethods = DTOUtils.getDTOClass(domain).getMethods();
        Map dtoMap = DTOUtils.go(domain);
        for(Method dtoMethod : dtoMethods){
            if(dtoMethod.getName().equals("getMetadata")){
                continue;
            }
            //只判断getter方法
            if(POJOUtils.isGetMethod(dtoMethod)){
                //如果dtoMap中有该字段，并且值为null
                if(dtoMap.containsKey(POJOUtils.getBeanField(dtoMethod)) && dtoMethod.invoke(domain) == null){
                    Id id = dtoMethod.getAnnotation(Id.class);
                    //不允许将主键改为null
                    if(id != null){
                        continue;
                    }
                    Column column = dtoMethod.getAnnotation(Column.class);
                    String columnName = column == null ? POJOUtils.humpToLine(POJOUtils.getBeanField(dtoMethod)) : column.name();
                    params.put(columnName, null);
                }
            }
        }
        domain.aset(fieldName, params);
    }


    /**
     * 反射获取getter方法中的值
     * @param method
     * @param domain
     * @return
     */
    private Object getGetterValue(T domain, Method method){
        Object value = null;
        try {
            method.setAccessible(true);
            value = method.invoke(domain);
            if(value instanceof Date){
                value = DateFormatUtils.format((Date)value, "yyyy-MM-dd HH:mm:ss");
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * 如果metadata中有空值字段名，则解析为field is null
     */
    private Set<String> parseNullField(T domain, Example.Criteria criteria){
        Set<String> nullFileds = new HashSet<>();
        //如果metadata中有空值字段名，则解析为field is null
        Object nullValueField = DTOUtils.getDTOClass(domain).isInterface() ? domain.mget(IDTO.NULL_VALUE_FIELD) : domain.getMetadata(IDTO.NULL_VALUE_FIELD);
        if(nullValueField != null){
            if(nullValueField instanceof String){
                nullFileds.add((String) nullValueField);
                criteria = criteria.andCondition(nullValueField + " is null ");
            }else if(nullValueField instanceof List){
                List<String> nullValueFields = (List)nullValueField;
                nullFileds.addAll(nullValueFields);
                for(String field : nullValueFields){
                    criteria = criteria.andCondition(field + " is null ");
                }
            }else if(nullValueField instanceof Set){
                Set<String> nullValueFields = (Set)nullValueField;
                nullFileds = nullValueFields;
                for(String field : nullValueFields){
                    criteria = criteria.andCondition(field + " is null ");
                }
            }
        }
        if(domain instanceof IMybatisForceParams) {
            IMybatisForceParams iMybatisForceParams = ((IMybatisForceParams) domain);
            if(CollectionUtils.isNotEmpty(iMybatisForceParams.getNullValueFields())){
                Set<String> nullValueFields = iMybatisForceParams.getNullValueFields();
                if(CollectionUtils.isNotEmpty(nullValueFields)) {
                    nullFileds = nullValueFields;
                    for(String field : nullValueFields){
                        criteria = criteria.andCondition(field + " is null ");
                    }
                }
            }
        }
        return nullFileds;
    }

    /**
     * 如果metadata中有非空值字段名，则解析为field is not null
     */
    private Set<String> parseNotNullField(T domain, Example.Criteria criteria){
        Set<String> notNullFileds = new HashSet<>();
        //如果metadata中有空值字段名，则解析为field is null
        Object notNullValueField = DTOUtils.getDTOClass(domain).isInterface() ? domain.mget(IDTO.NOT_NULL_VALUE_FIELD) : domain.getMetadata(IDTO.NOT_NULL_VALUE_FIELD);
        if(notNullValueField != null){
            if(notNullValueField instanceof String){
                notNullFileds.add((String)notNullValueField);
                criteria = criteria.andCondition(notNullValueField + " is not null ");
            }else if(notNullValueField instanceof List){
                List<String> notNullValueFields = (List)notNullValueField;
                notNullFileds.addAll(notNullFileds);
                for(String field : notNullValueFields){
                    criteria = criteria.andCondition(field + " is not null ");
                }
            }else if(notNullValueField instanceof Set){
                Set<String> notNullValueFields = (Set)notNullValueField;
                notNullFileds = notNullValueFields;
                for(String field : notNullValueFields){
                    criteria = criteria.andCondition(field + " is not null ");
                }
            }
        }
        if(domain instanceof IMybatisForceParams) {
            IMybatisForceParams iMybatisForceParams = ((IMybatisForceParams) domain);
            if(CollectionUtils.isNotEmpty(iMybatisForceParams.getNotNullValueFields())){
                Set<String> nullValueFields = iMybatisForceParams.getNotNullValueFields();
                if(CollectionUtils.isNotEmpty(nullValueFields)) {
                    notNullFileds = nullValueFields;
                    for (String field : nullValueFields) {
                        criteria = criteria.andCondition(field + " is not null ");
                    }
                }
            }
        }
        return notNullFileds;
    }

    /**
     * 构建批量插入sql
     * @param datas
     * @return
     */
    private String buildInsertSql(List<T> datas) {
        if(CollectionUtils.isEmpty(datas)){
            return null;
        }
        if(!(datas.get(0) instanceof IDynamicTableName)){
            throw new DataErrorException("未实现IDynamicTableName接口");
        }
        Class<T> dtoClass = (Class)DTOUtils.getDTOClass(datas.get(0));
        List<ITaosTableDomain> tableDomains = (List)datas;
        //按动态表名排序
        Collections.sort(tableDomains, (a, b) -> {
            return a.getDynamicTableName().compareTo(b.getDynamicTableName());
        });
        Table table = dtoClass.getAnnotation(Table.class);
        String tableName = table == null ? CamelTool.camelToUnderline(dtoClass.getSimpleName(), false) : table.name();

        final List<Map<String, Object>> mappings;
        try {
            mappings = beanTableMapping(datas, dtoClass);
        } catch (Exception e) {
            throw new DataErrorException(e);
        }
        //第0条数据映射，用于构建sql的columns部分
        Map<String, Object> mappings0 = mappings.get(0);
        int mappingSize = mappings0.size();
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO ");
        //记录当前表名，因为排了序，有不同的表名后，需要重新计算SQL
        String lastTableName = "";
        int size = tableDomains.size();
        for (int index = 0; index < size; index++) {
            ITaosTableDomain tableDomain = tableDomains.get(index);
            //重复的表名只需要拼接values值部分
            if(lastTableName.equals(tableDomain.getDynamicTableName())){
                appendValues(sqlBuilder, mappings.get(index), mappingSize);
                continue;
            }
            sqlBuilder.append(tableDomain.getDynamicTableName()).append(" USING ").append(tableName);
            //获取TaosTag
            appendTags(sqlBuilder, dtoClass, tableDomain);
            //拼接子表字段
            appendColumns(sqlBuilder, mappings0, mappingSize);
            sqlBuilder.append("VALUES");
            //拼接字段值
            appendValues(sqlBuilder, mappings.get(index), mappingSize);
            lastTableName = tableDomain.getDynamicTableName();
        }
        return sqlBuilder.toString();
    }

    /**
     * 添加insert values部分
     * @param sqlBuilder
     * @param item
     * @param mappingSize
     */
    private void appendValues(StringBuilder sqlBuilder, Map<String, Object> item, int mappingSize){
        int k=0;
        sqlBuilder.append(" (");
        for (Map.Entry<String, Object> entry : item.entrySet()) {
            Object value = entry.getValue();
            if(value == null){
                sqlBuilder.append("null");
            }else if(value instanceof Number) {
                sqlBuilder.append(value);
            }else{
                sqlBuilder.append("'").append(value).append("'");
            }
            if(k < mappingSize-1) {
                sqlBuilder.append(", ");
            }
            k++;
        }
        sqlBuilder.append(") ");
    }

    /**
     * 添加insert子表列
     * @param sqlBuilder
     * @param item
     * @param mappingSize
     */
    private void appendColumns(StringBuilder sqlBuilder, Map<String, Object> item, int mappingSize){
        sqlBuilder.append("(");
        int i=0;
        for (Map.Entry<String, Object> entry : item.entrySet()) {
            sqlBuilder.append(entry.getKey());
            if(i < (mappingSize-1)) {
                sqlBuilder.append(", ");
            }
            i++;
        }
        sqlBuilder.append(") ");
    }

    /**
     * 添加insert tags，包含columns和values
     * @param sqlBuilder
     * @param dtoClass
     * @param tableDomain
     */
    private void appendTags(StringBuilder sqlBuilder, Class<T> dtoClass, ITaosTableDomain tableDomain){
        //获取TaosTag
        List<String> tags = getTags(dtoClass);
        if(!tags.isEmpty()){
            sqlBuilder.append(" (");
            int tagSize = tags.size();
            tags.forEach(LambadaTools.forEachWithIndex((item, idx) -> {
                sqlBuilder.append(item);
                if(idx < (tagSize-1)) {
                    sqlBuilder.append(", ");
                }
            }));
            sqlBuilder.append(") TAGS (");
            tags.forEach(LambadaTools.forEachWithIndex((item, i) -> {
                Object tagValue = POJOUtils.getProperty(tableDomain, item);
                if(tagValue instanceof Number){
                    sqlBuilder.append(tagValue);
                }else {
                    sqlBuilder.append("'").append(tagValue).append("'");
                }
                if(i < (tagSize-1)) {
                    sqlBuilder.append(", ");
                }
            }));
            sqlBuilder.append(") ");
        }
    }

    /**
     * 构建Bean映射, 去掉tags中的字段
     * key为表名，value为字段值
     * 该方法不再支持动态代理DTO
     * @param datas
     * @return
     */
    private List<Map<String, Object>> beanTableMapping(List<T> datas, Class<T> tClass) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>(datas.size());
        if(tClass.isInterface()) {
            Method[] methods = tClass.getMethods();
            //先计算出需要get的方法
            List<Method> getters = new ArrayList<>();
            for (Method method : methods) {
                if (!POJOUtils.isGetMethod(method)) {
                    continue;
                }
                if (method.getAnnotation(TaosTag.class) != null) {
                    continue;
                }
                if (method.getAnnotation(Transient.class) != null) {
                    continue;
                }
                if ("getMetadata".equals(method.getName()) || "getFields".equals(method.getName()) || "getDynamicTableName".equals(method.getName()) || "getResultType".equals(method.getName())) {
                    continue;
                }
                getters.add(method);
            }
            for (T data : datas) {
                Map<String, Object> beanMap = new HashMap<>();
                for (Method getter : getters) {
                    beanMap.put(getColumnName(getter), getter.invoke(data));
                }
                list.add(beanMap);
            }
        }else{
            Field[] fields = tClass.getFields();
            //先计算出需要的属性
            List<Field> fieldList = new ArrayList<>();
            for (Field field : fields) {
                if (field.getAnnotation(TaosTag.class) != null) {
                    continue;
                }
                if (field.getAnnotation(Transient.class) != null) {
                    continue;
                }
                if(field.getDeclaringClass().isInterface()){
                    continue;
                }
                if ("metadata".equals(field.getName()) || "fields".equals(field.getName()) || "dynamicTableName".equals(field.getName()) || "resultType".equals(field.getName())) {
                    continue;
                }
                fieldList.add(field);
            }
            for (T data : datas) {
                Map<String, Object> beanMap = new HashMap<>();
                for (Field field : fieldList) {
                    beanMap.put(getColumnName(field), field.get(data));
                }
                list.add(beanMap);
            }
        }
        return list;
    }

    /**
     * 获取get方法上的Column列名，没有Column注解则按驼峰命名
     * @param method
     * @return
     */
    private String getColumnName(Method method) {
        Column column = method.getAnnotation(Column.class);
        return column == null ? CamelTool.camelToUnderline(POJOUtils.getBeanField(method.getName()), false) : column.name();
    }

    /**
     * 获取属性上的Column列名，没有Column注解则按驼峰命名
     * @param field
     * @return
     */
    private String getColumnName(Field field) {
        Column column = field.getAnnotation(Column.class);
        return column == null ? CamelTool.camelToUnderline(field.getName(), false) : column.name();
    }

    /**
     * 获取DTO上的Transient字段名
     * @param dtoClass
     * @return
     */
    private List<String> getTransients(Class<?> dtoClass){
        List<String> transients = new ArrayList<>();
        for (Method method : dtoClass.getMethods()) {
            //只处理getter方法
            if(!POJOUtils.isGetMethod(method)){
                continue;
            }
            Transient aTransient = method.getAnnotation(Transient.class);
            if(aTransient != null){
                Column column = method.getAnnotation(Column.class);
                String columnName = column == null ? CamelTool.camelToUnderline(POJOUtils.getBeanField(method.getName()), false) : column.name();
                transients.add(columnName);
            }
        }
        return transients;
    }

    /**
     * 获取DTO上的Tag字段名
     * @param dtoClass
     * @return
     */
    private List<String> getTags(Class<?> dtoClass){
        List<String> tags = new ArrayList<>();
        for (Method method : dtoClass.getMethods()) {
            //只处理getter方法
            if(!POJOUtils.isGetMethod(method)){
                continue;
            }
            if(method.getAnnotation(TaosTag.class) != null){
                Column column = method.getAnnotation(Column.class);
                String tag = column == null ? CamelTool.camelToUnderline(POJOUtils.getBeanField(method.getName()), false) : column.name();
                tags.add(tag);
            }
        }
        return tags;
    }
}
