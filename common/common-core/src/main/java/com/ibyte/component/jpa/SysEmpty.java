package com.ibyte.component.jpa;
import com.ibyte.common.core.entity.AbstractEntity;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @Description: <空表，该表只有一条记录，记录ID作为数据库的唯一标识>
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @Date: 2019-10-29
 */
@Setter
@Getter
@Entity
@Table
public class SysEmpty extends AbstractEntity {
}
