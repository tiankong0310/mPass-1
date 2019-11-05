package com.ibyte.framework.discovery.api;

import com.ibyte.framework.discovery.dto.SystemStatusVO;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @Description: <Eureka Server API>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-31
 */
public interface IEurekaServerApi {

    /**
     * 获取Eureka状态
     * @return
     */
    @PostMapping("systemStatus")
    SystemStatusVO systemStatus();

}
