package com.ibyte.common.core.repository;

import com.ibyte.common.core.entity.IEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * 仓库层接口
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 *
 */
@NoRepositoryBean
public interface IRepository<E extends IEntity>
		extends CrudRepository<E, String>, JpaSpecificationExecutor<E> {
	/**
	 * 获取延迟加载的Entity
	 * 
	 * @param id
	 * @return
	 */
	E getOne(String id);
}
