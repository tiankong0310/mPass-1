package com.ibyte.component.jpa.contributor;

import com.ibyte.common.util.IDGenerator;
import com.ibyte.common.util.ReflectUtil;
import com.ibyte.common.util.StringHelper;
import com.ibyte.framework.meta.MetaConstant;
import com.ibyte.framework.meta.MetaProperty;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.AnnotationException;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.engine.OptimisticLockStyle;
import org.hibernate.mapping.*;
import org.hibernate.tuple.GenerationTiming;
import org.hibernate.tuple.ValueGeneration;
import org.hibernate.tuple.ValueGenerator;
import org.hibernate.type.descriptor.converter.AttributeConverterTypeAdapter;

import javax.persistence.AttributeConverter;
import java.util.Map.Entry;

import static com.ibyte.component.jpa.HibernateMetadataExtractor.HBMTYPES;

/**
 * Hibernate的属性构造解析
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 */
public class HibernatePropertyParser implements MetaConstant {
	private InFlightMetadataCollector metadataCollector;

	public HibernatePropertyParser(
			InFlightMetadataCollector metadataCollector) {
		this.metadataCollector = metadataCollector;
	}

	/**
	 * 将数据字典解析成Hibernate的字段，不支持级联，不支持一对多
	 */
	public void parse(MetaProperty property, PersistentClass pclazz) {
		// feature
		HibernatePropertyFeature feature = property
				.getFeature(HibernatePropertyFeature.class);
		if (feature == null) {
			feature = new HibernatePropertyFeature();
		}
		// value
		Value value;
		if (property.isCollection()) {
			value = buildCollectionValue(property, feature, pclazz);
		} else {
			value = buildElement(pclazz, property, feature, pclazz.getTable());
		}
		// property
		Property prop = buildProperty(property, feature, pclazz);
		prop.setValue(value);
		pclazz.addProperty(prop);
		// version
		if (feature.isVersion()) {
			handleVersion(prop, pclazz);
		}
	}

	/**
	 * 生成列表的Value
	 */
	private Collection buildCollectionValue(MetaProperty property,
			HibernatePropertyFeature feature, PersistentClass pclazz) {
		// table：中间表
		String tableName = StringUtils.replace(feature.getJoinTable(),
				"{table}", pclazz.getTable().getName());
		Table table = metadataCollector.addTable(pclazz.getTable().getSchema(),
				pclazz.getTable().getCatalog(), tableName, null, false);

		// 带indexColumn的映射成list，否则映射成 bag
		Collection coll;
		if (StringUtils.isBlank(feature.getIndexColumn())) {
			coll = new Bag(metadataCollector, pclazz);
		} else {
			List list = new List(metadataCollector, pclazz);
			coll = list;
			// index列
			SimpleValue index = buildSimpleValue(table, TYPE_INTEGER,
					feature.getIndexColumn(), 0);
			list.setIndex(index);
		}

		// 基础属性
		coll.setRole(StringHelper.join(pclazz.getClassName(), '.',
				property.getName()));
		coll.setCollectionTable(table);
		coll.setLazy(true);

		// 关联到主表
		String columnName = StringUtils.isBlank(feature.getKeyColumn())
				? "fd_source_id" : feature.getKeyColumn();
		SimpleValue key = buildSimpleValue(table, TYPE_STRING, columnName,
				IDGenerator.LEN);
		coll.setKey(key);
		key.createForeignKeyOfEntity(pclazz.getClassName());

		// 填写列表元素的值
		SimpleValue element = buildElement(pclazz, property, feature, table);
		coll.setElement(element);

		metadataCollector.addCollectionBinding(coll);
		return coll;
	}

	/**
	 * 生成SimpleValue或ManyToOne
	 */
	private SimpleValue buildElement(PersistentClass pclazz,
			MetaProperty property, HibernatePropertyFeature feature,
			Table table) {
		// 列名，若没有定义则按命名规范创建一个
		String columnName = feature.getColumn();
		if (StringUtils.isBlank(columnName)) {
			Database database = metadataCollector.getDatabase();
			Identifier identifier = Identifier.toIdentifier(property.getName());
			columnName = database.getPhysicalNamingStrategy()
					.toPhysicalColumnName(identifier,
							database.getJdbcEnvironment())
					.render(database.getDialect());
			if (MetaConstant.isAssociation(property.getType())) {
				columnName = StringHelper.join(columnName, "_id");
			}
		}
		// 根据是否关联表，决定创建ManyToOne或SimpleValue
		if (MetaConstant.isAssociation(property.getType())) {
			return buildManyToOne(property, table, columnName);
		} else {
			SimpleValue value = buildSimpleValue(table, pclazz, property,
					columnName);
			if (StringUtils.isNotEmpty(feature.getColumnDefinition())) {
				Column column = (Column) value.getColumnIterator().next();
				if (StringUtils.isEmpty(column.getSqlType())) {
					column.setSqlType(feature.getColumnDefinition());
				}
			}
			// 补填精度
			if (property.getPrecision() != 0) {
				Column column = (Column) value.getColumnIterator().next();
				column.setPrecision(property.getPrecision());
				column.setScale(property.getScale());
			}
			// 处理版本
			if (feature.isVersion()) {
				value.makeVersion();
				value.setNullValue("undefined");
			}
			return value;
		}
	}

	/**
	 * 构造SimpleValue
	 */
	private SimpleValue buildSimpleValue(Table table, PersistentClass pclazz,
			MetaProperty property, String columnName) {
		if (MetaConstant.isEnum(property)) {
			Class<?>[] inners = ReflectUtil
					.classForName(property.getEnumClass()).getClasses();
			if (inners != null) {
				for (Class<?> inner : inners) {
					if (!AttributeConverter.class.isAssignableFrom(inner)) {
						continue;
					}
					SimpleValue value = new SimpleValue(metadataCollector,
							table);
					value.setTypeName(StringHelper.join(
							AttributeConverterTypeAdapter.NAME_PREFIX,
							inner.getName()));
					value.setTypeUsingReflection(pclazz.getClassName(),
							property.getName());
					buildColumn(columnName, property.getLength(), value, table);
					return value;
				}
			}
		}
		return buildSimpleValue(table, property.getType(), columnName,
				property.getLength());
	}

	/**
	 * 构造SimpleValue
	 */
	private SimpleValue buildSimpleValue(Table table, String type,
			String columnName, int len) {
		SimpleValue value = new SimpleValue(metadataCollector, table);
		String typeName = null;
		for (Entry<String, String> entry : HBMTYPES.entrySet()) {
			if (entry.getValue().equals(type)) {
				typeName = entry.getKey();
				break;
			}
		}
		value.setTypeName(typeName == null ? type.toLowerCase() : typeName);
		buildColumn(columnName, len, value, table);
		return value;
	}

	/**
	 * 构造ManyToOne
	 */
	private ManyToOne buildManyToOne(MetaProperty property, Table table,
			String columnName) {
		ManyToOne value = new ManyToOne(metadataCollector, table);
		// value.setPropertyName(property.getName());
		value.setTypeName(property.getType());
		value.setReferencedEntityName(property.getType());
		value.setLazy(property.isLazy());
		buildColumn(columnName, IDGenerator.LEN, value, table);
		value.createForeignKey();
		return value;
	}

	/**
	 * 构造列
	 */
	private Column buildColumn(String name, int len, SimpleValue value,
			Table table) {
		Column column = new Column();
		column.setName(name);
		column.setLength(len);
		table.addColumn(column);
		value.addColumn(column);
		return column;
	}

	/**
	 * 构造Hibernate的Property
	 */
	private Property buildProperty(MetaProperty property,
			HibernatePropertyFeature feature,
			PersistentClass pclazz) {
		Property prop = new Property();
		prop.setName(property.getName());
		if (property.isDynamic()) {
			prop.setPropertyAccessorName(
					DynamicPropertyAccessStrategy.class.getName());
		} else {
			prop.setPropertyAccessorName(
					ExtendPropertyAccessStrategy.class.getName());
		}
		prop.setValueGenerationStrategy(NoValueGeneration.INSTANCE);
		prop.setLazy(property.isLazy());
		prop.setOptional(property.isNotNull());
		return prop;
	}

	/**
	 * 构造Hibernate的乐观锁
	 */
	private void handleVersion(Property prop, PersistentClass pclazz) {
		if (!(pclazz instanceof RootClass)) {
			throw new AnnotationException(
					"Unable to define/override @Version on a subclass: "
							+ pclazz.getEntityName());
		}
		RootClass root = (RootClass) pclazz;
		root.setVersion(prop);
		root.setDeclaredVersion(prop);
		root.setOptimisticLockStyle(OptimisticLockStyle.VERSION);
	}

	/**
	 * Value生成策略（不生成）
	 *
	 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
	 */
	private static class NoValueGeneration implements ValueGeneration {
		private static final long serialVersionUID = -1417962966428301009L;
		public static final NoValueGeneration INSTANCE = new NoValueGeneration();

		@Override
		public GenerationTiming getGenerationTiming() {
			return GenerationTiming.NEVER;
		}

		@Override
		public ValueGenerator<?> getValueGenerator() {
			return null;
		}

		@Override
		public boolean referenceColumnInSql() {
			return true;
		}

		@Override
		public String getDatabaseGeneratedReferencedColumnValue() {
			return null;
		}
	}
}
