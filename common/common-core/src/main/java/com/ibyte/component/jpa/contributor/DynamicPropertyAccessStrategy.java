package com.ibyte.component.jpa.contributor;

import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.property.access.spi.PropertyAccessStrategy;

/**
 * 动态属性访问策略
 *
 * @author li.Shangzhi
 */
@SuppressWarnings("rawtypes")
public class DynamicPropertyAccessStrategy implements PropertyAccessStrategy {
	@Override
	public PropertyAccess buildPropertyAccess(Class containerJavaType,
			String propertyName) {
		return new DynamicPropertyAccess(this, containerJavaType, propertyName);
	}
}
