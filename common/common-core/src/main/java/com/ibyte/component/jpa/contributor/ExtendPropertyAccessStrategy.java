package com.ibyte.component.jpa.contributor;

import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.property.access.spi.PropertyAccessStrategy;

/**
 * 扩展属性访问策略
 * 
 * @author Li.shangzhi
 */
@SuppressWarnings("rawtypes")
public class ExtendPropertyAccessStrategy implements PropertyAccessStrategy {
	@Override
	public PropertyAccess buildPropertyAccess(Class containerJavaType,
			String propertyName) {
		return new ExtendPropertyAccess(this, containerJavaType, propertyName);
	}
}
