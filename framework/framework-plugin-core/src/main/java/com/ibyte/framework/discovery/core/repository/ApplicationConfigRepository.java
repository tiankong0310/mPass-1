package com.ibyte.framework.discovery.core.repository;

import com.ibyte.framework.discovery.core.entity.ApplicationConfig;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * 应用配置  仓库
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @since 1.0.1
 */
@Repository
public interface ApplicationConfigRepository
        extends CrudRepository<ApplicationConfig, String> {
}
