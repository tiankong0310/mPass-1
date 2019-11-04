package com.ibyte.component.jpa.contributor;

import lombok.Getter;
import lombok.Setter;

/**
 * Hibernate字段特性
 * 
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 */
@Getter
@Setter
public class HibernatePropertyFeature {
	/** 列 */
	private String column;

	/** 是否乐观锁版本 */
	private boolean version;

	/** 多对多中间表 */
	private String joinTable;

	/** 多对多连接主表的列 */
	private String keyColumn;

	/** 列表index列 */
	private String indexColumn;

	/** 列定义 */
	private String columnDefinition;
}
