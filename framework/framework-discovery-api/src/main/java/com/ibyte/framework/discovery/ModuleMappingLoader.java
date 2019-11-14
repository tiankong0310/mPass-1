package com.ibyte.framework.discovery;

import com.ibyte.common.constant.NamingConstant;
import com.ibyte.common.util.StringHelper;
import com.ibyte.framework.discovery.dto.ModuleMappingInfo;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 模块映射加载器
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @since 1.0.1
 */
public interface ModuleMappingLoader {

    /**
     * 模块映射配置前缀
     */
    String SVR_PREFIX = "kmss.svr.";

    /**
     * 加载模块映射信息
     *
     * @return
     */
    Map<String, ModuleMappingInfo> loadMapping();

    /**
     * 构建单个maping信息到map中
     *
     * @param resultMap
     * @param key
     * @param value
     */
    default void buildMapingInfo(Map<String, ModuleMappingInfo> resultMap, String key, String value) {
        if (!StringUtils.isEmpty(key) && key.startsWith(SVR_PREFIX) && !StringUtils.isEmpty(value)) {
            String method = "set" + StringHelper.toFirstUpperCase(key.substring(key.lastIndexOf(NamingConstant.DOT) + 1));
            key = key.substring(SVR_PREFIX.length(), key.lastIndexOf(NamingConstant.DOT));
            ModuleMappingInfo info = resultMap.get(key);
            if (info == null) {
                info = new ModuleMappingInfo();
                resultMap.put(key, info);
            }
            Method methodObj = ReflectionUtils.findMethod(info.getClass(), method, String.class);
            if (methodObj != null) {
                ReflectionUtils.invokeMethod(methodObj, info, value);
            }
        }
    }
}
