package com.ibyte.framework.discovery.core.license;

import com.ibyte.component.config.InitPropConfigFactory;

/**
 * 授权配置加载
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @since 1.0.1
 */
public class LicensePropConfigFactory extends InitPropConfigFactory {
    /**
     * 授权文件
     */
    private String LICENSE_FILE_PATH = "config/app-config/license.properties";

    @Override
    protected String getPropFilePath() {
        return LICENSE_FILE_PATH;
    }
}
