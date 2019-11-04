package com.ibyte.component.jpa;

import com.ibyte.common.core.constant.IEnum;
import com.ibyte.common.i18n.ResourceUtil;
import com.ibyte.common.util.ReflectUtil;
import com.ibyte.common.util.StringHelper;
import com.ibyte.component.jpa.contributor.DynamicPropertyAccessStrategy;
import com.ibyte.framework.meta.EnumItem;
import com.ibyte.framework.meta.MetaConstant;
import com.ibyte.framework.support.LocalMetaContextHolder;
import com.ibyte.framework.support.domain.MetaEntityImpl;
import com.ibyte.framework.support.domain.MetaPropertyImpl;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.boot.spi.SessionFactoryBuilderFactory;
import org.hibernate.boot.spi.SessionFactoryBuilderImplementor;
import org.hibernate.mapping.*;
import org.hibernate.type.Type;
import org.springframework.beans.BeanUtils;

import java.util.*;
import java.util.List;
import java.util.Map;

/**
 * @Description: <从hibernate中抽取数据字典的信息>
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @Date: 2019-10-29
 */
@Slf4j
public class HibernateMetadataExtractor
        implements SessionFactoryBuilderFactory, MetaConstant {
    public static Map<String, String> HBMTYPES = new HashMap<>();

    static {
        HBMTYPES.put("materialized_clob", "RTF");
        HBMTYPES.put("materialized_blob", "BLOB");
        HBMTYPES.put("timestamp", "DateTime");
        HBMTYPES.put("big_decimal", "BigDecimal");
    }

    @Override
    public SessionFactoryBuilder getSessionFactoryBuilder(
            MetadataImplementor metadata,
            SessionFactoryBuilderImplementor defaultBuilder) {
        List<String> handled = new ArrayList<>();
        for (PersistentClass entity : metadata.getEntityBindings()) {
            bind(entity, handled);
        }
        return null;
    }

    private static final char DOT = '.';

    /**
     * 根据Hbm的配置构造数据字典
     */
    private void bind(PersistentClass persister, List<String> handled) {
        // 优先处理父类
        if (handled.contains(persister.getClassName())) {
            return;
        }
        if (persister.getSuperclass() != null) {
            bind(persister.getSuperclass(), handled);
        }
        handled.add(persister.getClassName());

        MetaEntityImpl entity = (MetaEntityImpl) LocalMetaContextHolder.get()
                .getOrCreateEntity(persister.getEntityName());
        Class<?> clazz = ReflectUtil.classForName(entity.getEntityName());
        // id
        Property id = persister.getIdentifierProperty();
        if (id != null) {
            try {
                fillProperty(clazz, entity, id);
            } catch (Exception e) {
                log.error("无法解释Entity字段类型：" + persister.getEntityName()
                        + " - " + id.getName(), e);
            }
        }
        // 启动属性
        for (PersistentClass pclazz = persister; pclazz != null; pclazz = pclazz
                .getSuperclass()) {
            bind(pclazz, entity, clazz);
        }
    }

    @SuppressWarnings("unchecked")
    private void bind(PersistentClass persister, MetaEntityImpl entity,
                      Class<?> clazz) {
        Iterator<Property> attrs = persister.getPropertyIterator();
        // 其他属性
        while (attrs.hasNext()) {
            Property attr = attrs.next();
            try {
                if (entity.getTempProperties() != null && entity
                        .getTempProperties().contains(attr.getName())) {
                    continue;
                }
                fillProperty(clazz, entity, attr);
            } catch (Exception e) {
                log.error("无法解释Entity字段类型：" + persister.getEntityName()
                        + " - " + attr.getName(), e);
            }
        }
        // version
        Property version = persister.getVersion();
        if (version != null) {
            entity.setVersionProperty(version.getName());
        }
    }

    /**
     * 根据Hbm的配置构造数据字典字段
     */
    private void fillProperty(Class<?> clazz, MetaEntityImpl entity,
                              Property attr) throws Exception {
        // 读取或创建字段
        MetaPropertyImpl prop = (MetaPropertyImpl) entity
                .getProperty(attr.getName());
        if (prop == null) {
            prop = new MetaPropertyImpl();
            prop.setName(attr.getName());
            entity.getProperties().put(attr.getName(), prop);
        }

        Type type;
        Value value = attr.getValue();
        if (value instanceof org.hibernate.mapping.Collection) {
            prop.setCollection(true);
            prop.setMappedBy(((org.hibernate.mapping.Collection) value).getMappedByProperty());
            type = ((org.hibernate.mapping.Collection) value).getElement().getType();
        } else {
            type = value.getType();
        }
        Class<?> javaType = type.getReturnedClass();
        if (javaType != null && IEnum.class.isAssignableFrom(javaType)) {
            // 枚举信息
            fillPropEnumList(prop, javaType);
            Class<?> valueClass = ReflectUtil.getActualClass(javaType,
                    IEnum.class, "V");
            prop.setType(valueClass.getSimpleName());
        } else {
            prop.setType(formatHbmType(type));
        }
        // 级联、延迟加载、非空
        prop.setCascade(attr.getCascade());
        if (value instanceof Fetchable) {
            prop.setLazy(((Fetchable) value).isLazy());
        } else {
            prop.setLazy(attr.isLazy());
        }
        prop.setNotNull(prop.isNotNull() || !value.isNullable());
        // 长度精度
        Iterator<Selectable> columns = value.getColumnIterator();
        if (columns.hasNext()) {
            Selectable selectable = columns.next();
            if (selectable instanceof Column) {
                Column column = (Column) selectable;
                if (TYPE_STRING.equals(prop.getType())) {
                    prop.setLength(column.getLength());
                } else if (MetaConstant.isNumber(prop.getType())) {
                    prop.setPrecision(column.getPrecision());
                    prop.setScale(column.getScale());
                }
            }
        }
        // 处理动态
        if (DynamicPropertyAccessStrategy.class.getName()
                .equals(attr.getPropertyAccessorName())
                && BeanUtils.getPropertyDescriptor(clazz,
                prop.getName()) == null) {
            prop.setDynamic(true);
        }
    }

    /**
     * 转换HBM的类型
     */
    private String formatHbmType(Type type) {
        String typeName = type.getName();
        if (typeName.indexOf(DOT) > -1) {
            return typeName;
        }
        String metaType = HBMTYPES.get(typeName);
        if (metaType != null) {
            return metaType;
        }
        return StringHelper.join(Character.toUpperCase(typeName.charAt(0)),
                typeName.substring(1));
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
}
