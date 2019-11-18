package com.ibyte.framework.discovery.core.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * 应用配置
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @since 1.0.1
 */
@Entity
@Table
@Getter
@Setter
public class ApplicationConfig {
    @Id
    @Column(length = 255)
    private String fdId;

    @Access(AccessType.PROPERTY)
    private Integer fdTenantId;

    @Lob
    private String fdContent;
}
