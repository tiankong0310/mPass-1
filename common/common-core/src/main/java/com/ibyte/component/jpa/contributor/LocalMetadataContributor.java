package com.ibyte.component.jpa.contributor;

import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataContributor;
import org.jboss.jandex.IndexView;

/**
 * 自定义元数据完善，该服务在下面文件位置注册
 * /META-INF/services/org.hibernate.boot.spi.MetadataContributor
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 */
public class LocalMetadataContributor
		implements MetadataContributor {
	private MetadataContributor[] contributors = new MetadataContributor[] {
			new InterfaceMetadataContributor(), new ExtMetadataContributor(), new LanguageMetadataContributor(), new ExtraMappingMetadataContributor()
	};

	@Override
	public void contribute(InFlightMetadataCollector metadataCollector,
			IndexView jandexIndex) {
		for (MetadataContributor contributor : contributors) {
			contributor.contribute(metadataCollector, jandexIndex);
		}
	}
}
