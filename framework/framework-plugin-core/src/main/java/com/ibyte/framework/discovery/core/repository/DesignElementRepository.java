package com.ibyte.framework.discovery.core.repository;

import com.ibyte.framework.discovery.core.entity.DesignElement;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 设计元素仓库
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @since 1.0.1
 */
@Repository
public interface DesignElementRepository
        extends CrudRepository<DesignElement, String> {
    /**
     * 根据路径查找摘要信息
     *
     * @param path
     * @return
     */
    @Query("select fdId, fdLabel, fdMd5, fdMessageKey, fdModule from DesignElement where fdId like :path")
    List<Object[]> findSummary(@Param("path") String path);

    /**
     * 根据路径和应用名查找摘要信息
     *
     * @param path
     * @param appName
     * @return
     */
    @Query("select fdId, fdLabel, fdMd5, fdMessageKey, fdModule from DesignElement where fdId like :path and fdAppName=:appName")
    List<Object[]> findSummaryByApp(@Param("path") String path,
                                    @Param("appName") String appName);

    /**
     * 根据路径查找摘要信息
     *
     * @param path
     * @return
     */
    @Query("select fdContent from DesignElement where fdId like :path")
    List<String> findContent(@Param("path") String path);
}
