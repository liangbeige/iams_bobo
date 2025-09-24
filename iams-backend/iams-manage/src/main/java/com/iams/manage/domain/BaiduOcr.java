package com.iams.manage.domain;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * BaiduOcrProperties类用于配置百度OCR服务的相关属性。
 * 该类通过@ConfigurationProperties注解与配置文件中的baidu.ocr前缀绑定，
 * 使得我们可以从配置文件中动态读取appId, apiKey和secretKey等属性值
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "baidu.ocr")
public class BaiduOcr {
    // 百度OCR的App ID
    private String appId;
    // 百度OCR的API Key
    private String apiKey;
    // 百度OCR的Secret Key
    private String secretKey;
}
