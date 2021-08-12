package com.mxny.ss.dto;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Map;


/**
 * 基础实体类
 */
public interface IDomain<KEY extends Serializable> extends IDTO {

//	@Transient
//	KEY getId();
//	void setId(KEY id);

	@Transient
	Integer getPage();
	void setPage(Integer page);

	@Transient
	Integer getRows();
	void setRows(Integer rows);

	/**
	 * 排序字段，多个以逗号分隔
	 * @return
	 */
	@Transient
	String getSort();
	void setSort(String sort);

	/**
	 * 排序方式asc,desc，多个以逗号分隔
	 * @return
	 */
	@Transient
	String getOrder();
	void setOrder(String order);

	@Transient
	Object getMetadata(String key);
	void setMetadata(String key, Object value);

	@Transient
	Map getMetadata();
	void setMetadata(Map metadata);

	Boolean containsMetadata(String key);
}
