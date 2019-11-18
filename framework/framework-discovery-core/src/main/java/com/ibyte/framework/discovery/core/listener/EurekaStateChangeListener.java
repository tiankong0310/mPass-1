package com.ibyte.framework.discovery.core.listener;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.eureka.EurekaServerContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.eureka.server.event.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 注册中心状态监听
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @since 1.0.1
 */
@Slf4j
@Component
public class EurekaStateChangeListener {

    @Resource
    private ApplicationInfoManager applicationInfoManager;

    /**
     * Eureka Registry启动事件
     * @param event
     */
    @EventListener
    public void listen(EurekaRegistryAvailableEvent event) {
        log.info("Eureka Registry在[{}]启动成功", event.getTimestamp());
    }

    /**
     * Eureka Server启动事件
     * @param event
     */
    @EventListener
    public void listen(EurekaServerStartedEvent event) {
        log.info("Eureka Server在[{}]启动成功", event.getTimestamp());
    }

    /**
     * 服务实例下线事件
     * @param event
     */
    @EventListener
    public void listen(EurekaInstanceCanceledEvent event) {
        log.info("服务实例[{}]下线", event.getAppName());
    }

    /**
     * 服务实例注册事件
     * @param event
     */
    @EventListener
    public void listen(EurekaInstanceRegisteredEvent event) {
        /**
         * 获取当前已注册的所有服务器实例
         */
        int total = EurekaServerContextHolder.getInstance().getServerContext().getRegistry().getSortedApplications().size();
//        event.getInstanceInfo().getActionType()
        log.info("服务实例[{}]注册成功,当前服务器已注册服务实例数量[{}]", event.getInstanceInfo().getAppName(), total);

    }

    /**
     * 服务实例续约事件
     * @param event
     */
    @EventListener
    public void listen(EurekaInstanceRenewedEvent event) {
//    	log.info("服务实例[{}]续约成功", event.getInstanceInfo().getAppName());
    }
}