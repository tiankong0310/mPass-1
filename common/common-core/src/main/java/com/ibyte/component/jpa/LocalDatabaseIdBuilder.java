package com.ibyte.component.jpa;

import com.ibyte.framework.support.persistent.DatabaseIdBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * 读取本地数据库ID
 * 
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 */
@Service
public class LocalDatabaseIdBuilder implements DatabaseIdBuilder {
	@Autowired
	private EntityManager entityManager;

	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor = {})
	@Override
	public String getDatabaseId() {
		Query query = entityManager
				.createQuery("select fdId from SysEmpty order by fdId");
		query.setFirstResult(0);
		query.setMaxResults(1);
		List<String> list = query.getResultList();
		if (!list.isEmpty()) {
			return list.get(0);
		}
		SysEmpty entity = new SysEmpty();
		entityManager.persist(entity);
		return entity.getFdId();
	}
}
