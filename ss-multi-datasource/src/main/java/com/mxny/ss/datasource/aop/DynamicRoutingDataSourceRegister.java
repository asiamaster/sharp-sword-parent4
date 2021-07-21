package com.mxny.ss.datasource.aop;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.mxny.ss.constant.SsConstants;
import com.mxny.ss.datasource.*;
import com.mxny.ss.datasource.selector.RoundRobinSelector;
import com.mxny.ss.datasource.selector.WeightedRoundRobinSelector;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.util.text.BasicTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

//import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
//import org.springframework.boot.bind.RelaxedDataBinder;
//import org.springframework.boot.bind.RelaxedPropertyResolver;

/**
 * 动态数据源注册<br/>
 * 启动动态数据源请在启动类中（如SpringBootApplication）
 * 添加 @Import(DynamicDataSourceRegister.class)
 * Created by asiamaster on 2017/8/8 0008.
 */
public class DynamicRoutingDataSourceRegister implements ImportBeanDefinitionRegistrar, EnvironmentAware {

	private static final Logger logger = LoggerFactory.getLogger(DynamicRoutingDataSourceRegister.class);

	// 如配置文件中未指定数据源类型，使用该默认值
//	org.apache.tomcat.jdbc.pool.DataSource
	private static final String DATASOURCE_TYPE_DEFAULT = "com.alibaba.druid.pool.DruidDataSource";
	private static final String DATASOURCE_TYPE_HIKARI = "com.zaxxer.hikari.HikariDataSource";

	// 默认数据源
	private DataSource defaultDataSource;
	// 其它数据源
	public static Map<String, DataSource> customDataSources = new HashMap<>();

	@Autowired
	PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer;

	/**
	 * 加载多数据源配置
	 */
	@Override
	public void setEnvironment(Environment env) {
		initDefaultDataSource(env);
		initCustomDataSources(env);
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		Map<String, Object> allDataSources = new HashMap<String, Object>();
		// 将主数据源添加到更多数据源中
		allDataSources.put(SwitchDataSource.DEFAULT_DATASOURCE, defaultDataSource);
		// 添加更多数据源
		allDataSources.putAll(customDataSources);
		// 创建DynamicRoutingDataSource
		GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
		if(DataSourceManager.switchMode.equals(SwitchMode.MULTI)) {
			beanDefinition.setBeanClass(DynamicRoutingDataSource.class);
		}else {
			beanDefinition.setBeanClass(DynamicSelectorDataSource.class);
		}
		beanDefinition.setSynthetic(true);
		MutablePropertyValues mpv = beanDefinition.getPropertyValues();
		//设置AbstractRoutingDataSource的数据源(不会使用)
		mpv.addPropertyValue("defaultTargetDataSource", defaultDataSource);
		mpv.addPropertyValue("targetDataSources", allDataSources);
		//设置自定义的数据源
		mpv.addPropertyValue("defaultDataSource", defaultDataSource);
		mpv.addPropertyValue("dataSourceMap", allDataSources);
		if(DataSourceManager.switchMode.equals(SwitchMode.MASTER_SLAVE)) {
			if(DataSourceManager.selectorMode.equals(SelectorMode.ROUND_ROBIN)) {
				mpv.addPropertyValue("dataSourceSelector", new RoundRobinSelector());
			}else{
				mpv.addPropertyValue("dataSourceSelector", new WeightedRoundRobinSelector());
			}
		}
		registry.registerBeanDefinition("dataSource", beanDefinition);
		logger.info("DynamicRoutingDataSource Registry");
	}

	/**
	 * 初始化主数据源
	 */
	private void initDefaultDataSource(Environment env) {
		// 读取主数据源
//		RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "spring.datasource.");
		//初始化数据源切换模式，默认为1(多数据源)
		DataSourceManager.switchMode = SwitchMode.getSwitchModeByCode(env.getProperty("spring.datasource.switch-mode", "1"));
		if(SwitchMode.MASTER_SLAVE.equals(DataSourceManager.switchMode)) {
			//负载均衡模式，默认为轮询
			DataSourceManager.selectorMode = SelectorMode.getSelectorModeByCode(env.getProperty("spring.datasource.selector-mode", "1"));
		}
		defaultDataSource = buildDataSource(env, null);
	}

	/**
	 * 初始化更多数据源
	 */
	private void initCustomDataSources(Environment env) {
		// 读取配置文件获取更多数据源，也可以通过defaultDataSource读取数据库获取更多数据源
//		RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "spring.datasource.");
		String dsPrefixs = env.getProperty("spring.datasource.names");
		if(StringUtils.isBlank(dsPrefixs)){
			return;
		}
		for (String dsPrefix : dsPrefixs.split(",")) {// 多个数据源
			DataSource ds = buildDataSource(env, dsPrefix);
			customDataSources.put(dsPrefix, ds);
			//主从数据源需要初始化DataSourceManager的数据
			if(SwitchMode.MASTER_SLAVE.equals(DataSourceManager.switchMode)){
				DataSourceManager.slaves.add(dsPrefix);
				String weightStr = env.getProperty("spring.datasource." + dsPrefix + ".weight", "1");
				DataSourceManager.weights.put(dsPrefix, Integer.parseInt(weightStr));
			}
		}
	}

	/**
	 * 创建DataSource
	 *
	 * @param env	统一配置从环境中取
	 * @return
	 */
	private DataSource buildDataSource(Environment env, String dsPrefix) {
		try {
			Map<String, String> dsMap = new HashMap<>();
			if(StringUtils.isNotBlank(dsPrefix)) {
				dsMap.put("type", env.getProperty("spring.datasource." + dsPrefix + ".type"));
				dsMap.put("driver-class-name", env.getProperty("spring.datasource." + dsPrefix + ".driver-class-name"));
				dsMap.put("url", env.getProperty("spring.datasource." + dsPrefix + ".url"));
				dsMap.put("username", env.getProperty("spring.datasource." + dsPrefix + ".username"));
				dsMap.put("password", env.getProperty("spring.datasource." + dsPrefix + ".password"));
			}else{
				dsMap.put("type", env.getProperty("spring.datasource.type"));
				dsMap.put("driver-class-name", env.getProperty("spring.datasource.driver-class-name"));
				dsMap.put("url", env.getProperty("spring.datasource.url"));
				dsMap.put("username", env.getProperty("spring.datasource.username"));
				dsMap.put("password", env.getProperty("spring.datasource.password"));
			}

			String type = dsMap.get("type");
			if (type == null) {
				type = DATASOURCE_TYPE_DEFAULT;// 默认DataSource
				dsMap.put("type", type);
			}
			if(!type.equals(DATASOURCE_TYPE_DEFAULT) && !type.equals(DATASOURCE_TYPE_HIKARI)){
				throw new RuntimeException("暂不支持非DruidDataSource和HikariDataSource数据源!");
			}
//			Class<? extends DataSource> dataSourceType;
//			dataSourceType = (Class<? extends DataSource>) Class.forName((String) type);
//			DataSourceBuilder factory = DataSourceBuilder.create().driverClassName(driverClassName).url(url)
//					.username(username).password(password).type(dataSourceType);
//			DataSource dataSource =  factory.build();
//			dataBinder(dataSource, env);
			Binder binder = Binder.get(env);
			String prefix = dsPrefix == null ? "spring.datasource" : "spring.datasource." + dsPrefix;
			if(type.equals(DATASOURCE_TYPE_DEFAULT)) {
				Properties datasourceProp = binder.bind(prefix, Bindable.of(Properties.class)).get();
				datasourceProp.putAll(dsMap);
				//连接池统一配置
				return DruidDataSourceFactory.createDataSource(datasourceProp);
			}else{
				String driverClassName = dsMap.get("driver-class-name");
				String url = dsMap.get("url");
				String username = decrypt(dsMap.get("username"));
				String password = decrypt(dsMap.get("password"));
				//连接池统一配置
				Properties datasourceProp = null;
				try {
					datasourceProp = binder.bind(prefix + ".hikari", Bindable.of(Properties.class)).get();
					datasourceProp.putAll(dsMap);
				}catch (Exception e){
					logger.info("未配置hikari连接池设置，采用默认配置");
					datasourceProp = new Properties();
				}
				// 创建基础hikari数据源
				DataSourceBuilder<HikariDataSource> hikariDataSourceBuilder = DataSourceBuilder.create().type(HikariDataSource.class);
				hikariDataSourceBuilder.url(url);
				hikariDataSourceBuilder.username(username);
				hikariDataSourceBuilder.password(password);
				if(driverClassName != null) {
					hikariDataSourceBuilder.driverClassName(driverClassName);
				}
				HikariDataSource hikariDataSource = hikariDataSourceBuilder.build();
				hikariDataSource.setMinimumIdle(Integer.parseInt(datasourceProp.getProperty("minimumIdle", "10")));
				hikariDataSource.setMaximumPoolSize(Integer.parseInt(datasourceProp.getProperty("maximumPoolSize", "20")));
				// 池中连接最长生命周期, 如果不等于0且小于30秒则会被重置回30分钟，单位毫秒
				hikariDataSource.setMaxLifetime(Integer.parseInt(datasourceProp.getProperty("maxLifetime", "1800000")));
				// 连接允许在池中闲置的最长时间, 如果idleTimeout+1秒>maxLifetime 且 maxLifetime>0，则会被重置为0（代表永远不会退出）；如果idleTimeout!=0且小于10秒，则会被重置为10秒
				hikariDataSource.setIdleTimeout(Integer.parseInt(datasourceProp.getProperty("idleTimeout", "600000")));
				// 自动提交从池中返回的连接
				hikariDataSource.setAutoCommit(Boolean.parseBoolean(datasourceProp.getProperty("autoCommit", "true")));
				// 连接池的用户定义名称，主要出现在日志记录和JMX管理控制台中以识别池和池配置
				hikariDataSource.setPoolName(datasourceProp.getProperty("poolName", "HikariPool-1"));
				// 等待来自池的连接的最大毫秒数, 如果小于250毫秒，则被重置回30秒
				hikariDataSource.setConnectionTimeout(Long.parseLong(datasourceProp.getProperty("connectionTimeout", "30000")));
				// 如果您的驱动程序支持JDBC4，我们强烈建议您不要设置此属性
				hikariDataSource.setConnectionTestQuery(datasourceProp.getProperty("connectionTestQuery"));
				// 是否在其自己的事务中隔离内部池查询，例如连接活动测试
				hikariDataSource.setIsolateInternalQueries(Boolean.parseBoolean(datasourceProp.getProperty("isolateInternalQueries", "false")));
				// 从池中获取的连接是否默认处于只读模式
				hikariDataSource.setReadOnly(Boolean.parseBoolean(datasourceProp.getProperty("readOnly", "false")));
				//连接将被测试活动的最大时间量, 如果小于250毫秒，则会被重置回5秒
				hikariDataSource.setValidationTimeout(Long.parseLong(datasourceProp.getProperty("validationTimeout", "5000")));
				return hikariDataSource;
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 配置属性解密
	 * @param value
	 * @return
	 */
	private String decrypt(String value){
		if(StringUtils.isBlank(value)) {
			return value;
		}
		if(value.startsWith("ENC(") && value.endsWith(")")){
			BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
			textEncryptor.setPassword(SsConstants.ENCRYPT_PROPERTY_PASSWORD);
			return textEncryptor.decrypt(value.substring(4, value.length()-1));
		}else {
			return value;
		}
	}

	/**
	 * 为DataSource绑定更多数据
	 *
	 * @param dataSource
	 * @param env 统一配置从环境中取
	 */
	private void dataBinder(DataSource dataSource, Environment env){
		//记录spring.datasource.*除type,driver-class-name,url, username和password外的通用属性，用于DataSource属性绑定
//		PropertyValues dataSourcePropertyValues;

//		RelaxedDataBinder dataBinder = new RelaxedDataBinder(dataSource);
//		//dataBinder.setValidator(new LocalValidatorFactory().run(this.applicationContext));
//		dataBinder.setConversionService(conversionService);
//		dataBinder.setIgnoreNestedProperties(false);//false
//		dataBinder.setIgnoreInvalidFields(false);//false
//		dataBinder.setIgnoreUnknownFields(true);//true
//		Map<String, Object> rpr = new RelaxedPropertyResolver(env, "spring.datasource").getSubProperties(".");
//		Map<String, Object> values = new HashMap<>(rpr);
//		// 排除已经设置的属性
//		values.remove("type");
//		values.remove("driver-class-name");
//		values.remove("url");
//		values.remove("username");
//		values.remove("password");
//			dataSourcePropertyValues = new MutablePropertyValues(values);
//		dataBinder.bind(dataSourcePropertyValues);

//		Binder binder = Binder.get(env);
//		Properties datasourceProp = binder.bind("spring.datasource", Bindable.of(Properties.class)).get();
//		// 排除已经设置的属性
//		datasourceProp.remove("type");
//		datasourceProp.remove("driver-class-name");
//		datasourceProp.remove("url");
//		datasourceProp.remove("username");
//		datasourceProp.remove("password");
//		try {
//			DruidDataSourceFactory.createDataSource(datasourceProp);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		Class<? extends DataSource> dsClazz = dataSource.getClass();




	}




}