package com.ibyte.common.core.event;

import com.ibyte.common.core.entity.IEntity;

/**
 * Entity加载事件
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 */
public class EntityLoadEvent extends AbstractEntityEvent {
	private static final long serialVersionUID = -4373238742817189360L;

	public EntityLoadEvent(IEntity source) {
		super(source);
	}
}
