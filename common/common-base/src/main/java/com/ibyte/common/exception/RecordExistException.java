package com.ibyte.common.exception;

/**
 * 记录已存在异常
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @Date: 2019-10-12
 */
public class RecordExistException extends KmssRuntimeException {
	private static final long serialVersionUID = 2551048102709157038L;

	public RecordExistException() {
		super("errors.recordExist");
	}
}
