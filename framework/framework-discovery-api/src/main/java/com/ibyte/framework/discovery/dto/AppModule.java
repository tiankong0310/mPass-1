package com.ibyte.framework.discovery.dto;

import lombok.*;

import java.util.List;

/**
 * @Description: <模块映射信息>
 *
 * @Date: 2019-10-31
 * @author li.Shangzhi
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class AppModule {

    /**
     * 模块对应的应用名
     */
    private String app;

    /**
     * 模块对应应用上下文
     */
    private List<String> modules;

}
