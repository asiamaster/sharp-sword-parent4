package com.mxny.ss.base;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.mxny.ss.dao.ExampleExpand;
import com.mxny.ss.domain.BaseDomain;
import com.mxny.ss.domain.BasePage;
import com.mxny.ss.domain.EasyuiPageOutput;
import com.mxny.ss.domain.annotation.FindInSet;
import com.mxny.ss.domain.annotation.Like;
import com.mxny.ss.domain.annotation.Operator;
import com.mxny.ss.domain.annotation.SqlOperator;
import com.mxny.ss.dto.*;
import com.mxny.ss.exception.ParamErrorException;
import com.mxny.ss.metadata.ValueProviderUtils;
import com.mxny.ss.util.DateUtils;
import com.mxny.ss.util.POJOUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.IDynamicTableName;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import java.io.Serializable;
import java.lang.reflect.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Pattern;


/**
 *	服务基类
 *
 * @author asiamastor
 * @date 2016/12/28
 */
public abstract class BaseServiceAdaptor<T extends IDomain, KEY extends Serializable> implements BaseService<T, KEY> {
	protected static final Logger LOGGER = LoggerFactory.getLogger(BaseServiceAdaptor.class);

	/**
	 * 如果不使用通用mapper，可以自行在子类覆盖getDao方法
	 */
	public abstract MyMapper<T> getDao();

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int insert(T t) {
		return getDao().insert(t);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int insertSelective(T t) {
		return getDao().insertSelective(t);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int batchInsert(List<T> list) {
		return getDao().insertList(list);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
//	@Caching(evict = {@CacheEvict(value = "rc", key = "#root.getTarget().redisKey()+':id:' + #key")})
	public int delete(KEY key) {
		return getDao().deleteByPrimaryKey(key);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int deleteByExample(T t) {
		Class tClazz = getSuperClassGenricType(getClass(), 0);
		if(null == t) {
			t = getDefaultBean (tClazz);
		}
		Example example = new Example(tClazz);
		//接口只取getter方法
		if(tClazz.isInterface()) {
			buildExampleByGetterMethods(t, example);
		}else {//类取属性
			buildExampleByFields(t, example);
		}
		return getDao().deleteByExample(example);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
//	@Caching(evict = {@CacheEvict(value = "rc", key = "#root.getTarget().redisKey()+':id:' + #key")})
	public int delete(List<KEY> ids) {
		Type t = getClass().getGenericSuperclass();
		Class<T> entityClass = null;
		if(t instanceof ParameterizedType){
			Type[] p = ((ParameterizedType)t).getActualTypeArguments();
			entityClass = (Class<T>)p[0];
		}
		Example example = new Example(entityClass);
		example.createCriteria().andIn("id", ids);
		return getDao().deleteByExample(example);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
//	@Caching(evict = {@CacheEvict(value = "rc", key = "#root.getTarget().redisKey()+':id:' + #condtion.getId()")})
	public int updateSelective(T condtion) {
		return getDao().updateByPrimaryKeySelective(condtion);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
//	@Caching(evict = {@CacheEvict(value = "rc", key = "#root.getTarget().redisKey()+':id:' + #condtion.getId()")})
	public int updateSelectiveByExample(T domain, T condition) {
		Class tClazz = getSuperClassGenricType(getClass(), 0);
		if(null == condition) {
			condition = getDefaultBean(tClazz);
		}
		Example example = new Example(tClazz);
		//接口只取getter方法
		if(tClazz.isInterface()) {
			buildExampleByGetterMethods(condition, example);
		}else {//类取属性
			buildExampleByFields(condition, example);
		}
		return getDao().updateByExampleSelective(domain, example);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
//	@Caching(evict = {@CacheEvict(value = "rc", key = "#root.getTarget().redisKey()+':id:' + #condtion.getId()")})
	public int updateExactByExample(T domain, T condition) {
		Class tClazz = getSuperClassGenricType(getClass(), 0);
		if(null == condition) {
			condition = getDefaultBean(tClazz);
		}
		Example example = new Example(tClazz);
		//接口只取getter方法
		if(tClazz.isInterface()) {
			buildExampleByGetterMethods(condition, example);
		}else {//类取属性
			buildExampleByFields(condition, example);
		}
		return getDao().updateByExampleExact(domain, example);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
//	@Caching(evict = {@CacheEvict(value = "rc", key = "#root.getTarget().redisKey()+':id:' + #condtion.getId()")})
	public int updateExactByExampleSimple(T domain, T condition) {
		Class tClazz = getSuperClassGenricType(getClass(), 0);
		if(null == condition) {
			condition = getDefaultBean(tClazz);
		}
		Example example = new Example(tClazz);
		//接口只取getter方法
		if(tClazz.isInterface()) {
			buildExampleByGetterMethods(condition, example);
		}else {//类取属性
			buildExampleByFields(condition, example);
		}
		try {
			buildExactDomain(domain, "setForceParams");
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
		}
		return getDao().updateByExampleExact(domain, example);
	}

	@Override
//	@Caching(evict = {@CacheEvict(value = "rc", key = "#root.getTarget().redisKey()+':id:' + #condtion.getId()")})
	public int updateExact(T record){
		return getDao().updateByPrimaryKeyExact(record);
	}

    @Override
//	@Caching(evict = {@CacheEvict(value = "rc", key = "#root.getTarget().redisKey()+':id:' + #condtion.getId()")})
//	@Caching(evict = {@CacheEvict(value = "rc", key = "#root.target+':id:' + #condtion.getId()")})
    public int updateExactSimple(T record){
        try {
            buildExactDomain(record, "setForceParams");
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
        }
        return getDao().updateByPrimaryKeyExact(record);
    }

	@Override
	@Transactional(rollbackFor = Exception.class)
//	@Caching(evict = {@CacheEvict(value = "rc", key = "#root.getTarget().redisKey()+':id:' + #condtion.getId()")})
	public int update(T condtion) {
		return getDao().updateByPrimaryKey(condtion);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
//	@Caching(evict = {@CacheEvict(value = "rc", key = "#root.getTarget().redisKey()+':id:' + #condtion.getId()")})
	public int updateByExample(T domain, T condition) {
		Class tClazz = getSuperClassGenricType(getClass(), 0);
		if(null == condition) {
			condition = getDefaultBean (tClazz);
		}
		Example example = new Example(tClazz);
		//接口只取getter方法
		if(tClazz.isInterface()) {
			buildExampleByGetterMethods(condition, example);
		}else {//类取属性
			buildExampleByFields(condition, example);
		}
		return getDao().updateByExample(domain, example);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int batchUpdateSelective(List<T> list) {
		int count = 0;
		for(T t : list) {
			count+=getDao().updateByPrimaryKeySelective(t);
		}
		return count;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int batchUpdate(List<T> list) {
		int count = 0;
		for(T t : list) {
			count+=getDao().updateByPrimaryKey(t);
		}
		return count;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int saveOrUpdate(T t) {
		KEY id = null;
		if (t instanceof IBaseDomain) {
			id = (KEY)((IBaseDomain) t).getId();
		} else {
			try {
				Class<?> clz = t.getClass();
				id = (KEY) clz.getMethod("getId").invoke(t);
			} catch (Exception e) {
				LOGGER.warn("获取对象主键值失败!");
			}
		}
		if(id != null) {
			return this.update(t);
		}
		return this.insert(t);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int saveOrUpdateSelective(T t) {
		KEY id = null;
		if (t instanceof IBaseDomain) {
			id = (KEY)((IBaseDomain) t).getId();
		} else {
			try {
				Class<?> clz = t.getClass();
				id = (KEY) clz.getMethod("getId").invoke(t);
			} catch (Exception e) {
				LOGGER.warn("获取对象主键值失败!");
			}
		}
		if(id != null) {
			return this.updateSelective(t);
		}
		return this.insertSelective(t);
	}

	@Override
//	@Cacheable(value = "rc", key = "#root.getTarget().redisKey()+':id:' + #key", unless = "#result==null")
	public T get(KEY key) {
		return getDao().selectByPrimaryKey(key);
	}

	/**
	 * 根据实体查询
	 * @param condtion 查询条件
	 * @return
	 */
	@Override
	public List<T> list(T condtion) {
		return getDao().select(condtion);
	}

	/**
	 * 根据实体分页查询
	 * @param domain
	 * @return
	 */
	@Override
	public BasePage<T> listPage(T domain) {
//		T t = (T) BeanConver.copeBaseQueryBean(condtion, getSuperClassGenricType(getClass(), 0));
		//为了线程安全,请勿改动下面两行代码的顺序
		PageHelper.startPage(domain.getPage(), domain.getRows());
		List<T> list = getDao().select(domain);
		Page<T> page = (Page)list;
		BasePage<T> result = new BasePage<T>();
		result.setDatas(list);
		result.setPage(page.getPageNum());
		result.setRows(page.getPageSize());
		result.setTotalItem(Long.parseLong(String.valueOf(page.getTotal())));
		result.setTotalPage(page.getPages());
		result.setStartIndex(page.getStartRow());
		return result;
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
	public ExampleExpand getExample(T domain, Class<?> entityClass) {
		ExampleExpand exampleExpand = ExampleExpand.of(entityClass);
		if(!(domain instanceof IMybatisForceParams)){
			return exampleExpand;
		}
		IMybatisForceParams iMybatisForceParams =((IMybatisForceParams) domain);
		//这里构建Example，并设置selectColumns
		Set<String> columnsSet = iMybatisForceParams.getSelectColumns();
		if(columnsSet == null|| columnsSet.isEmpty()){
			return exampleExpand;
		}
		Boolean checkInjection = iMybatisForceParams.getCheckInjection();
		//如果不检查，则用反射强制注入
		if (checkInjection == null || !checkInjection) {
			//设置WhereSuffixSql
			if(StringUtils.isNotBlank(iMybatisForceParams.getWhereSuffixSql())){
				exampleExpand.setWhereSuffixSql(iMybatisForceParams.getWhereSuffixSql());
			}
			try {
				Field selectColumnsField = Example.class.getDeclaredField("selectColumns");
				selectColumnsField.setAccessible(true);
				selectColumnsField.set(exampleExpand, columnsSet);
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			return exampleExpand;
		} else {//如果要检查字段(防止注入)
//			ExampleExpand.Builder builder = new Example.Builder(entityClass);
//			builder.select(columnsSet.toArray(new String[]{}));
//			ExampleExpand exampleExpand1 = ExampleExpand.of(entityClass, builder);
			ExampleExpand exampleExpand1 = ExampleExpand.of(entityClass);
			exampleExpand1.selectProperties(columnsSet.toArray(new String[]{}));
			//设置WhereSuffixSql
			if(StringUtils.isNotBlank(iMybatisForceParams.getWhereSuffixSql())){
				exampleExpand1.setWhereSuffixSql(iMybatisForceParams.getWhereSuffixSql());
			}
			return exampleExpand1;
		}
	}

	/**
	 * 用于支持like, order by 的查询，支持分页
	 * @param domain
	 * @return
	 */
	@Override
	public List<T> listByExample(T domain){
		Class tClazz = getSuperClassGenricType(getClass(), 0);
		if(null == domain) {
			domain = getDefaultBean (tClazz);
		}
		ExampleExpand example = getExample(domain, tClazz);
		//接口只取getter方法
		if(tClazz.isInterface()) {
			buildExampleByGetterMethods(domain, example);
		}else {//类取属性
			buildExampleByFields(domain, example);
		}
		//设置分页信息
		Integer page = domain.getPage();
		page = (page == null) ? Integer.valueOf(1) : page;
		if(domain.getRows() != null && domain.getRows() >= 0) {
			//为了线程安全,请勿改动下面两行代码的顺序
			PageHelper.startPage(page, domain.getRows());
		}else if(domain.getRows() != null && domain.getRows() < 0){
			PageHelper.startPage(page, 0);
		}
		//设置动态表名
		if(domain instanceof IDynamicTableName){
			example.setTableName(((IDynamicTableName)domain).getDynamicTableName());
		}
		return getDao().selectByExampleExpand(example);
	}

	/**
	 * 用于支持like, order by 的分页查询
	 * @param domain
	 * @return
	 */
	@Override
	public BasePage<T> listPageByExample(T domain){
		List<T> list = listByExample(domain);
		BasePage<T> result = new BasePage<T>();
		result.setDatas(list);
		if(list instanceof Page) {
			Page<T> page = (Page) list;
			result.setPage(page.getPageNum());
			result.setRows(page.getPageSize());
			result.setTotalItem(Long.parseLong(String.valueOf(page.getTotal())));
			result.setTotalPage(page.getPages());
			result.setStartIndex(page.getStartRow());
		}else{
			result.setPage(1);
			result.setRows(list.size());
			result.setTotalItem((long)list.size());
			result.setTotalPage(1);
			result.setStartIndex(1L);
		}
		return result;
	}

	@Override
	public List<T> selectByExample(Object example){
		return getDao().selectByExample(example);
	}

	@Override
	public T selectByPrimaryKey(KEY key){
		return getDao().selectByPrimaryKey(key);
	}

	@Override
	public boolean existsWithPrimaryKey(KEY key){
		return getDao().existsWithPrimaryKey(key);
	}

    @Override
    public int insertExact(T t){
        return getDao().insertExact(t);
    }

    @Override
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
	@Override
	public EasyuiPageOutput listEasyuiPageByExample(T domain, boolean useProvider) throws Exception {
		List<T> list = listByExample(domain);
		long total = list instanceof Page ? ( (Page) list).getTotal() : list.size();
		List results = useProvider ? ValueProviderUtils.buildDataByProvider(domain, list) : list;
		return new EasyuiPageOutput(total, results);
	}

	/**
	 * 根据实体查询easyui分页结果
	 * @param domain
	 * @return
	 */
	@Override
	public EasyuiPageOutput listEasyuiPage(T domain, boolean useProvider) throws Exception {
		if(domain.getRows() != null && domain.getRows() >= 1) {
			//为了线程安全,请勿改动下面两行代码的顺序
			PageHelper.startPage(domain.getPage(), domain.getRows());
		}
		List<T> list = getDao().select(domain);
		long total = list instanceof Page ? ( (Page) list).getTotal() : list.size();
		List results = useProvider ? ValueProviderUtils.buildDataByProvider(domain, list) : list;
		return new EasyuiPageOutput(total, results);
	}

    //========================================= 私有方法分界线 =========================================

    /**
     * 设置IBaseDomain中的排序字段
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
        parseNullField(domain, criteria);
		parseNotNullField(domain, criteria);
        List<Field> fields = new ArrayList<>();
        getDeclaredField(domain.getClass(), fields);
        for(Field field : fields){
            Column column = field.getAnnotation(Column.class);
            String columnName = column == null ? field.getName() : column.name();
//			跳过空值字段
            if(isNullField(columnName, domain.getMetadata(IDTO.NULL_VALUE_FIELD)) || isNotNullField(columnName, domain.getMetadata(IDTO.NOT_NULL_VALUE_FIELD))){
                continue;
            }
            Transient transient1 = field.getAnnotation(Transient.class);
            if(transient1 != null) {
                continue;
            }
            Like like = field.getAnnotation(Like.class);
            Operator operator = field.getAnnotation(Operator.class);
			FindInSet findInSet = field.getAnnotation(FindInSet.class);
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
				} else if (findInSet != null) {
					andFindInSet(criteria, columnName, value);
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
				} else if (findInSet != null) {
					orFindInSet(criteria, columnName, value);
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

        StringBuilder orderByClauseBuilder = new StringBuilder();
        for(Field field : tClazz.getFields()) {
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
     * 排除非getter，getPage(),getRows(),getMetadata()和getMetadata(String key)等IBaseDomain或BaseDomain上定义的基础方法
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
        //排除IBaseDomain或BaseDomain上定义的基础方法
        if (IBaseDomain.class.equals(declaringClass) || BaseDomain.class.equals(declaringClass)){
            return true;
        }
        return false;
    }

    /**
     * 根据类或接口的getter方法构建查询Example
     * @param domain
     */
    protected void buildExampleByGetterMethods(T domain, Example example){
        Example.Criteria criteria = example.createCriteria();
        Class tClazz = DTOUtils.getDTOClass(domain);
        //解析空值字段(where xxx is null)
        parseNullField(domain, criteria);
		parseNotNullField(domain, criteria);
        List<Method> methods = new ArrayList<>();
        //设置子类和所有超类的方法
        getDeclaredMethod(tClazz, methods);
        for(Method method : methods){
            if(excludeMethod(method)) {
                continue;
            }
            Column column = method.getAnnotation(Column.class);
            //数据库列名
            String columnName = column == null ? POJOUtils.humpToLineFast(POJOUtils.getBeanField(method)) : column.name();
//			跳过空值字段
            if(isNullField(columnName, domain.mget(IDTO.NULL_VALUE_FIELD)) || isNotNullField(columnName, domain.mget(IDTO.NOT_NULL_VALUE_FIELD))){
                continue;
            }
            Transient transient1 = method.getAnnotation(Transient.class);
            if(transient1 != null) {
                continue;
            }
            Like like = method.getAnnotation(Like.class);
			Operator operator = method.getAnnotation(Operator.class);
			FindInSet findInSet = method.getAnnotation(FindInSet.class);
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
				} else if (findInSet != null) {
					andFindInSet(criteria, columnName, value);
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
				} else if (findInSet != null) {
					orFindInSet(criteria, columnName, value);
				} else {
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
        //设置@OrderBy注解的排序(会被IBaseDomain中的排序字段覆盖)
		buildOrderByClause(methods, example);
//		设置IBaseDomain中的排序字段(会覆盖@OrderBy注解的排序)
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
	 * or findInSet
	 * @param criteria
	 * @param columnName
	 * @param value
	 */
	private void orFindInSet(Example.Criteria criteria, String columnName, Object value){
		if(Number.class.isAssignableFrom(value.getClass())){
			criteria = criteria.orCondition("find_in_set (" + value + ", "+columnName+")");
		}else{
			criteria = criteria.orCondition("find_in_set ('" + value + "', "+columnName+")");
		}
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
	 * and findInSet
	 * @param criteria
	 * @param columnName
	 * @param value
	 */
	private void andFindInSet(Example.Criteria criteria, String columnName, Object value){
		if(Number.class.isAssignableFrom(value.getClass())){
			criteria = criteria.andCondition("find_in_set (" + value + ", "+columnName+")");
		}else{
			criteria = criteria.andCondition("find_in_set ('" + value + "', "+columnName+")");
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
	private void buildOrderByClause(List<Method> methods, Example example){
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
	 * 判断是否为空值字段
	 * @param columnName
	 * @param nullValueField
	 * @return
	 */
	private boolean isNullField(String columnName, Object nullValueField){
		if(nullValueField != null){
			if(nullValueField instanceof String){
				if(columnName.equals(nullValueField)){
					return true;
				}
			}else if(nullValueField instanceof List){
				return ((List) nullValueField).contains(columnName);
			}
		}
		return false;
	}

	/**
	 * 判断是否为空值字段
	 * @param columnName
	 * @param notNullValueField
	 * @return
	 */
	private boolean isNotNullField(String columnName, Object notNullValueField){
		if(notNullValueField != null){
			if(notNullValueField instanceof String){
				if(columnName.equals(notNullValueField)){
					return true;
				}
			}else if(notNullValueField instanceof List){
				return ((List) notNullValueField).contains(columnName);
			}
		}
		return false;
	}

	/**
	 * 如果metadata中有空值字段名，则解析为field is null
	 */
	private void parseNullField(T domain, Example.Criteria criteria){
		//如果metadata中有空值字段名，则解析为field is null
		Object nullValueField = DTOUtils.getDTOClass(domain).isInterface() ? domain.mget(IDTO.NULL_VALUE_FIELD) : domain.getMetadata(IDTO.NULL_VALUE_FIELD);
		if(nullValueField != null){
			if(nullValueField instanceof String){
				criteria = criteria.andCondition(nullValueField + " is null ");
			}else if(nullValueField instanceof List){
				List<String> nullValueFields = (List)nullValueField;
				for(String field : nullValueFields){
					criteria = criteria.andCondition(field + " is null ");
				}
			}
		}
	}

	/**
	 * 如果metadata中有非空值字段名，则解析为field is not null
	 */
	private void parseNotNullField(T domain, Example.Criteria criteria){
		//如果metadata中有空值字段名，则解析为field is null
		Object notNullValueField = DTOUtils.getDTOClass(domain).isInterface() ? domain.mget(IDTO.NOT_NULL_VALUE_FIELD) : domain.getMetadata(IDTO.NOT_NULL_VALUE_FIELD);
		if(notNullValueField != null){
			if(notNullValueField instanceof String){
				criteria = criteria.andCondition(notNullValueField + " is not null ");
			}else if(notNullValueField instanceof List){
				List<String> nullValueFields = (List)notNullValueField;
				for(String field : nullValueFields){
					criteria = criteria.andCondition(field + " is not null ");
				}
			}
		}
	}
}