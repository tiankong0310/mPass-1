package com.ibyte.common.core.event;

import com.ibyte.common.core.entity.IEntity;

/**
 * Entity删除事件
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * 
 */
public class EntityDeleteEvent extends AbstractEntityEvent {
	private static final long serialVersionUID = 3286242784061272924L;

	public EntityDeleteEvent(IEntity source) {
		super(source);
	}
}
