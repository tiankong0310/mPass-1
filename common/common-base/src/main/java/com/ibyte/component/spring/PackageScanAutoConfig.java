package com.ibyte.component.spring;

import com.ibyte.common.constant.NamingConstant;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: <基础包路径扫描>
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @Date: 2019-10-09 10:27
 */
@Configuration
@ComponentScan(basePackages = {NamingConstant.BASE_PACKAGE})
public class PackageScanAutoConfig {
}
