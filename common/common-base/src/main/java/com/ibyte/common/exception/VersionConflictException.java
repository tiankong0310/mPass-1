package com.ibyte.common.exception;

/**
 * 版本冲突异常
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @Date: 2019-10-12
 */
public class VersionConflictException extends KmssRuntimeException {
	/**  */
	private static final long serialVersionUID = -9147092293425702423L;

	public VersionConflictException() {
		super("errors.versionConflict");
	}
}
