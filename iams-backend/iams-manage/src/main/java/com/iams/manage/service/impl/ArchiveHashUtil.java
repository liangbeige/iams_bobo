package com.iams.manage.service.impl;

import cn.hutool.core.io.IoUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpUtil;
import com.iams.manage.domain.Archive;
import com.iams.manage.domain.Document;
import com.iams.minio.config.MinIOInfo;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 档案哈希计算工具类
 * 用于计算档案的真实性和完整性标记
 */
@Component
public class ArchiveHashUtil {

    @Autowired
    private MinioClient minioClient;

    @Resource
    private MinIOInfo minioInfo;

    // 大文件阈值：10MB
    @Value("${archive.hash.large-file-threshold}")
    private static final long LARGE_FILE_THRESHOLD = 0;

    // 采样块大小：1MB
    @Value("${archive.hash.sample-block-size}")
    private static final int SAMPLE_BLOCK_SIZE = 10 * 1024 * 1024;

    // 采样块数量
    @Value("${archive.hash.sample-blocks}")
    private static final int SAMPLE_BLOCKS = 3;

    /**
     * 计算档案的真实性哈希
     * 基于档案信息和文档信息（名称、内容）
     */
    public String calculateAuthenticityHash(Archive archive, List<Document> documents, String config) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // 1. 添加档案基本信息
            String archiveInfo = buildArchiveInfoString(archive) + config;
            md.update(archiveInfo.getBytes("UTF-8"));

            // 2. 对文档列表按名称排序
            List<Document> sortedDocs = documents.stream()
                    .sorted(Comparator.comparing(Document::getName))
                    .collect(Collectors.toList());

            // 3. 添加每个文档的信息
            for (Document doc : sortedDocs) {
                String docInfo = buildDocumentInfoString(doc);
                md.update(docInfo.getBytes("UTF-8"));

                // 如果文档有内容字段，也加入计算
                if (doc.getContent() != null && !doc.getContent().isEmpty()) {
                    md.update(doc.getContent().getBytes("UTF-8"));
                }
            }

            // 4. 生成哈希值
            byte[] digest = md.digest();
            return bytesToHex(digest);

        } catch (Exception e) {
            throw new RuntimeException("计算真实性哈希失败", e);
        }
    }

    /**
     * 计算档案的完整性哈希
     * 基于档案信息和文档文件本身
     */
    public String calculateIntegrityHash(Archive archive, List<Document> documents, String config) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // 1. 添加档案基本信息
            String archiveInfo = buildArchiveInfoString(archive) + config;
            md.update(archiveInfo.getBytes("UTF-8"));

            // 2. 对文档列表按名称排序
            List<Document> sortedDocs = documents.stream()
                    .sorted(Comparator.comparing(Document::getName))
                    .collect(Collectors.toList());

            // 3. 添加每个文档文件的哈希
            for (Document doc : sortedDocs) {
                // 根据文件大小决定使用完整哈希还是采样哈希
                byte[] fileHash = calculateFileHashOptimized(doc);
                if (fileHash != null) {
                    md.update(fileHash);
                }
            }

            // 4. 生成最终哈希值
            byte[] digest = md.digest();
            return bytesToHex(digest);

        } catch (Exception e) {
            throw new RuntimeException("计算完整性哈希失败", e);
        }
    }

    /**
     * 优化的文件哈希计算方法
     * 根据文件大小决定使用完整哈希还是采样哈希
     */
    private byte[] calculateFileHashOptimized(Document document) {
        try {
            Long fileSize = document.getFileSize();

            // 如果文件大小未知或小于阈值，使用完整哈希
            if (fileSize == null || fileSize < LARGE_FILE_THRESHOLD) {
                return calculateFileHash(document);
            }

            // 大文件使用采样哈希
            if (document.getMinioName() != null && !document.getMinioName().isEmpty()) {
                return calculateSampledHashFromMinio(document.getMinioName(), fileSize);
            }

            if (document.getFilePath() != null && !document.getFilePath().isEmpty()) {
                return calculateSampledHashFromUrl(document.getFilePath(), fileSize);
            }

            return null;
        } catch (Exception e) {
            System.err.println("计算文件哈希失败: " + document.getName() + ", " + e.getMessage());
            return null;
        }
    }

    /**
     * 从MinIO计算大文件的采样哈希
     */
    private byte[] calculateSampledHashFromMinio(String objectName, long fileSize) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        // 计算采样位置
        long[] samplePositions = calculateSamplePositions(fileSize);

        // 添加文件大小信息到哈希计算中
        md.update(String.valueOf(fileSize).getBytes("UTF-8"));

        for (long position : samplePositions) {
            // 使用范围请求获取部分数据
            GetObjectArgs args = GetObjectArgs.builder()
                    .bucket(minioInfo.getBucket())
                    .object(objectName)
                    .offset(position)
                    .length((long) SAMPLE_BLOCK_SIZE)
                    .build();

            try (InputStream stream = minioClient.getObject(args)) {
                byte[] buffer = new byte[SAMPLE_BLOCK_SIZE];
                int totalRead = 0;
                int read;

                while (totalRead < SAMPLE_BLOCK_SIZE && (read = stream.read(buffer, totalRead, SAMPLE_BLOCK_SIZE - totalRead)) != -1) {
                    totalRead += read;
                }

                // 更新哈希值
                md.update(buffer, 0, totalRead);
            }
        }

        return md.digest();
    }

    /**
     * 从URL计算大文件的采样哈希
     */
    private byte[] calculateSampledHashFromUrl(String fileUrl, long fileSize) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String cleanUrl = extractCleanUrl(fileUrl);

        // 添加文件大小信息到哈希计算中
        md.update(String.valueOf(fileSize).getBytes("UTF-8"));

        // 计算采样位置
        long[] samplePositions = calculateSamplePositions(fileSize);

        for (long position : samplePositions) {
            // 使用Range请求获取部分数据
            long endPosition = Math.min(position + SAMPLE_BLOCK_SIZE - 1, fileSize - 1);
            byte[] partialContent = HttpUtil.createGet(cleanUrl)
                    .header("Range", String.format("bytes=%d-%d", position, endPosition))
                    .execute()
                    .bodyBytes();

            md.update(partialContent);
        }

        return md.digest();
    }

    /**
     * 计算采样位置
     * 返回文件开头、中间和结尾的采样位置
     */
    private long[] calculateSamplePositions(long fileSize) {
        long[] positions = new long[SAMPLE_BLOCKS];

        // 开头位置
        positions[0] = 0;

        // 中间位置
        positions[1] = (fileSize / 2) - (SAMPLE_BLOCK_SIZE / 2);
        if (positions[1] < 0) positions[1] = 0;

        // 结尾位置
        positions[2] = fileSize - SAMPLE_BLOCK_SIZE;
        if (positions[2] < 0) positions[2] = 0;

        // 确保位置不重叠
        for (int i = 1; i < positions.length; i++) {
            if (positions[i] < positions[i-1] + SAMPLE_BLOCK_SIZE) {
                positions[i] = positions[i-1] + SAMPLE_BLOCK_SIZE;
            }
        }

        return positions;
    }

    /**
     * 获取MinIO对象的大小
     */
    private long getMinioObjectSize(String objectName) throws Exception {
        StatObjectResponse stat = minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(minioInfo.getBucket())
                        .object(objectName)
                        .build());
        return stat.size();
    }

    /**
     * 构建档案信息字符串
     */
    private String buildArchiveInfoString(Archive archive) {
        StringBuilder sb = new StringBuilder();

        // 添加档案的关键信息
        if (archive.getDanghao() != null) {
            sb.append(archive.getDanghao()).append("|");
        }
        if (archive.getName() != null) {
            sb.append(archive.getName()).append("|");
        }
        if (archive.getRfid() != null) {
            // 只取前22个字符
            String rfid = archive.getRfid();
            sb.append(rfid, 0, Math.min(rfid.length(), 22)).append("|");
        }
        if (archive.getProjectId() != null) {
            sb.append(archive.getProjectId()).append("|");
        }

        return sb.toString();
    }

    /**
     * 构建文档信息字符串
     */
    private String buildDocumentInfoString(Document document) {
        StringBuilder sb = new StringBuilder();

        // 添加文档的关键信息
        if (document.getName() != null) {
            sb.append(document.getName()).append("|");
        }
        if (document.getFileType() != null) {
            sb.append(document.getFileType()).append("|");
        }
        if (document.getFileSize() != null) {
            sb.append(document.getFileSize()).append("|");
        }

        return sb.toString();
    }

    /**
     * 计算单个文件的哈希值（完整文件）
     */
    private byte[] calculateFileHash(Document document) {
        try {
            // 方法1：如果有MinIO文件名，直接从MinIO下载
            if (document.getMinioName() != null && !document.getMinioName().isEmpty()) {
                return calculateHashFromMinio(document.getMinioName());
            }

            // 方法2：从URL下载文件
            if (document.getFilePath() != null && !document.getFilePath().isEmpty()) {
                return calculateHashFromUrl(document.getFilePath());
            }

            return null;
        } catch (Exception e) {
            // 记录错误但不中断整体流程
            System.err.println("计算文件哈希失败: " + document.getName() + ", " + e.getMessage());
            return null;
        }
    }

    /**
     * 从MinIO直接获取文件并计算哈希
     */
    private byte[] calculateHashFromMinio(String objectName) throws Exception {

        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minioInfo.getBucket())
                        .object(objectName)
                        .build())) {

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;

            while ((read = stream.read(buffer)) != -1) {
                md.update(buffer, 0, read);
            }

            return md.digest();
        }
    }

    /**
     * 从URL下载文件并计算哈希
     */
    private byte[] calculateHashFromUrl(String fileUrl) throws Exception {
        // 处理URL，提取实际的文件路径
        String cleanUrl = extractCleanUrl(fileUrl);

        // 下载文件内容
        byte[] fileContent = HttpUtil.downloadBytes(cleanUrl);

        // 计算哈希
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(fileContent);
        return md.digest();
    }

    /**
     * 从带签名的URL中提取干净的URL
     */
    private String extractCleanUrl(String signedUrl) {
        // 移除查询参数，只保留基本URL
        int queryIndex = signedUrl.indexOf("?");
        if (queryIndex > 0) {
            return signedUrl.substring(0, queryIndex);
        }
        return signedUrl;
    }

    /**
     * 将字节数组转换为十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * 使用Hutool简化版本（可选）
     */
    public String calculateAuthenticityHashSimple(Archive archive, List<Document> documents) {
        StringBuilder content = new StringBuilder();

        // 添加档案信息
        content.append(buildArchiveInfoString(archive));

        // 排序并添加文档信息
        documents.stream()
                .sorted(Comparator.comparing(Document::getName))
                .forEach(doc -> {
                    content.append(buildDocumentInfoString(doc));
                    if (doc.getContent() != null) {
                        content.append(doc.getContent());
                    }
                });

        // 使用Hutool计算SHA256
        return SecureUtil.sha256(content.toString());
    }
}