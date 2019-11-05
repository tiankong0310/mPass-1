package com.ibyte.framework.discovery.dto;

import lombok.*;

/**
 * @Description: <系统配置前端对象>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-31
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigVo {
    /**
     * 主键,配置项key
     */
    private String fdKey;

    /**
     * 配置项value
     */
    private String fdValue;

}
