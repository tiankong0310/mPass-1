package com.ibyte.framework.discovery.core.repository;

import com.ibyte.framework.discovery.core.entity.SystemConfig;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


/**
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @since 1.0.1
 */
@Repository
public interface SystemConfigRepository extends CrudRepository<SystemConfig, String> {
}

