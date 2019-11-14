package com.ibyte.framework.discovery;

import com.ibyte.framework.discovery.dto.ModuleMappingInfo;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模块映射帮助类
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @since 1.0.1
 */
@Slf4j
@ConfigurationProperties("kmss")
@RefreshScope
public class ModuleMappingHelper implements InitializingBean {
    /**
     * 匹配信息
     */
    @Setter
    private Map<String, ModuleMappingInfo> svr = new HashMap<>(1);

    @Autowired(required = false)
    List<ModuleMappingLoader> mappingLoaders;

    /**
     * 获取模块对应的信息
     *
     * @param moduleName
     * @return
     */
    public ModuleMappingInfo getMappingInfo(String moduleName) {
        return svr.get(moduleName);
    }

    /**
     * 映射信息中，应用是否存在
     *
     * @param appName
     * @return
     */
    public boolean appExists(String appName) {
        boolean exists = false;
        for (ModuleMappingInfo info : svr.values()) {
            if (info.getApp().equalsIgnoreCase(appName)) {
                exists = true;
                break;
            }
        }
        return exists;
    }

    /**
     * 映射信息中，模块是否存在
     *
     * @param moduleName
     * @return
     */
    public boolean moduleExists(String moduleName) {
        return svr.get(moduleName) != null;
    }

    /**
     * 所有映射
     *
     * @return
     */
    public Map<String, ModuleMappingInfo> getMappingInfos() {
        return svr;
    }

    /**
     * bean初始化完，额外动作
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (CollectionUtils.isEmpty(svr)) {
            if (!CollectionUtils.isEmpty(mappingLoaders)) {
                for (ModuleMappingLoader mappingLoader : mappingLoaders) {
                    Map<String, ModuleMappingInfo> mappings = mappingLoader.loadMapping();
                    if (!CollectionUtils.isEmpty(mappings)) {
                        svr.putAll(mappings);
                    }
                }
            }
        }
    }
}