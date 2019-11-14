package com.ibyte.framework.discovery.dto;

import lombok.*;

/**
 * 模块映射信息
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @since 1.0.1
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ModuleMappingInfo {

    /**
     * 模块对应的应用名
     */
    private String app;

    /**
     * 模块对应应用上下文
     */
    private String contextPath;

}
