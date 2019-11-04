package com.ibyte.common.core.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 通用展现对象：fdId
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @Date: 2019-10-14
 */
@Getter
@Setter
@ToString
public class IdProperty {
	private String fdId;

	public static IdProperty of(String fdId) {
		IdProperty result = new IdProperty();
		result.setFdId(fdId);
		return result;
	}
}
