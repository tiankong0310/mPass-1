package com.ibyte.framework.discovery;

import com.ibyte.common.constant.NamingConstant;
import com.ibyte.common.util.thread.ThreadLocalHolder;
import com.ibyte.common.util.thread.ThreadLocalUtil;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * api远程调用头部信息帮助类
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @since 1.0.1
 */
public class DiscoveryHeaderHelper {

    /**
     * 配置中心配置前缀
     */
    private final String configPrefix = "spring.cloud.config";

    /**
     * helper实例
     */
    static DiscoveryHeaderHelper INSTANCE;

    /**
     * 环境对象
     */
    private Environment env;

    /**
     * 构造函数
     *
     * @param env
     */
    DiscoveryHeaderHelper(Environment env) {
        this.env = env;
    }

    public static DiscoveryHeaderHelper getInstance() {
        return INSTANCE;
    }

    /**
     * 远程调用
     *
     * @return
     */
    public Map<String, String> getRequestHeaderInfo() {
        Map<String, String> headers = new HashMap<String, String>(1);
        //通过本地线程变量传递需要远程服务接收的参数
        Map<String, Object> transMap = ThreadLocalHolder.getTranVars();
        for (Map.Entry<String, Object> entry : transMap.entrySet()) {
            if (!CollectionUtils.isEmpty(transMap)) {
                headers.put(ThreadLocalUtil.TRAN_PREFIX + entry.getKey(), (String) entry.getValue());
            }
        }
        //增加远程调用协议信息，因为系统所有请求是基于json请求参数的，MediaType 含编码,MimeTypeUtils不含编码
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        //增加本服务的认证信息,先用简单加密方式
        String serviceName = env.getProperty(configPrefix + ".headers." + NamingConstant.HEADER_KEY_SERVICE);
        if (!StringUtils.isEmpty(serviceName)) {
            headers.put(NamingConstant.HEADER_KEY_SERVICE, serviceName);
        }
        return headers;
    }
}
