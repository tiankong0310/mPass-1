package com.ibyte.framework.discovery.listener;

import com.netflix.discovery.EurekaEvent;
import com.netflix.discovery.EurekaEventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @Description: <EurekaClientEventListener>
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @since 1.0.1
 */
@Slf4j
@Component
public class EurekaClientEventListener implements EurekaEventListener {

    @EventListener
    public void listen(InstanceRegisteredEvent<EurekaInstanceConfigBean> event) {
        log.info("实例[{}]注册事件", event.getConfig().getAppname());
    }

    @EventListener
    public void listen(RefreshScopeRefreshedEvent event) {
        log.info("实例[{}]注册事件", event.getName());
    }

    @Override
    public void onEvent(EurekaEvent event) {
//		if(event instanceof CacheRefreshedEvent) {
//			log.info("触发刷新缓存事件[{}]", ((CacheRefreshedEvent) event).getTimestamp());
//		}else if(event instanceof StatusChangeEvent) {
//			log.info("触发状态变更事件,之前状态[{}],当前状态[{}]", ((StatusChangeEvent) event).getPreviousStatus(), ((StatusChangeEvent) event).getStatus());
//		}else {
//			log.info("触发EurekaEvent事件[{}]", event.getClass());
//		}
    }

}
