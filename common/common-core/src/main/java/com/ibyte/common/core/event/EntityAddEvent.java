package com.ibyte.common.core.event;

import com.ibyte.common.core.entity.IEntity;

/**
 * Entity添加事件
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 */
public class EntityAddEvent extends AbstractEntityEvent {
	private static final long serialVersionUID = 5222265806945558604L;

	public EntityAddEvent(IEntity source) {
		super(source);
	}
}
