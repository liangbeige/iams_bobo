package com.iams.manage.domain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.crypto.spec.SecretKeySpec;

@Configuration
public class EncryptionConfig {

//    // 读取配置文件中的密钥
//    @Value("${app.encryption.key}")
//    private String secretKey;
//
//    // 创建加密用的密钥对象
//    @Bean
//    public SecretKeySpec aesKey() {
//        byte[] keyBytes = secretKey.getBytes(); // 把字符串转成字节
//        return new SecretKeySpec(keyBytes, "AES"); // AES加密算法
//    }
}
