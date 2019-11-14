package com.ibyte.framework.discovery.dto;

import lombok.*;

/**
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @since 1.0.1
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayRoute {

    private String id;

    private String uri;

    @Builder.Default
    private Integer order = 1;

    private String predicates;

    private String filters;

    @Override
    public String toString() {
        return id + "=" + uri + "," + predicates;
    }
}