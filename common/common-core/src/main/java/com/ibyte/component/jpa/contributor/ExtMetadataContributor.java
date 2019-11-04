package com.ibyte.component.jpa.contributor;

import com.fasterxml.jackson.databind.JavaType;
import com.ibyte.common.util.JsonUtil;
import com.ibyte.framework.meta.MetaConstant;
import com.ibyte.framework.meta.MetaProperty;
import com.ibyte.framework.support.ApplicationContextHolder;
import com.ibyte.framework.support.LocalMetaContextHolder;
import com.ibyte.framework.support.domain.MetaEntityImpl;
import com.ibyte.framework.support.domain.MetaPropertyImpl;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataContributor;
import org.hibernate.mapping.PersistentClass;
import org.jboss.jandex.IndexView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 根据扩展配置增强字段
 * 
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 */
public class ExtMetadataContributor
		implements MetadataContributor, MetaConstant {
	private static final String KEY_EXT_METADATA = "kmss.metadata.ext";

	@Override
	public void contribute(InFlightMetadataCollector metadataCollector,
			IndexView jandexIndex) {
		List<MetaEntityImpl> entities = requestExtEntities(
				metadataCollector);
		if (entities == null || entities.isEmpty()) {
			return;
		}
		HibernatePropertyParser parser = new HibernatePropertyParser(
				metadataCollector);
		for (MetaEntityImpl extEntity : entities) {
			PersistentClass pclazz = metadataCollector
					.getEntityBinding(extEntity.getEntityName());
			if (pclazz == null) {
				continue;
			}
			// 数据字典合并
			MetaEntityImpl entity = (MetaEntityImpl) LocalMetaContextHolder
					.get().getOrCreateEntity(extEntity.getEntityName());
			mergeFeature(extEntity.getFeatures(), entity.getFeatures());
			for (MetaProperty p : extEntity.getProperties().values()) {
				MetaPropertyImpl extProp = (MetaPropertyImpl) p;
				// 往hibernate追加属性
				if (!pclazz.hasProperty(extProp.getName())) {
					parser.parse(extProp, pclazz);
				}
				// 数据字典合并
				MetaPropertyImpl prop = (MetaPropertyImpl) entity
						.getProperty(extProp.getName());
				if (prop == null) {
					entity.getProperties().put(extProp.getName(), extProp);
				} else {
					mergeFeature(extProp.getFeatures(), prop.getFeatures());
				}
			}
		}
	}

	/** 合并Feature */
	private void mergeFeature(Map<String, Object> from,
			Map<String, Object> to) {
		for (Entry<String, Object> entry : from.entrySet()) {
			to.putIfAbsent(entry.getKey(), entry.getValue());
		}
	}

	/** 请求有扩展配置的Entity */
	private List<MetaEntityImpl>
			requestExtEntities(InFlightMetadataCollector metadataCollector) {
		String json = ApplicationContextHolder.getApplicationContext()
				.getEnvironment().getProperty(KEY_EXT_METADATA);
		if (StringUtils.isBlank(json)) {
			return new ArrayList<>();
		}
		JavaType type = JsonUtil.getMapper().getTypeFactory()
				.constructParametricType(ArrayList.class, MetaEntityImpl.class);
		return JsonUtil.parseObject(json, type);
	}
}
