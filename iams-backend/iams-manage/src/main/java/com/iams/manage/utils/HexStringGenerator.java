package com.iams.manage.utils;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;


public class HexStringGenerator {

    private static final SecureRandom random = new SecureRandom();

    public static String generate23CharHexString() {
        // 生成 12 个随机字节（12 * 2 = 24 个十六进制字符）
        byte[] bytes = new byte[12];
        random.nextBytes(bytes);

        // 转换为十六进制字符串并截取前 23 位
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }

        return hex.substring(0, 22);
    }
}