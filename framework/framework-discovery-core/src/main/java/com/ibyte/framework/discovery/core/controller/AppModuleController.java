package com.ibyte.framework.discovery.core.controller;

import com.ibyte.framework.discovery.dto.AppModule;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 模块服务管理
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @since 1.0.1
 */
@RestController
@RequestMapping("/api/framework-discovery/frameworkAppModule")
public interface AppModuleController {

    /**
     * 同步应用模块映射关系
     *
     * @param appModules
     */
    @PostMapping("syncAppModule")
    void syncAppModule(@RequestBody List<AppModule> appModules);

}
