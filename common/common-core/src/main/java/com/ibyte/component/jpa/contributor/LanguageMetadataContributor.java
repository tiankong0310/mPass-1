package com.ibyte.component.jpa.contributor;

import com.ibyte.common.util.LangUtil;
import com.ibyte.common.util.ReflectUtil;
import com.ibyte.framework.meta.MetaConstant;
import com.ibyte.framework.meta.MetaEntity;
import com.ibyte.framework.meta.MetaProperty;
import com.ibyte.framework.support.LocalMetaContextHolder;
import com.ibyte.framework.support.domain.MetaEntityImpl;
import com.ibyte.framework.support.domain.MetaPropertyImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataContributor;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.SimpleValue;
import org.jboss.jandex.IndexView;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

/**
 * 根据多语言特性动态生成多语言字段
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 */
@Slf4j
public class LanguageMetadataContributor implements MetadataContributor, MetaConstant {
    private HibernatePropertyParser parser;

    @Override
    public void contribute(InFlightMetadataCollector metadataCollector, IndexView jandexIndex) {
        // 判断是否开启多语言
        if (!LangUtil.isSuportEnabled()) {
            return;
        }
        // 获取多语言
        this.parser = new HibernatePropertyParser(metadataCollector);
        for (PersistentClass pclazz : metadataCollector.getEntityBindingMap().values()) {
            Class<?> clazz = ReflectUtil.classForName(pclazz.getClassName());
            if (clazz == null) {
                continue;
            }
            try {
                MetaEntity entity = LocalMetaContextHolder.get().getOrCreateEntity(pclazz.getClassName());
                Map<String, MetaProperty> properties = entity.getProperties();
                for (String name : properties.keySet()) {
                    MetaProperty property = properties.get(name);
                    if (property.isLangSupport() && !pclazz.isPropertyDefinedInSuperHierarchy(name)) {
                        multilingualProcess(pclazz, (MetaEntityImpl) entity, name);
                    }
                }
            } catch (Exception e) {
                log.debug("读取数据字典失败：" + pclazz.getClassName());
            }
        }
    }

    private void multilingualProcess(PersistentClass pclazz, MetaEntityImpl entity, String name) {
        Class<?> clazz = ReflectUtil.classForName(pclazz.getClassName());
        PropertyDescriptor[] descs = BeanUtils.getPropertyDescriptors(clazz);
        for (PropertyDescriptor desc : descs) {
            // 判断是否要做多语言
            if (name.equals(desc.getName())) {
                for (String lang : LangUtil.getSupportCountries()) {
                    String propertyName = name + lang;
                    MetaPropertyImpl langProp = new MetaPropertyImpl();
                    langProp.setShowType(ShowType.ALWAYS);
                    langProp.setDynamic(true);
                    langProp.setName(propertyName);
                    SimpleValue simpleValue = (SimpleValue) pclazz.getProperty(name).getValue();
                    if (simpleValue.isLob()) {
                        langProp.setType(MetaConstant.TYPE_RTF);
                    } else {
                        langProp.setType(MetaConstant.TYPE_STRING);
                    }
                    Column column = (Column) ((SimpleValue) pclazz.getProperty(name).getValue()).getConstraintColumns().get(0);
                    if (StringUtils.isNotEmpty(column.getSqlType())) {
                        HibernatePropertyFeature feature = new HibernatePropertyFeature();
                        feature.setColumnDefinition(column.getSqlType());
                        Map<String, Object> features = new HashMap<>(1);
                        features.put(HibernatePropertyFeature.class.getName(), feature);
                        langProp.setFeatures(features);
                    }
                    langProp.setLength(column.getLength());
                    parser.parse(langProp, pclazz);

                    // 增加的字段标记为临时属性，不需要生成数据字典
                    entity.addTempProperty(propertyName);
                }
            }
        }
    }

}
