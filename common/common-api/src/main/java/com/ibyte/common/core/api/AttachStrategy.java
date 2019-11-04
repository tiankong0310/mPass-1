package com.ibyte.common.core.api;

/**
 * @Description: <数据导入附件策略>
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @Date: 2019-10-12
 */
public interface AttachStrategy {

    String getEntityClass();

    /**
     * 处理实体和附件的关联关系
     * @param entityClass
     * @param fdEntityId
     * @param fdAttachId
     */
    void handleAttach(String entityClass, String fdEntityId, String fdAttachId);
}
