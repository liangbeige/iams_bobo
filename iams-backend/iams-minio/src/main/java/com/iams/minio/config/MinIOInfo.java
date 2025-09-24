package com.iams.minio.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinIOInfo {

    private String endpoint;    // minio服务器的IP:PORT地址
    private String accessKey;   // minio服务器的用户名
    private String secretKey;   // minio服务器的用户密码
    private String bucket;      // 存放上传的完整文件
    private String tempBucket;  // 临时存放上传的文件分片
    private String certBucket;  // 存放project cert文件

}
