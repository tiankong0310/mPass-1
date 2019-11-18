package com.ibyte.framework.discovery.core.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ibyte.common.util.StringHelper;
import com.ibyte.common.util.TenantUtil;
import com.ibyte.framework.config.ApplicationConfigApi;
import com.ibyte.framework.config.dto.PluginConfig;
import com.ibyte.framework.discovery.core.entity.DesignElement;
import com.ibyte.framework.discovery.core.repository.DesignElementRepository;
import com.ibyte.framework.meta.MetaSummary;
import com.ibyte.framework.support.domain.ExtensionImpl;
import com.ibyte.framework.support.domain.ExtensionPointImpl;
import com.ibyte.framework.support.domain.MetaApplicationImpl;
import com.ibyte.framework.support.persistent.DesignElementRemoteApi;
import com.ibyte.framework.support.persistent.PersistentConstant;
import com.ibyte.framework.support.persistent.dto.DesignElementDetail;
import com.ibyte.framework.support.persistent.dto.DesignElementGroup;
import com.ibyte.framework.support.util.SerializeUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 设计信息保存服务
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @since 1.0.1
 */
@RestController
@RequestMapping("/api/framework-discovery/designElement")
@Service
@Transactional(readOnly = true, rollbackFor = {})
public class DesignElementService
        implements DesignElementRemoteApi, PersistentConstant {
    private static final long CACHE_EXPIRE_DAY = 7;
    private static final int EXTENSIONID_SEGMENT = 3;

    @Autowired
    private DesignElementRepository repository;

    @Autowired
    private AfterCommit afterCommit;

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private ApplicationConfigApi applicationConfigApi;

    // ========== 设计元素持久化操作 ==========

    @Override
    @Transactional(rollbackFor = {})
    public void saveAll(DesignElementGroup group) {
        // 保存
        List<DesignElementDetail> saveList = group.getSaveList();
        for (DesignElementDetail detail : saveList) {
            detail.setFdAppName(group.getFdAppName());
            DesignElement entity = voToEntity(detail);
            repository.save(entity);
        }
        // 删除
        Set<String> paths = new HashSet<>();
        Set<String> points = new HashSet<>();
        for (String id : group.getDeleteList()) {
            paths.add(id);
            appendClearPath(id, paths, points);
            if (repository.existsById(id)) {
                repository.deleteById(id);
            }
        }
        // 清理缓存
        afterCommit.execute(new Runnable() {
            @Override
            public void run() {
                updateCache(saveList, paths, points);
                notifyPointChanged(points);
            }
        });
    }

    @Override
    public String get(String id) {
        RBucket<String> cache = redisson.getBucket(id, StringCodec.INSTANCE);
        String result = cache.get();
        if (result == null) {
            Optional<DesignElement> entity = repository.findById(id);
            if (entity.isPresent()) {
                result = entity.get().getFdContent();
            } else {
                result = JSON_EMPTY;
            }
            cache.set(result, CACHE_EXPIRE_DAY, TimeUnit.DAYS);
        }
        return result;
    }

    @Override
    public String findSummary(String path) {
        RBucket<String> cache = redisson.getBucket(path, StringCodec.INSTANCE);
        String result = cache.get();
        if (result == null) {
            result = listToString(repository.findSummary(
                    StringHelper.join(path, ":%")));
            cache.set(result, CACHE_EXPIRE_DAY, TimeUnit.DAYS);
        }
        return result;
    }

    @Override
    public String findSummaryByApp(DesignElementDetail detail) {
        String result = listToString(repository.findSummaryByApp(
                StringHelper.join(detail.getFdId(), ":%"),
                detail.getFdAppName()));
        return result;
    }

    @Override
    public String findApplications() {
        String path = ElementType.MetaApplication.name();
        RBucket<String> cache = redisson.getBucket(path, StringCodec.INSTANCE);
        String result = cache.get();
        if (result == null) {
            // 缓存中没有，从数据库加载
            List<String> contents = repository
                    .findContent(StringHelper.join(path, ":%"));
            List<MetaApplicationImpl> applications = new ArrayList<>(
                    contents.size());
            for (String content : contents) {
                MetaApplicationImpl application = SerializeUtil.parseObject(
                        content, MetaApplicationImpl.class);
                applications.add(application);
            }
            result = SerializeUtil.toString(applications);
            cache.set(result, CACHE_EXPIRE_DAY, TimeUnit.DAYS);
        }
        return result;
    }

    @Override
    public String findExtensions(String pointId) {
        String path = PersistentConstant.toId(ElementType.Extension, pointId);
        RBucket<String> cache = redisson.getBucket(path, StringCodec.INSTANCE);
        String result = cache.get();
        if (result == null) {
            // 缓存中没有，从数据库加载
            List<String> contents = repository
                    .findContent(StringHelper.join(path, ":%"));
            List<ExtensionImpl> extensions = new ArrayList<>(contents.size());
            PluginConfig config = getPluginConfig();
            ExtensionImpl selected = null;
            for (String content : contents) {
                ExtensionImpl extension = SerializeUtil.parseObject(content,
                        ExtensionImpl.class);
                String id = PersistentConstant.toId(ElementType.Extension,
                        pointId, extension.getId());
                // 禁用
                if (config.getDisabledExtensions().contains(id)) {
                    continue;
                }
                // 选定
                if (config.getSelectedExtensions().contains(id)) {
                    selected = extension;
                }
                extensions.add(extension);
            }
            // 取扩展点，根据扩展点进行排序或取单值
            ExtensionPointImpl point = getExtensionPoint(pointId);
            if (point != null) {
                if (point.isOrdered()) {
                    Collections.sort(extensions);
                }
                if (point.isSingleton() && extensions.size() > 1) {
                    if (selected == null) {
                        selected = extensions.get(0);
                    }
                    extensions.clear();
                    extensions.add(selected);
                }
            }
            result = SerializeUtil.toString(extensions);
            cache.set(result, CACHE_EXPIRE_DAY, TimeUnit.DAYS);
        }
        return result;
    }

    /** 读取扩展点 */
    private ExtensionPointImpl getExtensionPoint(String pointId) {
        String text = get(PersistentConstant.toId(
                ElementType.ExtensionPoint, pointId));
        if (JSON_EMPTY.equals(text)) {
            return null;
        } else {
            return SerializeUtil.parseObject(text, ExtensionPointImpl.class);
        }
    }

    /** 更新缓存 */
    private void updateCache(List<DesignElementDetail> details,
                             Set<String> paths, Set<String> points) {
        // 更新ID对应的内容
        for (DesignElementDetail detail : details) {
            appendClearPath(detail.getFdId(), paths, points);
            redisson.getBucket(detail.getFdId(), StringCodec.INSTANCE).set(
                    detail.getFdContent(), CACHE_EXPIRE_DAY, TimeUnit.DAYS);
        }
        // 清空上级内容
        for (String path : paths) {
            redisson.getBucket(path, StringCodec.INSTANCE).delete();
        }
    }

    /** 追加需要清理缓存的路径，需要通知的扩展点 */
    private void appendClearPath(String path, Set<String> paths,
                                 Set<String> points) {
        // 清理ExtensionPoint的时候，要清理Extension的列表缓存
        if (path.startsWith(ElementType.ExtensionPoint.name())) {
            String exPath = StringHelper.join(ElementType.Extension.name(),
                    path.substring(ElementType.ExtensionPoint.name().length()));
            paths.add(exPath);
        } else if (path.startsWith(ElementType.Extension.name())) {
            String[] array = path.split(PATH_SPLIT);
            if (array.length == EXTENSIONID_SEGMENT) {
                points.add(array[1]);
            }
        }
        // 清理上级的列表缓存
        int index = path.lastIndexOf(PATH_SPLIT);
        paths.add(path.substring(0, index));
    }

    /** 查询摘要信息，并转换成String */
    private String listToString(List<Object[]> list) {
        JSONArray array = new JSONArray();
        for (Object[] summary : list) {
            JSONObject json = new JSONObject();
            json.put(PROP_ID, summary[0]);
            json.put(PROP_LABEL, summary[1]);
            json.put(PROP_MD5, summary[2]);
            json.put(PROP_MESSAGEKEY, summary[3]);
            json.put(PROP_MODULE, summary[4]);
            array.add(json);
        }
        return array.toString();
    }

    /** VO转PO */
    private DesignElement voToEntity(DesignElementDetail detail) {
        DesignElement config = new DesignElement();
        JSONObject json = JSONObject.parseObject(detail.getFdContent());
        config.setFdId(detail.getFdId());
        config.setFdAppName(detail.getFdAppName());
        config.setFdContent(detail.getFdContent());
        config.setFdLabel(json.getString(PROP_LABEL));
        if (StringUtils.isBlank(detail.getFdMd5())) {
            config.setFdMd5(buildMd5(detail.getFdContent()));
        } else {
            config.setFdMd5(detail.getFdMd5());
        }
        config.setFdMessageKey(json.getString(PROP_MESSAGEKEY));
        config.setFdModule(json.getString(PROP_MODULE));
        return config;
    }

    /** 计算MD5 */
    private String buildMd5(String content) {
        String md5 = DigestUtils.md5DigestAsHex(
                content.getBytes(StandardCharsets.UTF_8));
        return StringHelper.join(md5,
                Integer.toHexString(content.hashCode()), ':',
                Integer.toHexString(content.length()));
    }

    // ========== 插件配置操作 ==========

    @Override
    public List<ExtensionPointImpl> findConfigurablePoints() {
        String path = StringHelper.join(ElementType.ExtensionPoint.name(),
                ":%");
        List<String> contents = repository.findContent(path);
        List<ExtensionPointImpl> result = new ArrayList<>();
        for (String content : contents) {
            ExtensionPointImpl point = SerializeUtil.parseObject(content,
                    ExtensionPointImpl.class);
            if (point.isConfigurable()) {
                result.add(point);
            }
        }
        return result;
    }

    @Override
    public List<MetaSummary> findAllExtensions(String pointId) {
        String path = StringHelper.join(
                PersistentConstant.toId(ElementType.Extension, pointId), ":%");
        // fdId, fdLabel, fdMd5, fdMessageKey, fdModule
        List<Object[]> list = repository.findSummary(path);
        List<MetaSummary> result = new ArrayList<>();
        for (Object[] info : list) {
            MetaSummary summary = new MetaSummary();
            summary.setId((String) info[0]);
            summary.setLabel((String) info[1]);
            // 2是md5，不需要返回
            summary.setMessageKey((String) info[3]);
            summary.setModule((String) info[4]);
            result.add(summary);
        }
        return result;
    }

    @Override
    public PluginConfig getPluginConfig() {
        PluginConfig config = applicationConfigApi.get(PluginConfig.class,
                TenantUtil.SYSTEM_TENANT);
        return config == null ? new PluginConfig() : config;
    }

    @Transactional(rollbackFor = {})
    @Override
    public void savePluginConfig(PluginConfig config) {
        // 根据新旧的差异，计算需要清理缓存的key
        PluginConfig old = getPluginConfig();
        Set<String> paths = new HashSet<>();
        Set<String> points = new HashSet<>();
        appendDiffToClearPath(old.getDisabledExtensions(),
                config.getDisabledExtensions(), paths, points);
        appendDiffToClearPath(old.getSelectedExtensions(),
                config.getSelectedExtensions(), paths, points);
        // 保存
        applicationConfigApi.save(config, TenantUtil.SYSTEM_TENANT);
        // 提交后清理缓存
        afterCommit.execute(new Runnable() {
            @Override
            public void run() {
                for (String path : paths) {
                    redisson.getBucket(path, StringCodec.INSTANCE).delete();
                }
                notifyPointChanged(points);
            }
        });
    }

    /** 将列表差异点写入缓存清理的内容中 */
    private void appendDiffToClearPath(List<String> one, List<String> two,
                                       Set<String> paths, Set<String> points) {
        for (String s : one) {
            if (!two.contains(s)) {
                appendClearPath(s, paths, points);
            }
        }
        for (String s : two) {
            if (!one.contains(s)) {
                appendClearPath(s, paths, points);
            }
        }
    }

    /** 通知扩展点的扩展信息发生变更 */
    private void notifyPointChanged(Set<String> points) {
        if (points.isEmpty()) {
            return;
        }
        redisson.getTopic(EXTENSIONPOINT_CHANGE_TOPIC).publish(points);
    }
}
