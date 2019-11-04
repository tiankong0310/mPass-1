package com.ibyte.component.jpa.contributor;

import com.ibyte.common.core.entity.IEntity;
import com.ibyte.common.core.util.EntityUtil;
import com.ibyte.framework.meta.MetaConstant;
import com.ibyte.framework.meta.MetaEntity;
import com.ibyte.framework.meta.MetaProperty;
import lombok.AllArgsConstructor;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.property.access.spi.PropertyAccessStrategy;
import org.hibernate.property.access.spi.Setter;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 动态属性访问
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 */
@SuppressWarnings("rawtypes")
public class DynamicPropertyAccess implements PropertyAccess, MetaConstant {
	private PropertyAccessStrategy strategy;

	private Getter getter;

	private Setter setter;

	public DynamicPropertyAccess(PropertyAccessStrategy strategy,
			Class containerJavaType, String propertyName) {
		this.strategy = strategy;
		PropertyDescriptor desc = BeanUtils
				.getPropertyDescriptor(containerJavaType, propertyName);
		Class<?> returnType = null;
		MetaEntity entity = MetaEntity.localEntity(containerJavaType.getName());
		MetaProperty property = entity == null ? null
				: entity.getProperty(propertyName);
		if (property != null) {
			returnType = EntityUtil.getPropertyType(property.getType());
		}
		if (returnType == null) {
			returnType = Object.class;
		}
		this.getter = new DynamicPropertyGetterSetter(propertyName, returnType,
				desc == null ? null : desc.getReadMethod());
		this.setter = new DynamicPropertyGetterSetter(propertyName, returnType,
				desc == null ? null : desc.getWriteMethod());
	}

	@Override
	public PropertyAccessStrategy getPropertyAccessStrategy() {
		return strategy;
	}

	@Override
	public Getter getGetter() {
		return getter;
	}

	@Override
	public Setter getSetter() {
		return setter;
	}

	@AllArgsConstructor
	public static class DynamicPropertyGetterSetter implements Getter, Setter {
		private static final long serialVersionUID = 6035259930110797399L;

		private String propertyName;

		private Class<?> returnType;

		private Method method;

		@Override
		public void set(Object target, Object value,
				SessionFactoryImplementor factory) {
			getDynamicProps(target).put(propertyName, value);
		}

		@Override
		public Object get(Object owner) {
			return getDynamicProps(owner).get(propertyName);
		}

		private Map<String, Object> getDynamicProps(Object owner) {
			IEntity entity = (IEntity) owner;
			Map<String, Object> map = entity.getDynamicProps();
			if (map == null) {
				map = new HashMap<String, Object>(16);
				entity.setDynamicProps(map);
			}
			return map;
		}

		@Override
		public Object getForInsert(Object owner, Map mergeMap,
				SharedSessionContractImplementor session) {
			return get(owner);
		}

		@Override
		public Class getReturnType() {
			return returnType;
		}

		@Override
		public Member getMember() {
			return method;
		}

		@Override
		public String getMethodName() {
			return method == null ? null : method.getName();
		}

		@Override
		public Method getMethod() {
			return method;
		}
	}
}
