package com.ibyte.framework.discovery.core.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * 元素设计
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @since 1.0.1
 */
@Entity
@Table
@Getter
@Setter
public class DesignElement {
    @Id
    @Column(length = 255)
    private String fdId;

    @Column(length = 200)
    private String fdLabel;

    @Column(length = 200)
    private String fdMessageKey;

    @Column(length = 100)
    private String fdAppName;

    @Column(length = 100)
    private String fdModule;

    @Column(length = 100)
    private String fdMd5;

    @Lob
    private String fdContent;
}
