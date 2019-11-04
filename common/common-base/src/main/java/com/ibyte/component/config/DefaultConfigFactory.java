package com.ibyte.component.config;

import java.util.Map;

/**
 * @Description: <默认配置提供器>
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @Date: 2019-10-08 22:47
 */
public interface DefaultConfigFactory {

    /**
     * 默认配置信息
     *
     * @return
     */
    Map<String, Object> defaultConfig();

}
