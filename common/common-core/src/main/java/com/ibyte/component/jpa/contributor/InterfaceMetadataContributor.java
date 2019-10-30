package com.ibyte.component.jpa.contributor;

import com.ibyte.common.core.constant.IEnum;
import com.ibyte.common.core.data.field.IField;
import com.ibyte.common.core.entity.IEntity;
import com.ibyte.common.i18n.ResourceUtil;
import com.ibyte.common.util.ReflectUtil;
import com.ibyte.common.util.StringHelper;
import com.ibyte.framework.meta.EnumItem;
import com.ibyte.framework.meta.MetaConstant;
import com.ibyte.framework.support.domain.MetaPropertyImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.NamingHelper;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataContributor;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.validator.constraints.Length;
import org.jboss.jandex.IndexView;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 根据IField接口增强字段
 *
 * @author ibyte
 */
@Slf4j
public class InterfaceMetadataContributor
		implements MetadataContributor, MetaConstant {
	@Override
	public void contribute(InFlightMetadataCollector metadataCollector,
			IndexView jandexIndex) {
		HibernatePropertyParser parser = new HibernatePropertyParser(
				metadataCollector);
		List<String> handled = new ArrayList<>();
		for (PersistentClass pclazz : metadataCollector.getEntityBindingMap()
				.values()) {
			readInterface(parser, pclazz, handled, metadataCollector);
		}
	}

	/** 读取接口信息 */
	private void readInterface(HibernatePropertyParser parser,
			PersistentClass pclazz, List<String> handled,
			InFlightMetadataCollector metadataCollector) {
		// 判断是否已经处理过
		if (handled.contains(pclazz.getClassName())) {
			return;
		}
		// 判断是否存在IField接口
		Class<?> clazz = ReflectUtil.classForName(pclazz.getClassName());
		if (clazz == null || !IField.class.isAssignableFrom(clazz)) {
			return;
		}
		// 先处理父类
		if (pclazz.getSuperclass() != null) {
			readInterface(parser, pclazz.getSuperclass(), handled,
					metadataCollector);
		}
		handled.add(pclazz.getClassName());
		PropertyDescriptor[] descs = BeanUtils
				.getPropertyDescriptors(clazz);
		List<Class<?>> ifaces = new ArrayList<>();
		for (PropertyDescriptor desc : descs) {
			if (isNewProperty(pclazz, desc)) {
				parser.parse(buildProperty(clazz, pclazz, desc), pclazz);
				// 把字段接口放入一个列表，最后处理索引
				Class<?> iface = desc.getReadMethod().getDeclaringClass();
				if (!ifaces.contains(iface)) {
					ifaces.add(iface);
				}
			}
		}
		// 使用Length注解时，没有将max长度写到hibernate字段映射中，这里手动处理，否则后面的多语言字段无法获取length长度值
		try {
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				if (field.isAnnotationPresent(Length.class)) {
					Length length = field.getAnnotation(Length.class);
					Iterator<org.hibernate.mapping.Column> iterator = pclazz.getTable().getColumnIterator();
					while (iterator.hasNext()) {
						org.hibernate.mapping.Column column = iterator.next();
						if (column.getName().equals(camel2Underline(field.getName()))) {
							column.setLength(length.max());
							break;
						}
					}
				}
			}
		} catch (Exception e) {
		}
		for (Class<?> iface : ifaces) {
			readIndex(pclazz, iface);
		}
	}

	/** 是否需要新增的属性 */
	private boolean isNewProperty(PersistentClass pclazz,
			PropertyDescriptor desc) {
		for (PersistentClass cl = pclazz; cl != null; cl = cl.getSuperclass()) {
			if (cl.hasProperty(desc.getName())) {
				return false;
			}
		}
		Method read = desc.getReadMethod();
		Method write = desc.getWriteMethod();
		if (read == null || write == null) {
			return false;
		}
		if (!read.getDeclaringClass().isInterface()) {
			return false;
		}
		if (read.isAnnotationPresent(Transient.class)) {
			return false;
		}
		return true;
	}

	/** 读取索引配置 */
	private void readIndex(PersistentClass pclazz, Class<?> iface) {
		Table table = iface.getAnnotation(Table.class);
		if (table == null) {
			return;
		}
		org.hibernate.mapping.Table tb = pclazz.getTable();
		outloop: for (Index index : table.indexes()) {
			List<Identifier> columnNames = new ArrayList<>();
			org.hibernate.mapping.Index idx = new org.hibernate.mapping.Index();
			// columnList="fdId, fdName"
			String[] columns = index.columnList().split(",");
			for (String column : columns) {
				column = column.trim();
				int i = column.indexOf(' ');
				String order = null;
				if (i > -1) {
					order = column.substring(i).trim();
					column = column.substring(0, i);
				}
				Property property = pclazz.getProperty(column);
				org.hibernate.mapping.Column col = (org.hibernate.mapping.Column) property
						.getColumnIterator().next();
				if (col == null) {
					log.error(StringHelper.join(iface.getName(), "指定的索引列不存在：",
							column));
					continue outloop;
				}
				columnNames.add(Identifier.toIdentifier(column));
				idx.addColumn(col, order);
			}
			idx.setTable(tb);
			// 创建索引名称
			String name = index.name();
			if (StringUtils.isBlank(name)) {
				name = NamingHelper.INSTANCE.generateHashedConstraintName("IDX",
						idx.getTable().getNameIdentifier(), columnNames);
			}
			idx.setName(name);
			// 判断索引是否存在
			if (tb.getIndex(name) == null) {
				tb.addIndex(idx);
			}
		}
	}

	/** 构造数据字典属性 */
	private MetaPropertyImpl buildProperty(Class<?> entityClass,
			PersistentClass pclazz, PropertyDescriptor desc) {
		MetaPropertyImpl property = new MetaPropertyImpl();
		// name/type
		property.setName(desc.getName());
		property.setType(getType(entityClass, property, desc));

		// feature
		Map<String, Object> features = new HashMap<>(1);
		HibernatePropertyFeature feature = new HibernatePropertyFeature();
		features.put(HibernatePropertyFeature.class.getName(), feature);
		property.setFeatures(features);

		// 解析相应的标签
		Method read = desc.getReadMethod();
		if (read.isAnnotationPresent(Length.class)) {
			parseLength(property, feature, read.getAnnotation(Length.class));
		}
		if (read.isAnnotationPresent(Column.class)) {
			parseColumn(property, feature, read.getAnnotation(Column.class));
		}
		if (read.isAnnotationPresent(OrderColumn.class)) {
			feature.setIndexColumn(
					read.getAnnotation(OrderColumn.class).name());
		}
		if (read.isAnnotationPresent(Basic.class)) {
			parseBasic(property, feature, read.getAnnotation(Basic.class));
		}
		if (read.isAnnotationPresent(Version.class)) {
			feature.setVersion(true);
		}
		if (read.isAnnotationPresent(ManyToOne.class)) {
			parseManyToOne(property, feature,
					read.getAnnotation(ManyToOne.class));
		}
		if (read.isAnnotationPresent(JoinColumn.class)) {
			parseColumn(property, feature,
					read.getAnnotation(JoinColumn.class));
		}
		if (read.isAnnotationPresent(JoinTable.class)) {
			parseJoinTable(property, feature,
					read.getAnnotation(JoinTable.class));
		}
		return property;
	}

	/** 解析Length标签 */
	private void parseLength(MetaPropertyImpl property,
			HibernatePropertyFeature feature, Length length) {
		if (length.max() > 0) {
			property.setLength(length.max());
		}
	}

	/** 解析Column标签 */
	private void parseColumn(MetaPropertyImpl property,
			HibernatePropertyFeature feature, Column column) {
		property.setLength(column.length());
		feature.setColumn(column.name());
		if (StringUtils.isNotEmpty(column.columnDefinition())) {
			feature.setColumnDefinition(column.columnDefinition());
		}
		if (!column.nullable()) {
			property.setNotNull(true);
		}
		property.setPrecision(column.precision());
		property.setScale(column.scale());
		if (!column.updatable()) {
			property.setReadOnly(true);
		}
	}

	/** 解析JoinColumn标签 */
	private void parseColumn(MetaPropertyImpl property,
			HibernatePropertyFeature feature, JoinColumn column) {
		feature.setColumn(column.name());
		if (!column.nullable()) {
			property.setNotNull(true);
		}
		if (!column.updatable()) {
			property.setReadOnly(true);
		}
	}

	/** 解析Basic标签 */
	private void parseBasic(MetaPropertyImpl property,
			HibernatePropertyFeature feature, Basic basic) {
		if (FetchType.LAZY == basic.fetch()) {
			property.setLazy(true);
		}
		if (!basic.optional()) {
			property.setNotNull(true);
		}
	}

	/** 解析ManyToOne标签，不支持级联 */
	private void parseManyToOne(MetaPropertyImpl property,
			HibernatePropertyFeature feature, ManyToOne manyToOne) {
		if (FetchType.LAZY == manyToOne.fetch()) {
			property.setLazy(true);
		}
		if (!manyToOne.optional()) {
			property.setNotNull(true);
		}
	}

	/** 解析JoinTable标签 */
	private void parseJoinTable(MetaPropertyImpl property,
			HibernatePropertyFeature feature, JoinTable join) {
		feature.setJoinTable(join.name());
		if (join.joinColumns().length > 0) {
			feature.setKeyColumn(join.joinColumns()[0].name());
		}
		if (join.inverseJoinColumns().length > 0) {
			feature.setColumn(join.inverseJoinColumns()[0].name());
		}
	}

	/** 读取字段类型 */
	private String getType(Class<?> entityClass, MetaPropertyImpl property,
			PropertyDescriptor desc) {
		Method read = desc.getReadMethod();
		Type type = read.getGenericReturnType();
		if (type instanceof ParameterizedType) {
			// 泛型：List<?>
			ParameterizedType pType = (ParameterizedType) type;
			if (Collection.class
					.isAssignableFrom((Class<?>) pType.getRawType())) {
				property.setCollection(true);
				type = pType.getActualTypeArguments()[0];
				if (type instanceof Class) {
					return getType((Class<?>) type, property, read);
				} else if (type instanceof TypeVariable) {
					Class<?> clazz = ReflectUtil.getActualClass(entityClass,
							read.getDeclaringClass(),
							((TypeVariable<?>) type).getName());
					return getType(clazz, property, read);
				}
			}
			throw new RuntimeException("不支持的泛型类型：" + property.getName());
		} else if (type instanceof TypeVariable) {
			// 泛型变量，用于多对一
			Class<?> clazz = ReflectUtil.getActualClass(entityClass,
					read.getDeclaringClass(),
					((TypeVariable<?>) type).getName());
			return getType(clazz, property, read);
		} else {
			return getType((Class<?>) type, property, read);
		}
	}

	/** 读取字段类型 */
	private String getType(Class<?> clazz, MetaPropertyImpl property,
			Method read) {
		// 非泛型
		if (IEntity.class.isAssignableFrom(clazz)) {
			return clazz.getName();
		} else if (IEnum.class.isAssignableFrom(clazz)) {
			// 枚举
			fillPropEnumList(property, clazz);
			return ReflectUtil.getActualClass(clazz, IEnum.class, "V")
					.getSimpleName();
		} else if (Date.class.isAssignableFrom(clazz)) {
			// 日期
			if (read.isAnnotationPresent(Temporal.class)) {
				TemporalType tType = read.getAnnotation(Temporal.class)
						.value();
				return tType == TemporalType.DATE ? TYPE_DATE
						: (tType == TemporalType.TIME ? TYPE_TIME
								: TYPE_DATETIME);
			} else {
				return TYPE_DATETIME;
			}
		} else if (read.isAnnotationPresent(Lob.class)) {
			// 大字段
			return clazz == String.class ? TYPE_RTF : TYPE_BLOB;
		} else {
			return clazz.getSimpleName();
		}
	}

	/**
	 * 填充枚举信息
	 */
	private void fillPropEnumList(MetaPropertyImpl prop,
			Class<?> enumClass) {
		prop.setEnumClass(enumClass.getName());
		List<EnumItem> items = new ArrayList<>();
		for (Object o : enumClass.getEnumConstants()) {
			IEnum<?> en = (IEnum<?>) o;
			EnumItem item = new EnumItem();
			item.setLabel(ResourceUtil.getString(en.getMessageKey()));
			item.setMessageKey(en.getMessageKey());
			item.setValue(String.valueOf(en.getValue()));
			items.add(item);
		}
		prop.setEnumList(items);
	}

	/**
	 * 驼峰法转下划线正则
	 */
	private static Pattern camel2UnderlinePattern = Pattern.compile("[A-Z]([a-z\\d]+)?");

	/**
	 * 驼峰法转下划线
	 *
	 * @param line 源字符串
	 * @return 转换后的字符串
	 */
	public static String camel2Underline(String line) {
		if (line == null || "".equals(line)) {
			return "";
		}
		line = String.valueOf(line.charAt(0)).toUpperCase().concat(line.substring(1));
		StringBuffer sb = new StringBuffer();
		Matcher matcher = camel2UnderlinePattern.matcher(line);
		while (matcher.find()) {
			String word = matcher.group();
			sb.append(word.toLowerCase());
			sb.append(matcher.end() == line.length() ? "" : "_");
		}
		return sb.toString();
	}
}
