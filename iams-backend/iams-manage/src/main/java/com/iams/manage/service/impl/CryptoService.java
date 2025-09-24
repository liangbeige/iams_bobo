package com.iams.manage.service.impl;
// src/main/java/com/yourpackage/service/CryptoService.java
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.NoSuchPaddingException;

public class CryptoService {
    private static final int ITERATIONS = 100000; // 必须与前端一致
    private static final int KEY_LENGTH = 256;     // AES-256

    public static SecretKey deriveKey(String password, byte[] iv)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                iv,          // 使用IV作为盐值（必须与前端一致）
                ITERATIONS,
                KEY_LENGTH
        );

        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * 获取解密用的 Cipher 实例
     * @param key 解密密钥（由 deriveKey 方法生成）
     * @param iv  初始化向量（必须与加密时使用的相同）
     * @return 初始化的解密 Cipher 对象
     */
    public static Cipher getDecryptCipher(SecretKey key, byte[] iv)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException {

        // 1. 获取 AES/GCM/NoPadding 密码实例
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        // 2. 创建参数规范（GCM 需要 128 位认证标签）
        AlgorithmParameterSpec gcmSpec = new GCMParameterSpec(128, iv);

        // 3. 初始化解密模式
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

        return cipher;
    }
}
