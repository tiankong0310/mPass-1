package com.ibyte.framework.discovery.core.service;

import com.ibyte.common.constant.NamingConstant;
import com.ibyte.common.util.StringHelper;
import com.ibyte.framework.discovery.core.entity.ApplicationConfig;
import com.ibyte.framework.discovery.core.repository.ApplicationConfigRepository;
import com.ibyte.framework.support.persistent.ApplicationConfigRemoteApi;
import com.ibyte.framework.support.persistent.PersistentConstant;
import com.ibyte.framework.support.persistent.dto.ApplicationConfigVO;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 应用配置保存服务
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @since 1.0.1
 */
@RestController
@RequestMapping("/api/framework-discovery/applicationConfig")
@Service
@Transactional(readOnly = true, rollbackFor = {})
public class ApplicationConfigService
        implements ApplicationConfigRemoteApi, PersistentConstant {
    private static final long CACHE_EXPIRE_DAY = 7;

    @Autowired
    private ApplicationConfigRepository repository;

    @Autowired
    private AfterCommit afterCommit;

    @Autowired
    private RedissonClient redisson;

    @Override
    @Transactional(rollbackFor = {})
    public void save(ApplicationConfigVO vo) {
        ApplicationConfig entity = new ApplicationConfig();
        entity.setFdId(vo.getFdId());
        entity.setFdTenantId(vo.getFdTenantId());
        entity.setFdContent(vo.getFdContent());

        // 保存
        repository.save(entity);
        // 清理缓存
        afterCommit.execute(new Runnable() {
            @Override
            public void run() {
                getCache(entity.getFdId()).set(entity.getFdContent(),
                        CACHE_EXPIRE_DAY, TimeUnit.DAYS);
            }
        });
    }

    @Override
    public String get(String id) {
        RBucket<String> cache = getCache(id);
        String result = cache.get();
        if (result == null) {
            Optional<ApplicationConfig> entity = repository.findById(id);
            if (entity.isPresent()) {
                result = entity.get().getFdContent();
            } else {
                result = JSON_EMPTY;
            }
            cache.set(result, CACHE_EXPIRE_DAY, TimeUnit.DAYS);
        }
        return result;
    }

    private RBucket<String> getCache(String id) {
        String key = StringHelper.join(CONFIG_PREFIX,
                NamingConstant.shortName(id));
        return redisson.getBucket(key, StringCodec.INSTANCE);
    }
}
