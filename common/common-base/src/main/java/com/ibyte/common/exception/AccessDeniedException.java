package com.ibyte.common.exception;

/**
 * 无访问权限
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @Date: 2019-10-12
 */
public class AccessDeniedException extends KmssRuntimeException {
	private static final long serialVersionUID = 6084486928183353506L;

	public AccessDeniedException() {
		super("global.accessDenied");
	}
}
