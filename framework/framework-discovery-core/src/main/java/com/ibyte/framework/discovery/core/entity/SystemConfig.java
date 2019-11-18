package com.ibyte.framework.discovery.core.entity;

import com.ibyte.framework.discovery.constant.SystemConfigConstant;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 *  系统配置
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @since 1.0.1
 */
@Table
@Entity
@Getter
@Setter
public class SystemConfig implements SystemConfigConstant {

    /**
     * 主键信息,配置项key
     */
    @Id
    @Column(length = 200)
    private String fdId;

    /**
     * 配置项value
     */
    @Lob
    private String fdValue;

    /**
     * 应用名,默认：全局配置，global
     */
    @Column(length = 20)
    private String fdApplication = CONFIG_APPLICATION;

    /**
     * 应用使用配置，默认：default
     */
    @Column(length = 20)
    private String fdProfile = CONFIG_PROFILE;

    /**
     * 应用标签，默认：master
     */
    @Column(length = 20)
    private String fdLabel = CONFIG_LABEL;

}
