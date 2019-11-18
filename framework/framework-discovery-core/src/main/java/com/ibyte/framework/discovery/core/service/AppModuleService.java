package com.ibyte.framework.discovery.core.service;

import com.ibyte.common.constant.NamingConstant;
import com.ibyte.framework.discovery.ModuleMappingHelper;
import com.ibyte.framework.discovery.ModuleMappingLoader;
import com.ibyte.framework.discovery.api.IAppModuleApi;
import com.ibyte.framework.discovery.api.ISystemConfigApi;
import com.ibyte.framework.discovery.dto.AppModule;
import com.ibyte.framework.discovery.dto.GatewayRoute;
import com.ibyte.framework.discovery.dto.SystemConfigVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 应用模块实现处理
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @since 1.0.1
 */
@Slf4j
@Service
public class AppModuleService implements IAppModuleApi, ApplicationListener<ApplicationReadyEvent> {

    /**
     * redis模块信息存储key
     */
    private final String REDIS_KEY_APP_MODULES = "app-modules-cache-key";

    /**
     * redis
     */
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * redisson
     */
    @Autowired
    RedissonClient redissonClient;

    /**
     * 系统参数配置
     */
    @Autowired
    private ISystemConfigApi configService;

    /**
     * 模块映射服务
     */
    @Autowired
    private ModuleMappingHelper moduleMappingHelper;

    /**
     * 同步应用模块
     *
     * @param appModules
     */
    @Override
    public void syncAppModule(List<AppModule> appModules) {
        Map<String, String> routes = new HashMap<>();
        List<SystemConfigVo> configVoList = new ArrayList<>(1);
        // 清除应用模块信息
        configService.clear(ModuleMappingLoader.SVR_PREFIX);
        appModules.forEach(appModule -> {
            String app = appModule.getApp();
            List<String> modules = appModule.getModules();
            for (String module : modules){
                configVoList.add(new SystemConfigVo(ModuleMappingLoader.SVR_PREFIX + module + ".app", app));
                routes.put(module, GatewayRoute.builder().id(module).predicates("Path=/data/".concat(module).concat("/**")).uri("lb://".concat(app)).build().toString());
                routes.put(module, GatewayRoute.builder().id(module).predicates("Path=/api/".concat(module).concat("/**")).uri("lb://".concat(app)).build().toString());
            }
        });
        if (!CollectionUtils.isEmpty(configVoList)) {
            configService.saveAll(configVoList);
        }
        redisTemplate.opsForHash().putAll(REDIS_KEY_APP_MODULES, routes);
        redissonClient.getTopic(NamingConstant.TOPIC_APP_CONFIG_REFRESH).publish("config.refreshed");
        try {
            moduleMappingHelper.getMappingInfos().clear();
            moduleMappingHelper.afterPropertiesSet();
        } catch (Exception e) {
            log.error("注册中心模块映射关系刷新失败", e);
        }
        log.info("配置刷新完毕");

    }

    /**
     * 服务器启动完，调用业务刷新处理
     *
     * @param event
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        redissonClient.getTopic(NamingConstant.TOPIC_APP_CONFIG_REFRESH).publish("config.refreshed");
    }
}
