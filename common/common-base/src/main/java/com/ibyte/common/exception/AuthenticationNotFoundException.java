package com.ibyte.common.exception;

/**
 * 无认证信息异常
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @Date: 2019-10-12
 */
public class AuthenticationNotFoundException extends KmssRuntimeException {

    private static final long serialVersionUID = -1056037658177543184L;

    public AuthenticationNotFoundException() {
        super("status.401");
    }
}
