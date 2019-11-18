package com.ibyte.framework.discovery.core.encryptor;

import com.ibyte.common.security.encryption.IEncrypt;
import com.ibyte.common.security.encryption.provider.AesEncryptProvider;
import org.springframework.cloud.bootstrap.encrypt.KeyProperties;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.util.StringUtils;

/**
 * ase 加密器
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @since 1.0.1
 */
public class CustomAesEncryptor implements TextEncryptor {
    /**
     * 加密接口
     */
    private IEncrypt encryptor = null;

    /**
     * 默认构造函数
     */
    CustomAesEncryptor(KeyProperties props) {
        if (!StringUtils.isEmpty(props.getKey())) {
            encryptor = new AesEncryptProvider(props.getKey());
        }
    }

    @Override
    public String encrypt(String s) {
        if (encryptor != null) {
            return encryptor.encrypt(s);
        } else {
            return s;
        }
    }

    @Override
    public String decrypt(String s) {
        if (encryptor != null) {
            return encryptor.decrypt(s);
        } else {
            return s;
        }
    }
}
