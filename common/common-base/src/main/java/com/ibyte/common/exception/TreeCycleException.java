package com.ibyte.common.exception;

/**
 * 树产生循环嵌套异常
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @Date: 2019-10-12
 */
public class TreeCycleException extends KmssRuntimeException {
    private static final long serialVersionUID = 6031483304077608411L;

    public TreeCycleException() {
        super("errors.treeCycle");
    }
}
