package com.ibyte.framework.discovery.api;

import com.ibyte.framework.discovery.dto.SystemConfigVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Description: <系统级配置接口>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-31
 */
public interface ISystemConfigApi {

    /**
     * 保存配置
     * @param systemConfigVoList
     */
    @PostMapping("saveAll")
    void saveAll(@RequestBody List<SystemConfigVo> systemConfigVoList);

    /**
     * 删除
     *
     * @param key
     */
    @PostMapping("delete")
    void delete(@RequestBody String key);

    /**
     * 删除
     *
     * @param keys
     */
    @PostMapping("deleteAll")
    void deleteAll(@RequestBody String[] keys);

    /**
     * 获取所有的系统配置信息
     * @param keys
     * @return 配置信息列表
     */
    @PostMapping("findAll")
    List<SystemConfigVo> findAll(@RequestBody String[] keys);

    /**
     * 获取某个具体配置项的配置信息
     * @param key
     * @return 配置信息
     */
    @PostMapping("findOne")
    SystemConfigVo findOne(@RequestBody String key);

    /**
     * 清理某项配置
     *
     * @param configPre
     */
    @PostMapping("clear")
    void clear(String configPre);
}
