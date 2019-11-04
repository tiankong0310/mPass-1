package com.ibyte.component.jpa.contributor;

import com.ibyte.common.core.util.DatabaseUtil;
import com.ibyte.common.util.IDGenerator;
import com.ibyte.common.util.ReflectUtil;
import com.ibyte.common.util.StringHelper;
import com.ibyte.framework.meta.MetaConstant;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.MappingException;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataContributor;
import org.hibernate.cfg.SecondPass;
import org.hibernate.mapping.*;
import org.hibernate.type.descriptor.converter.AttributeConverterTypeAdapter;
import org.jboss.jandex.IndexView;

import javax.persistence.AttributeConverter;
import java.util.Iterator;
import java.util.Map;

/**
 * Hibernate映射额外处理
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 */
public class ExtraMappingMetadataContributor
		implements MetadataContributor, MetaConstant {
	private String fkSqlType = StringHelper.join("varchar(",
			IDGenerator.LEN, ")");

	private static final String HBM_STRING = "string";

	private static final String HBM_CLOB = "materialized_clob";

	private static final String SYSORGELEMENTSUMMARY = "com.ibyte.sys.org.entity.SysOrgElementSummary";

	@Override
	public void contribute(InFlightMetadataCollector collector,
			IndexView jandexIndex) {
		DatabaseUtil.setDialect(collector.getDatabase().getDialect().getClass()
				.getSimpleName());
		// SecondPass将在hbm初始化完成后触发
		collector.addSecondPass(new SecondPass() {
			private static final long serialVersionUID = 481789941157136484L;

			@SuppressWarnings("rawtypes")
			@Override
			public void doSecondPass(Map persistentClasses)
					throws MappingException {
				for (Object clazz : persistentClasses.values()) {
					PersistentClass pclazz = (PersistentClass) clazz;
					Iterator<?> iterator = pclazz.getPropertyIterator();
					while (iterator.hasNext()) {
						Property prop = (Property) iterator.next();
						Value value = prop.getValue();
						if (value instanceof ToOne) {
							handleToOne(pclazz, (ToOne) value);
						} else if (value instanceof SimpleValue) {
							handleSimpleValue(pclazz, (SimpleValue) value);
						} else if (value instanceof Collection) {
							handleCollection(pclazz, (Collection) value);
						}
					}
				}
			}
		});
	}

	/**
	 * 处理对象类型字段
	 */
	private void handleToOne(PersistentClass pclazz, ToOne value) {
		// 指定外键长度
		Iterator<Selectable> iterator = value.getColumnIterator();
		while (iterator.hasNext()) {
			Column colunm = (Column) iterator.next();
			if (colunm.getSqlType() == null) {
				colunm.setSqlType(fkSqlType);
				colunm.setLength(IDGenerator.LEN);
			}
		}
		// 关联到组织架构不创建外键
		if (SYSORGELEMENTSUMMARY.equals(value.getReferencedEntityName())) {
			disableForeignKey(value);
		}
	}

	/**
	 * 禁止生成外键
	 */
	private void disableForeignKey(ToOne value) {
		@SuppressWarnings("unchecked")
		Iterator<ForeignKey> iterator = value.getTable()
				.getForeignKeyIterator();
		while (iterator.hasNext()) {
			ForeignKey key = iterator.next();
			if (key.isCreationEnabled() && SYSORGELEMENTSUMMARY
					.equals(key.getReferencedEntityName())) {
				key.disableCreation();
			}
		}
	}

	/**
	 * 处理简单类型字段
	 */
	private void handleSimpleValue(PersistentClass pclazz, SimpleValue value) {
		if (DatabaseUtil.isOracle()) {
			handleSimpleValue4Oracle(value);
		} else if (DatabaseUtil.isMySQL()) {
			handleSimpleValue4MySQL(value);
		} else if (DatabaseUtil.isSQLServer()) {
			handleSimpleValue4SQLServer(value);
		}
		handleEnumType(pclazz, value);
	}

	/**
	 * 处理简单类型字段(Oracle)
	 */
	private void handleSimpleValue4Oracle(SimpleValue value) {
	}

	/**
	 * 处理简单类型字段(MySQL)
	 */
	private void handleSimpleValue4MySQL(SimpleValue value) {
	}

	/**
	 * 处理枚举类型的转换器
	 */
	private void handleEnumType(PersistentClass pclazz, SimpleValue value) {
		// 自动装载枚举转换
		if (!"org.hibernate.type.EnumType".equals(value.getTypeName())) {
			return;
		}
		if (value.getTypeParameters() == null) {
			return;
		}
		String returnedClass = value.getTypeParameters().getProperty(
				"org.hibernate.type.ParameterType.returnedClass");
		if (StringUtils.isBlank(returnedClass)) {
			return;
		}

		Class<?> enumClass = ReflectUtil.classForName(returnedClass);
		Class<?>[] inners = enumClass.getClasses();
		if (inners == null) {
			return;
		}
		for (Class<?> inner : inners) {
			if (!AttributeConverter.class.isAssignableFrom(inner)) {
				continue;
			}
			String propertyName = value.getTypeParameters().getProperty(
					"org.hibernate.type.ParameterType.propertyName");
			value.setTypeName(StringHelper.join(
					AttributeConverterTypeAdapter.NAME_PREFIX,
					inner.getName()));
			value.setTypeName(null);
			value.setTypeParameters(null);
			value.setTypeUsingReflection(pclazz.getClassName(), propertyName);
			return;
		}
	}

	/**
	 * 处理简单类型字段(SQLServer)
	 */
	private void handleSimpleValue4SQLServer(SimpleValue value) {
		String type = value.getType().getName();
		if (HBM_STRING.equals(type)) {
			Iterator<Selectable> iterator = value.getColumnIterator();
			while (iterator.hasNext()) {
				Column colunm = (Column) iterator.next();
				if (colunm.getSqlType() == null) {
					colunm.setSqlType(StringHelper.join("nvarchar(",
							colunm.getLength(), ")"));
				}
			}
		} else if (HBM_CLOB.equals(type)) {
			Iterator<Selectable> iterator = value.getColumnIterator();
			while (iterator.hasNext()) {
				Column colunm = (Column) iterator.next();
				if (colunm.getSqlType() == null) {
					colunm.setSqlType("nvarchar(MAX)");
				}
			}
		}
	}

	/**
	 * 处理数组
	 */
	private void handleCollection(PersistentClass pclazz, Collection value) {
		// key，连到主表
		Iterator<Selectable> iterator = value.getKey().getColumnIterator();
		while (iterator.hasNext()) {
			Column colunm = (Column) iterator.next();
			if (colunm.getSqlType() == null) {
				colunm.setSqlType(fkSqlType);
				colunm.setLength(IDGenerator.LEN);
			}
		}
		// element，值
		Value element = value.getElement();
		if (element instanceof ToOne) {
			handleToOne(pclazz, (ToOne) element);
		} else if (element instanceof SimpleValue) {
			handleSimpleValue(pclazz, (SimpleValue) element);
		}
	}
}
