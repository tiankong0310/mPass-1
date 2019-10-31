package com.ibyte.framework.discovery.dto;

import lombok.*;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @Description: <eureka system status vo>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-31
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SystemStatusVO {

    private String applicationId;

    private Date currentTime;

    private String upTime;

    private String environment;

    private String datacenter;

    private boolean leaseExpirationEnabled;

    private int renewsPerMinThreshold;

    private long renewsInLastMin;

    private String warningMessage;

    private Set<Map.Entry<String, String>> replicas;
}
