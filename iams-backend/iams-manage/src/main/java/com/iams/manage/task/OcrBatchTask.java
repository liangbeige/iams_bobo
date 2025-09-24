package com.iams.manage.task;

import com.iams.manage.domain.Document;
import com.iams.manage.domain.DocumentDto;
import com.iams.manage.domain.Metadata;
import com.iams.manage.service.IDocumentService;
import com.iams.manage.service.IMetadataService;
import com.iams.minio.config.MinIOInfo;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import jakarta.annotation.Resource;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class OcrBatchTask {

    @Resource
    private IDocumentService documentService;

    @Resource
    private IMetadataService metadataService;

    @Resource
    private MinioClient minioClient;

//    private final String MAIN_BUCKET = "iams-main-files";
    @Resource
    private MinIOInfo minIOInfo;

    @Value("${ocr.server.url}")
    private String ocrServerUrl; // 例如 http://localhost:8080/ocr/upload

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 每隔 5 分钟执行一次任务
     */
    @Scheduled(cron = "0 0 0 * * ?") // 每天0点执行
    public void performOcrBatchTask() {
        List<Document> documents = documentService.selectMinioNameList();
        List<Document> filteredDocuments = documents.stream()
                .filter(doc -> doc.getName() != null && (doc.getName().endsWith(".pdf")
                        || doc.getName().endsWith(".jpg")
                        || doc.getName().endsWith(".jpeg")
                        || doc.getName().endsWith(".png")))
                .toList();
        if (documents.isEmpty()) {
            return;
        }
        ArrayList<DocumentDto> documentDtos = new ArrayList<>();

        for (Document document : filteredDocuments) {

            List<Integer> metadataIds = new ArrayList<>();
            String[] idArray = document.getMetadataIds().split(","); // 先获取分割后的数组

            for (String id : idArray) {
                try {
                    // 去除空格后解析为整数
                    metadataIds.add(Integer.parseInt(id.trim()));
                } catch (NumberFormatException e) {
                    // 处理无效输入（例如记录日志或跳过）
                    System.err.println("Skipping invalid metadata ID: " + id);
                    // 可选：根据需求抛出自定义异常或添加默认值
                }
            }
            List<Metadata> metadataList = metadataService.selectMetadataByIds(metadataIds);
            DocumentDto documentDto = new DocumentDto(document.getId(), document.getMinioName(), metadataList);
            documentDtos.add(documentDto);
        }
        CompletableFuture.runAsync(() -> sendToOcrServer(documentDtos));
    }

    private void sendToOcrServer(List<DocumentDto> document) {
        try {

            Map<String,Object> map = new HashMap<>();
            map.put("files", document);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(map, headers);
            restTemplate.postForEntity(ocrServerUrl, requestEntity, String.class);

        } catch (Exception e) {
            //System.err.println("处理文档 " + document.getId() + " 出错: " + e.getMessage());
        }

    }
    // 同步版本
//    private void sendToOcrServer(List<DocumentDto> document) {
//        try {
//            Map<String,Object> map = new HashMap<>();
//            map.put("files", document);
//
//            // 2. 设置请求头
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//
//            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(map, headers);
//
//            // 3. 发送 POST 请求到 OCR 服务器并获取响应
//            ResponseEntity<String> response = restTemplate.postForEntity(ocrServerUrl, requestEntity, String.class);
//
//            // 4. 打印返回结果
//            if (response.getStatusCode() == HttpStatus.OK) {
//                List<Object> results = Collections.singletonList(response.getBody());
//                for (Object result : results) {
////                    Document document = documentService.selectDocumentById(result.id);
//                }
//                System.out.println("请求成功，返回结果: " + response.getBody());
//            } else {
//                System.err.println("请求失败，状态码: " + response.getStatusCode());
//            }
//
//        } catch (Exception e) {
//            System.err.println("处理文档出错: " + e.getMessage());
//        }
//    }
}
