package com.iams.minio.service;

import io.minio.MinioClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class MinIOService {
    @Resource
    private MinioClient minioClient;

    public void test() {
        System.out.println(minioClient);
    }
}
