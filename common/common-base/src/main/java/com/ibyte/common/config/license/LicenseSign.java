package com.ibyte.common.config.license;

import lombok.Getter;
import lombok.Setter;

/**
 * @Description: <授权前面信息>
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @Date: 2019-10-11
 */
@Setter
@Getter
public class LicenseSign {

    /**
     * 启用授权签名
     */
    private boolean enable;

    /**
     * 授权签名id
     */
    private String id;
}
