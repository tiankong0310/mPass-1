package com.ibyte.framework.discovery.client.interceptor;

import com.ibyte.framework.discovery.DiscoveryHeaderHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Map;

/**
 * RestTemplate interceptors 远程调用请求增加头部信息处理
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @since 1.0.1
 */
public class RestApiHeaderInterceptor implements ClientHttpRequestInterceptor {


    /**
     * 处理restTemplate的请求拦截
     *
     * @param request
     * @param body
     * @param execution
     * @return
     * @throws IOException
     */
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        HttpHeaders headers = request.getHeaders();
        for (Map.Entry<String, String> entry : DiscoveryHeaderHelper.getInstance().getRequestHeaderInfo().entrySet()) {
            headers.add(entry.getKey(), entry.getValue());
        }
        return execution.execute(request, body);
    }
}
