package com.ibyte.framework.discovery.client.interceptor;

import com.ibyte.framework.discovery.DiscoveryHeaderHelper;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

import java.util.Collections;
import java.util.Map;

/**
 * 注册中心,注册心跳及复制使用过滤器
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @since 1.0.1
 */
public class DiscoveryHeaderClientFilter extends ClientFilter {
    @Override
    public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
        Map<String, String> headers = DiscoveryHeaderHelper.getInstance().getRequestHeaderInfo();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            cr.getHeaders().put(entry.getKey(), Collections.singletonList(entry.getValue()));
        }
        return getNext().handle(cr);
    }
}
