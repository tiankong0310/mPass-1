package com.ibyte.framework.discovery;

import com.ibyte.framework.discovery.client.interceptor.DiscoveryHeaderClientFilter;
import com.ibyte.framework.discovery.listener.EurekaClientEventListener;
import com.netflix.discovery.DiscoveryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * 服务注册中心配置
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @since 1.0.1
 */
@Configuration
@EnableConfigurationProperties(ModuleMappingHelper.class)
public class DiscoveryConfig {
    @Autowired
    Environment environment;

    /**
     * DiscoveryHeaderHelper默认bean
     * @return
     */
    @Bean
    public DiscoveryHeaderHelper discoveryHeaderHelper() {
        DiscoveryHeaderHelper discoveryHeaderHelper = new DiscoveryHeaderHelper(environment);
        DiscoveryHeaderHelper.INSTANCE = discoveryHeaderHelper;
        return discoveryHeaderHelper;
    }

    /**
     * resttemplate构建
     */
    @Resource
    private RestTemplateBuilder restTemplateBuilder;

    /**
     * resttemplate请求bean,更改系统本身的builder
     * @return
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = restTemplateBuilder.configure(new RestTemplate());
        //RestTemplate interceptors 远程调用请求增加头部信息处理
        restTemplate.getInterceptors().add(new RestApiHeaderInterceptor());
        //RestTemplate Set the error handler 错误处理
        restTemplate.setErrorHandler(new RestResponseErrorHandler());
        return  restTemplate;
    }

    @Bean
    public DiscoveryClient.DiscoveryClientOptionalArgs discoveryClientOptionalArgs() {
        DiscoveryClient.DiscoveryClientOptionalArgs discoveryClientOptionalArgs = new DiscoveryClient.DiscoveryClientOptionalArgs();
        discoveryClientOptionalArgs.setAdditionalFilters(Collections.singletonList(new DiscoveryHeaderClientFilter()));
        discoveryClientOptionalArgs.setEventListeners(Collections.singleton(new EurekaClientEventListener()));
        return discoveryClientOptionalArgs;
    }
}