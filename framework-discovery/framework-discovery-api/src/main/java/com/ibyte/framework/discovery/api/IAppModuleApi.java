package com.ibyte.framework.discovery.api;

import com.ibyte.framework.discovery.dto.AppModule;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Description: <应用模块api>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-31
 */
public interface IAppModuleApi {

    /**
     * 同步应用模块
     * @param appModules
     */
    @PostMapping("syncAppModule")
    void syncAppModule(@RequestBody List<AppModule> appModules);
}
