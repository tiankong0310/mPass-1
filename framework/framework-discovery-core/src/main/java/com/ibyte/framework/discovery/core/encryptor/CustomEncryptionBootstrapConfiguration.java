package com.ibyte.framework.discovery.core.encryptor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.bootstrap.encrypt.EncryptionBootstrapConfiguration;
import org.springframework.cloud.bootstrap.encrypt.KeyProperties;
import org.springframework.cloud.config.server.config.EncryptionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.encrypt.TextEncryptor;

/**
 * 自定义配置文件及配置的加解密
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @since 1.0.1
 */
@Configuration
@EnableConfigurationProperties({KeyProperties.class})
@AutoConfigureBefore({EncryptionAutoConfiguration.class, EncryptionBootstrapConfiguration.class})
public class CustomEncryptionBootstrapConfiguration {
    /**
     * 配置中心自定义aes加密器
     * @param keyProperties
     * @return
     */
    @Bean
    @Primary
    public TextEncryptor getDefaultTextEncryptor(KeyProperties keyProperties) {
        return new CustomAesEncryptor(keyProperties);
    }
}
