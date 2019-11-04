package com.ibyte.common.core.dto;

import lombok.ToString;

/**
 * ID对象，用于Controller/API的get方法，可指定加载哪些机制
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @Date: 2019-10-14
 */
@ToString(callSuper = true)
public class IdVO extends AbstractVO {
	public static IdVO of(String fdId) {
		IdVO vo = new IdVO();
		vo.setFdId(fdId);
		return vo;
	}
}
