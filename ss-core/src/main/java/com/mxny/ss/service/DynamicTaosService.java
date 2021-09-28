package com.mxny.ss.service;

import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.mxny.ss.ClassCache;
import com.mxny.ss.base.BaseTaosService;
import com.mxny.ss.dao.ExampleExpand;
import com.mxny.ss.domain.DynamicCondition;
import com.mxny.ss.domain.DynamicDomain;
import com.mxny.ss.domain.DynamicField;
import com.mxny.ss.dto.IDTO;
import com.mxny.ss.exception.ParamErrorException;
import com.mxny.ss.glossary.DynamicConditionType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 动态实体服务
 * @author: WM
 * @time: 2021/9/27 11:40
 */
@Service
public class DynamicTaosService extends BaseTaosService<DynamicDomain> {

    /**
     * 查询动态实体
     * @param domain
     * @return
     */
    public List<Map> listDynamicDomain(DynamicDomain domain) {
        try {
            ExampleExpand exampleExpand = buildDynamicExample(domain);
            //设置分页信息
            Integer page = domain.getPage();
            page = (page == null) ? Integer.valueOf(1) : page;
            if (domain.getRows() != null) {
                Integer rows = 0;
                if (domain.getRows() >= 0) {
                    rows = domain.getRows();
                }
                //TAOS数据库使用count(1)不会报错
                String countColumn = "1";
                Boolean isCount = true;
                //设置CountColumn
                if (StringUtils.isNotBlank(domain.getCountColumn())) {
                    countColumn = domain.getCountColumn();
                }
                //设置是否查总数
                if (domain.getIsCount() != null) {
                    isCount = domain.getIsCount();
                }
                //为了线程安全,请勿改动下面两行代码的顺序
                if (isCount) {
                    PageHelper.startPage(page, rows, isCount).countColumn(countColumn);
                } else {
                    PageHelper.startPage(page, rows, false);
                }
            }
            return (List)getDao().selectByExampleExpand(exampleExpand);
        } catch (MyBatisSystemException e) {
            //处理TDEngine在分页查询无数据返回时，会在ExecutorUtil.executeAutoCount返回empty List，导致异常
            //Mysql是正常返回第一个元素为0的List
            if (e.getCause() != null && e.getCause().getCause() instanceof IndexOutOfBoundsException) {
                return new ArrayList<>();
            }
            throw e;
        }
    }

    /**
     * 构建动态Example
     * @param domain
     * @return
     */
    public ExampleExpand buildDynamicExample(DynamicDomain domain){
        Class tClazz = getSuperClassGenricType(getClass(), 0);
        if(null == domain) {
            domain = newInstance(tClazz);
        }
        ExampleExpand example = createDynamicExample(domain, tClazz);
        //接口和类的处理方式一样
        buildDynamicDomainExample(domain, example);
        //设置动态表名
        String dynamicTableName = domain.getDynamicTableName();
        if(StringUtils.isNotBlank(dynamicTableName)) {
            example.setTableName(dynamicTableName);
        }
        //设置动态返回值类型
        String resultType = domain.getResultType();
        if(StringUtils.isNotBlank(resultType)) {
            example.setResultType(resultType);
        }else{
            example.setResultType(HashMap.class.getName());
        }
        return example;
    }

    /**
     * 单行插入动态实体
     * @param dynamicDomain
     * @param data
     */
    @Transactional(rollbackFor = Exception.class)
    public void insertSelectiveByTags(DynamicDomain dynamicDomain, Map data) {
        batchInsert(dynamicDomain, Lists.newArrayList(data));
    }


    /**
     * 批量插入动态实体
     * @param dynamicDomain
     * @param datas
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchInsert(DynamicDomain dynamicDomain, List<Map> datas) {
        if(CollectionUtils.isEmpty(datas) || dynamicDomain == null){
            return;
        }
        jdbcTemplate.execute(buildDynamicInsertSql(dynamicDomain, datas));
    }

    /**
     * 构建动态批量插入sql
     * 由于DynamicDomain仅为单子表的语义模型，所有该方法只支持单子表的批量插入
     * @param dynamicDomain
     * @return
     */
    public <T extends DynamicDomain> String buildDynamicInsertSql(T dynamicDomain, List<Map> datas) {
        Map<String, DynamicField> dynamicFields = dynamicDomain.getDynamicFields();
        if(dynamicFields == null || dynamicFields.isEmpty() || CollectionUtils.isEmpty(datas)){
            return null;
        }
        //超级表名
        String tableName = dynamicDomain.getTableName();
        // 构建Bean映射, 去掉tags中的字段
        // key为列名，value为字段值
        // 左边为tag， 右边为其它字段
        MutablePair<List<Map<String, Object>>, List<Map<String, Object>>> pair = dynamicDomainTableMapping(dynamicDomain, datas);
        List<Map<String, Object>> fieldList = pair.getRight();
        List<Map<String, Object>> tagList = pair.getLeft();
        //第0条数据映射，用于构建sql的columns部分
        Map<String, Object> field0 = fieldList.get(0);
        int fieldSize = field0.size();
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO ");
        //记录当前表名，因为排了序，有不同的表名后，需要重新计算SQL
        String lastTableName = "";
        //插入的数据行数
        int size = datas.size();
        for (int index = 0; index < size; index++) {
            //重复的表名只需要拼接values值部分
            if(lastTableName.equals(dynamicDomain.getDynamicTableName())){
                appendValues(sqlBuilder, fieldList.get(index), fieldSize);
                continue;
            }
            sqlBuilder.append(dynamicDomain.getDynamicTableName()).append(" USING ").append(tableName);
            //获取TaosTag
            appendDynamicDomainTags(sqlBuilder, tagList.get(index));
            //拼接子表字段
            appendColumns(sqlBuilder, field0, fieldSize);
            sqlBuilder.append("VALUES");
            //拼接字段值
            appendValues(sqlBuilder, fieldList.get(index), fieldSize);
            lastTableName = dynamicDomain.getDynamicTableName();
        }
        return sqlBuilder.toString();
    }


    // ==============================================================================================

    /**
     * 添加insert tags，包含columns和values
     * @param sqlBuilder
     * @param tag
     */
    protected <T extends DynamicDomain>  void appendDynamicDomainTags(StringBuilder sqlBuilder, Map<String, Object> tag){
        if (tag == null || tag.isEmpty()) {
            return;
        }
        sqlBuilder.append(" (");
        int tagSize = tag.size();
        int idx = 0;
        for (Map.Entry<String, Object> entry : tag.entrySet()) {
            sqlBuilder.append(entry.getKey());
            if(idx < (tagSize - 1)) {
                sqlBuilder.append(", ");
            }
            idx++;
        }
        sqlBuilder.append(") TAGS (");
        idx = 0;
        for (Map.Entry<String, Object> entry : tag.entrySet()) {
            Object tagValue = entry.getValue();
            if(tagValue instanceof Number){
                sqlBuilder.append(tagValue);
            }else {
                sqlBuilder.append("'").append(tagValue).append("'");
            }
            if(idx < (tagSize - 1)) {
                sqlBuilder.append(", ");
            }
            idx++;
        }
        sqlBuilder.append(") ");
    }

    /**
     * 构建Bean映射， 拆分为tag和field
     * key为列名，value为字段值
     * 该方法不再支持动态代理DTO
     * @param dynamicDomain
     * @param datas
     * @return 左边为tag， 右边为其它字段
     */
    protected <T extends DynamicDomain> MutablePair<List<Map<String, Object>>,  List<Map<String, Object>>> dynamicDomainTableMapping(T dynamicDomain, List<Map> datas) {
        List<Map<String, Object>> fieldList = new ArrayList<>(datas.size());
        List<Map<String, Object>> tagList = new ArrayList<>(datas.size());
        for (Map data : datas) {
            Map<String, DynamicField> dynamicFields = dynamicDomain.getDynamicFields();
            Map<String, Object> fieldMap = new HashMap<>(dynamicFields.size());
            //一般tag数量不会太多
            Map<String, Object> tagMap = new HashMap<>(4);
            for (Map.Entry<String, DynamicField> entry : dynamicFields.entrySet()) {
                DynamicField dynamicField = entry.getValue();
                if (dynamicField.getIsTag()) {
                    tagMap.put(dynamicField.getColumnName(), data.get(dynamicField.getFieldName()));
                }else {
                    fieldMap.put(dynamicField.getColumnName(), data.get(dynamicField.getFieldName()));
                }
            }
            fieldList.add(fieldMap);
            tagList.add(tagMap);
        }
        return MutablePair.of(tagList, fieldList);
    }
    /**
     * 根据类或接口的getter方法构建查询Example
     * @param domain
     */
    protected void buildDynamicDomainExample(DynamicDomain domain, Example example){
        Example.Criteria criteria = example.createCriteria();
        buildWhereBlock(domain, criteria);
        //拼接自定义and conditon expr
        if(domain.mget(IDTO.AND_CONDITION_EXPR) != null){
            criteria.andCondition(domain.mget(IDTO.AND_CONDITION_EXPR).toString());
        }
        //拼接自定义or conditon expr
        if(domain.mget(IDTO.OR_CONDITION_EXPR) != null){
            criteria.orCondition(domain.mget(IDTO.OR_CONDITION_EXPR).toString());
        }
        if (domain.getDynamicTableName() != null) {
            example.setTableName(domain.getDynamicTableName());
        }else{
            example.setTableName(domain.getTableName());
        }
        //设置@OrderBy注解的排序(会被ITaosDomain中的排序字段覆盖)
        buildDynamicFieldsOrderByClause(domain.getDynamicConditions(), example);
        //设置ITaosDomain中的排序字段(会覆盖@OrderBy注解的排序)
        setOrderBy(domain, example);
    }

    /**
     * 构建where块
     * @param domain
     * @param criteria
     */
    private void buildWhereBlock(DynamicDomain domain, Example.Criteria criteria ) {
        //解析空值字段(where xxx is null)
        Set<String> nullFields = parseNullField(domain, criteria);
        Set<String> notNullFields = parseNotNullField(domain, criteria);
        Map<String, DynamicCondition> dynamicConditions = domain.getDynamicConditions();
        if(dynamicConditions == null || dynamicConditions.isEmpty()){
            return;
        }
        for (Map.Entry<String, DynamicCondition> entry : dynamicConditions.entrySet()) {
            DynamicCondition dynamicCondition = entry.getValue();
            //不处理非条件类型
            if (!DynamicConditionType.CONDITION.getCode().equals(dynamicCondition.getType())) {
                continue;
            }
            Object value = dynamicCondition.getValue();
            String columnName = dynamicCondition.getColumnName();
            //跳过空值字段
            if(nullFields.contains(columnName) || notNullFields.contains(columnName)){
                continue;
            }
            //没值就不拼接sql
            if(value == null) {
                continue;
            }
            //防注入
            if(value instanceof String && !checkXss((String)value)){
                throw new ParamErrorException("SQL注入拦截:"+value);
            }
            if (dynamicCondition.getLikeType() != null) {
                andLike(criteria, columnName, dynamicCondition.getLikeType(), value);
            } else if (dynamicCondition.getOperator() != null) {
                Class aClass = ClassCache.classCaches.get(dynamicCondition.getFieldType());
                if (aClass == null) {
                    try {
                        ClassCache.classCaches.put(dynamicCondition.getFieldType(), Class.forName(dynamicCondition.getFieldType()));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        continue;
                    }
                }
                if (!andOperator(criteria, columnName, aClass, dynamicCondition.getOperator(), value)) {
                    continue;
                }
            } else {
                andEqual(criteria, columnName, value);
            }
        }
    }

    /**
     * 设置@OrderBy注解的排序
     */
    protected void buildDynamicFieldsOrderByClause(Map<String, DynamicCondition> dynamicFields, Example example){
        StringBuilder orderByClauseBuilder = new StringBuilder();
        for (Map.Entry<String, DynamicCondition> entry : dynamicFields.entrySet()) {
            String orderBy = entry.getValue().getOrderBy();
            if(orderBy == null) {
                continue;
            }
            orderByClauseBuilder.append(","+entry.getValue().getColumnName()+" "+orderBy);
        }
        if(orderByClauseBuilder.length()>1) {
            example.setOrderByClause(orderByClauseBuilder.substring(1));
        }
    }


    /**
     * 构建where后缀函数
     * (支持interval, sliding和 fill)
     * @param domain
     * @param exampleExpand
     */
    private void buildWhereSuffix(DynamicDomain domain, ExampleExpand exampleExpand ){
        if(StringUtils.isNotBlank(domain.getWhereSuffixSql())){
            exampleExpand.setWhereSuffixSql(domain.getWhereSuffixSql());
        }else{
            Map<String, DynamicCondition> dynamicConditions = domain.getDynamicConditions();
            if (dynamicConditions == null || dynamicConditions.isEmpty()) {
                return;
            }
            StringBuilder suffix = new StringBuilder(32);
            for (Map.Entry<String, DynamicCondition> entry : dynamicConditions.entrySet()) {
                DynamicCondition dynamicCondition = entry.getValue();
                if (DynamicConditionType.SUFFIX.getCode().equals(dynamicCondition.getType())) {
                    if (dynamicCondition.getFunc() != null) {
                        suffix.append(dynamicCondition.getFunc()).append("(").append(dynamicCondition.getValue()).append(") ");
                    }else{
                        suffix.append(entry.getValue());
                    }
                }
            }
            exampleExpand.setWhereSuffixSql(suffix.toString());
        }
    }

    /**
     * 指定要查询的属性列 - 这里会自动映射到表字段
     * 构建Example，并设置selectColumns和WhereSuffixSql，并判断是否防止注入
     * @param entityClass
     * @return
     */
    protected ExampleExpand createDynamicExample(DynamicDomain domain, Class<?> entityClass) {
        ExampleExpand exampleExpand = ExampleExpand.of(entityClass);
        Set<String> columnsSet = null;
        Boolean checkInjection = false;
        //设置WhereSuffixSql
        buildWhereSuffix(domain, exampleExpand);
        if (CollectionUtils.isNotEmpty(domain.getSelectColumns())) {
            columnsSet = domain.getSelectColumns();
        }
        if(domain.getCheckInjection() != null) {
            checkInjection = domain.getCheckInjection();
        }
        // 如果没有自定义的columnsSet，则构建默认的
        // 暂不考虑查询Tag
        if(CollectionUtils.isEmpty(columnsSet)){
            columnsSet = new HashSet<>(domain.getDynamicFields().size());
            //先找出所有Func注解
            for (Map.Entry<String, DynamicField> entry : domain.getDynamicFields().entrySet()) {
                DynamicField dynamicField = entry.getValue();
                //暂不查询tag
                if(dynamicField.getIsTag()){
                    continue;
                }
                //有Func的字段
                String func = dynamicField.getFunc();
                String columnName = dynamicField.getColumnName();
                if(StringUtils.isBlank(func)){
                    if (StringUtils.isBlank(dynamicField.getAlias())) {
                        columnsSet.add(columnName);
                    }else{
                        columnsSet.add(columnName + " as " + dynamicField.getAlias());
                    }
                }else{
                    String alias = StringUtils.isBlank(dynamicField.getAlias()) ? columnName : dynamicField.getAlias();
                    columnsSet.add(new StringBuilder().append(func).append("(").append(entry.getValue().getColumnName()).append(") as ").append(alias).toString());
                }
            }
        }
        //如果不检查，则用反射强制注入
        if (!checkInjection) {
            try {
                Field selectColumnsField = Example.class.getDeclaredField("selectColumns");
                selectColumnsField.setAccessible(true);
                selectColumnsField.set(exampleExpand, columnsSet);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return exampleExpand;
        } else {//如果要检查字段(防止注入)
            //防止SQL注入
            exampleExpand.selectProperties(columnsSet.toArray(new String[]{}));
            return exampleExpand;
        }
    }
}
