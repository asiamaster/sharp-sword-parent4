package com.mxny.ss.base;

import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.mxny.ss.constant.ClassCache;
import com.mxny.ss.dao.ExampleExpand;
import com.mxny.ss.domain.DynamicCondition;
import com.mxny.ss.domain.DynamicDomain;
import com.mxny.ss.domain.DynamicField;
import com.mxny.ss.domain.SelectColumn;
import com.mxny.ss.dto.DTOUtils;
import com.mxny.ss.dto.IDTO;
import com.mxny.ss.dto.ITaosDomain;
import com.mxny.ss.dto.TaosFieldDescribe;
import com.mxny.ss.exception.ParamErrorException;
import com.mxny.ss.glossary.DynamicConditionType;
import com.mxny.ss.service.DynamicFieldService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.lang.reflect.Field;
import java.sql.ResultSetMetaData;
import java.util.*;

/**
 * 动态实体服务
 * @author: WM
 * @time: 2021/9/27 11:40
 */
@Service
public class DynamicTaosService extends BaseTaosService<DynamicDomain> {

    @Autowired
    protected DynamicFieldService dynamicFieldService;

    /**
     * 创建TAOS表
     * @param dynamicFields
     */
    public void createTable(List<DynamicField> dynamicFields){
        getJdbcTemplate().execute(buildCreateTableSql(dynamicFields));
    }

    /**
     * 添加字段
     * TDEngine只支持一次添加一个字段
     * @param dynamicFields
     */
    @Transactional(rollbackFor = Exception.class)
    public void addDynamicFields(List<DynamicField> dynamicFields){
        for (DynamicField dynamicField : dynamicFields) {
            addDynamicField(dynamicField);
        }
    }

    /**
     * 添加动态字段
     * @param dynamicField
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void addDynamicField(DynamicField dynamicField){
        if (StringUtils.isBlank(dynamicField.getColumnName())
                || StringUtils.isBlank(dynamicField.getTableName())
                || StringUtils.isBlank(dynamicField.getDataType())) {
            throw new ParamErrorException("字段类型、字段名或表名为空");
        }
        DynamicField condition = DTOUtils.newInstance(DynamicField.class);
        condition.setColumnName(dynamicField.getColumnName());
        condition.setTableName(dynamicField.getTableName());
        condition.setEnabled(true);
        List<DynamicField> list = dynamicFieldService.list(condition);
        if (!list.isEmpty()) {
            throw new ParamErrorException("动态字段重复");
        }
        dynamicField.setEnabled(true);
        dynamicFieldService.insertSelective(dynamicField);
//        DynamicRoutingDataSourceContextHolder.push(GlobalConstants.DS_TDENGINE);
//        addColumn(dynamicField);
//        DynamicRoutingDataSourceContextHolder.clear();
    }

    /**
     * 添加TAOS动态列
     * @param dynamicFields
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void addColumns(List<DynamicField> dynamicFields) {
        for (DynamicField dynamicField : dynamicFields) {
            addColumn(dynamicField);
        }
    }

    /**
     * 添加单个字段
     * @param dynamicField
     */
    @Transactional(rollbackFor = Exception.class)
    public void addColumn(DynamicField dynamicField){
        getJdbcTemplate().execute(buildAddColumnSql(dynamicField));
    }

    /**
     * 删除TAOS单个字段
     * @param dynamicField
     */
    @Transactional(rollbackFor = Exception.class)
    public void dropDynamicField(DynamicField dynamicField){
        if (StringUtils.isBlank(dynamicField.getColumnName())
                || StringUtils.isBlank(dynamicField.getTableName())){
            throw new ParamErrorException("字段名或表名为空");
        }
        DynamicField condition = DTOUtils.newInstance(DynamicField.class);
        condition.setColumnName(dynamicField.getColumnName());
        condition.setTableName(dynamicField.getTableName());
        condition.setEnabled(true);
        List<DynamicField> list = dynamicFieldService.list(condition);
        if (list.isEmpty()) {
            throw new ParamErrorException("字段不存在");
        }
        dynamicFieldService.deleteByExample(dynamicField);
//        DynamicRoutingDataSourceContextHolder.push(GlobalConstants.DS_TDENGINE);
//        dropColumn(dynamicField);
//        DynamicRoutingDataSourceContextHolder.clear();
    }

    /**
     * 删除TAOS单个字段
     * @param dynamicField
     */
    @Transactional(rollbackFor = Exception.class)
    public void dropColumn(DynamicField dynamicField){
        if (StringUtils.isBlank(dynamicField.getColumnName())
                || StringUtils.isBlank(dynamicField.getTableName())) {
            throw new ParamErrorException("字段名或表名为空");
        }
        getJdbcTemplate().execute(buildDropColumnSql(dynamicField));
    }

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
    public void insertByTags(DynamicDomain dynamicDomain, Map data) {
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
        getJdbcTemplate().execute(buildDynamicInsertSql(dynamicDomain, datas));
    }

    /**
     * 构建动态批量插入sql
     * 由于DynamicDomain仅为单子表的语义模型，所以该方法只支持单子表的批量插入
     * @param dynamicDomain， 主要参数：DynamicFields、 TableName和DynamicTableName
     * @return
     */
    public <T extends DynamicDomain> String buildDynamicInsertSql(T dynamicDomain, List<Map> datas) {
        List<DynamicField> dynamicFields = dynamicDomain.getDynamicFields();
        if(dynamicFields == null || dynamicFields.isEmpty() || CollectionUtils.isEmpty(datas)){
            return null;
        }
        //按每行数据的动态表名排序
        Collections.sort(datas, (a, b) -> {
            Object dynamicTableName1 = a.get(DynamicDomain.DYNAMIC_TABLE_NAME_KEY);
            Object dynamicTableName2 = b.get(DynamicDomain.DYNAMIC_TABLE_NAME_KEY);
            if (dynamicTableName1 == null || dynamicTableName2 == null) {
                return 0;
            }
            return dynamicTableName1.toString().compareTo(dynamicTableName2.toString());
        });

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
        int size = fieldList.size();
        for (int index = 0; index < size; index++) {
            Object currentDynamicTableName = datas.get(index).get(DynamicDomain.DYNAMIC_TABLE_NAME_KEY);
            if (currentDynamicTableName != null) {
                dynamicDomain.setDynamicTableName(currentDynamicTableName.toString());
            }
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

    /**
     * 查询表结构
     * @param tableName
     * @return
     */
    public List<TaosFieldDescribe> describe(String tableName) {
        return getJdbcTemplate().query("DESCRIBE "+tableName, (rs, rowNum) ->{
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            TaosFieldDescribe fieldDescribe = DTOUtils.newInstance(TaosFieldDescribe.class);
            for(int i = 1; i <= columnCount; ++i) {
                String column = JdbcUtils.lookupColumnName(rsmd, i);
                switch (column) {
                    case "Field" : fieldDescribe.setField(rs.getString(column));break;
                    case "Type" : fieldDescribe.setType(rs.getString(column));break;
                    case "Length" : fieldDescribe.setLength(rs.getInt(column));break;
                    case "Note" : fieldDescribe.setNote(rs.getString(column));break;
                }
            }
            return fieldDescribe;
        });
    }

    // ==============================================================================================

    /**
     * 构建建表SQL
     * @param dynamicFields
     * @return
     */
    protected String buildCreateTableSql(List<DynamicField> dynamicFields){
        if (CollectionUtils.isEmpty(dynamicFields)) {
            return null;
        }
        StringBuilder sql = new StringBuilder("CREATE STABLE IF NOT EXISTS ");
        sql.append(dynamicFields.get(0).getTableName());
        sql.append(" (").append(ITaosDomain.ID).append(" TIMESTAMP,");
        //一般tag都不会太多
        List<DynamicField> tags = new ArrayList<>(4);
        for (DynamicField dynamicField : dynamicFields) {
            //跳过主键
            if (dynamicField.getColumnName().equalsIgnoreCase(ITaosDomain.ID)) {
                continue;
            }
            //默认添加字段名
            if (dynamicField.getIsTag() == null || dynamicField.getIsTag() == false) {
                sql.append(dynamicField.getColumnName()).append(" ").append(dynamicField.getDataType());
                //字段为空，则只添加数据类型
                if (dynamicField.getDataLength() == null) {
                    sql.append(",");
                }else{
                    sql.append("(").append(dynamicField.getDataLength()).append("),");
                }
            }else{
                tags.add(dynamicField);
            }
        }
        StringBuilder resultSql = new StringBuilder();
        resultSql.append(StringUtils.stripEnd(sql.toString(), ",")).append(")");
        if (tags.isEmpty()) {
            return resultSql.toString();
        }
        resultSql.append(" TAGS (");
        sql = new StringBuilder();
        for (DynamicField tag : tags) {
            sql.append(tag.getColumnName()).append(" ").append(tag.getDataType());
            //字段为空，则只添加数据类型
            if (tag.getDataLength() == null) {
                sql.append(",");
            } else {
                sql.append("(").append(tag.getDataLength()).append("),");
            }
        }
        resultSql.append(StringUtils.stripEnd(sql.toString(), ",")).append(")");
        return resultSql.toString();
    }

    /**
     * 构建新增列SQL
     * @param dynamicField
     * @return
     */
    protected String buildAddColumnSql(DynamicField dynamicField){
        StringBuilder sql = new StringBuilder("ALTER STABLE ");
        sql.append(dynamicField.getTableName());
        //默认添加字段名
        if (dynamicField.getIsTag() == null || dynamicField.getIsTag() == false) {
            sql.append(" ADD COLUMN ");
        }else{
            sql.append(" ADD TAG ");
        }
        sql.append(dynamicField.getColumnName()).append(" ");
        sql.append(dynamicField.getDataType());
        //字段为空，则只添加数据类型
        if (dynamicField.getDataLength() != null) {
            sql.append("(").append(dynamicField.getDataLength()).append(")");
        }
        return sql.toString();
    }

    /**
     * 构建删除字段SQL
     * @param dynamicField
     * @return
     */
    protected String buildDropColumnSql(DynamicField dynamicField){
        StringBuilder sql = new StringBuilder("ALTER STABLE ");
        sql.append(dynamicField.getTableName());
        //默认添加字段名
        if (dynamicField.getIsTag() == null || dynamicField.getIsTag() == false) {
            sql.append(" DROP COLUMN ");
        } else {
            sql.append(" DROP TAG ");
        }
        sql.append(dynamicField.getColumnName());
        return sql.toString();
    }

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
            List<DynamicField> dynamicFields = dynamicDomain.getDynamicFields();
            Map<String, Object> fieldMap = new HashMap<>(dynamicFields.size());
            //一般tag数量不会太多
            Map<String, Object> tagMap = new HashMap<>(4);
            for (DynamicField dynamicField : dynamicFields) {
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
        //设置DynamicCondition的排序(会被domain中的排序字段覆盖)
        if (CollectionUtils.isNotEmpty(domain.getDynamicConditions())) {
            buildDynamicFieldsOrderByClause(domain.getDynamicConditions(), example);
        }
        //设置domain中的排序字段(会覆盖DynamicCondition的排序)
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
        List<DynamicCondition> dynamicConditions = domain.getDynamicConditions();
        if(dynamicConditions == null || dynamicConditions.isEmpty()){
            return;
        }
        //查询条件排序，后期用于处理括号
        Collections.sort(dynamicConditions, (a, b) -> {
            return a.getOrderNumber().compareTo(b.getOrderNumber());
        });
        for (DynamicCondition dynamicCondition : dynamicConditions) {
            //这里只处理条件类型，非条件类型在whereSuffix处理
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
                        aClass = Class.forName(dynamicCondition.getFieldType());
                        ClassCache.classCaches.put(dynamicCondition.getFieldType(), aClass);
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
    protected void buildDynamicFieldsOrderByClause(List<DynamicCondition> dynamicConditions, Example example){
        StringBuilder orderByClauseBuilder = new StringBuilder();
        for (DynamicCondition entry : dynamicConditions) {
            String orderBy = entry.getOrderType();
            if(orderBy == null) {
                continue;
            }
            if (!DynamicConditionType.SORT.getCode().equals(entry.getType())) {
                continue;
            }
            orderByClauseBuilder.append(","+entry.getColumnName()+" "+orderBy);
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
            List<DynamicCondition> dynamicConditions = domain.getDynamicConditions();
            if (dynamicConditions == null || dynamicConditions.isEmpty()) {
                return;
            }
            StringBuilder suffix = new StringBuilder(32);
            for (DynamicCondition dynamicCondition : dynamicConditions) {
                if (DynamicConditionType.SUFFIX.getCode().equals(dynamicCondition.getType())) {
                    if (dynamicCondition.getFunc() != null) {
                        suffix.append(dynamicCondition.getFunc()).append("(").append(dynamicCondition.getValue()).append(") ");
                    } else {
                        suffix.append(dynamicCondition.getValue());
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
            //如果动态查询列不为空，则转为columnsSet，否则查询所有字段
            if(domain.getSelectColumnList() != null && !domain.getSelectColumnList().isEmpty()){
                //先找出所有Func注解
                for (SelectColumn selectColumn : domain.getSelectColumnList()) {
                    //有Func的字段
                    String func = selectColumn.getFunc();
                    String columnName = selectColumn.getColumnName();
                    if(StringUtils.isBlank(func)){
                        if (StringUtils.isBlank(selectColumn.getAlias())) {
                            columnsSet.add(columnName);
                        }else{
                            columnsSet.add(columnName + " as " + selectColumn.getAlias());
                        }
                    }else{
                        String alias = StringUtils.isBlank(selectColumn.getAlias()) ? columnName : selectColumn.getAlias();
                        columnsSet.add(new StringBuilder().append(func).append("(").append(selectColumn.getColumnName()).append(") as ").append(alias).toString());
                    }
                }
            }else{
                //先找出所有Func注解
                for (DynamicField dynamicField : domain.getDynamicFields()) {
                    //暂不查询tag
                    if(dynamicField.getIsTag()){
                        continue;
                    }
//                    columnsSet.add(dynamicField.getColumnName() + " as " + dynamicField.getFieldName());
                    columnsSet.add(dynamicField.getColumnName());
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
