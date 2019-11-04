package com.ibyte.common.core.validation;

import com.ibyte.common.exception.ParamsNotValidException;

/**
 * 自定义校验的参数
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @Date: 2019-10-14
 */
public interface Validatable {
	/**
	 * 执行校验
	 * 
	 * @param groups
	 * @throws ParamsNotValidException
	 */
	void validate(Class<?>... groups) throws ParamsNotValidException;
}
