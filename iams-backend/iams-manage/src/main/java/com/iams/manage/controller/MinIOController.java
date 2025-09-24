package com.iams.manage.controller;

import com.google.common.hash.HashCode;
import com.iams.manage.domain.Archive;
import com.iams.manage.domain.Document;
import com.iams.manage.domain.Project;
import com.iams.manage.service.IArchiveService;
import com.iams.manage.service.IDocumentService;
import com.iams.manage.service.IProjectService;
import com.iams.minio.config.MinIOInfo;
import com.iams.minio.result.R;
import com.iams.minio.service.EncryptionService;
import com.iams.manage.domain.MergeRequestDTO.MergeRequest;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.google.common.hash.Hashing;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.iams.framework.datasource.DynamicDataSourceContextHolder.log;

@CrossOrigin
@RestController
@RequestMapping("/minio")
public class MinIOController {
//    private final String TEMP_BUCKET = "iams-tmp-files";
//    private final String MAIN_BUCKET = "iams-main-files";
//    private final String CERT_BUCKET = "iams-project-cert";
    @Resource
    private IDocumentService documentService;
    @Resource
    private IProjectService projectService;
    @Resource
    private MinioClient minioClient;
    @Resource
    private EncryptionService encryptionService;
    @Resource
    private MinIOInfo minIOInfo;

    // 检查文件状态
    @GetMapping("/check")
    public ResponseEntity<?> checkFile(@RequestParam String fileHash,
                                       @RequestParam String filename) {
        try {
            boolean exists = false;
            // 检查完整文件是否存在
            try {
                minioClient.statObject(
                        StatObjectArgs.builder().bucket(minIOInfo.getBucket()).object(filename).build()
                );
                exists = true;
            } catch (ErrorResponseException e) {
                if ("NoSuchKey".equals(e.errorResponse().code())) {
                    System.out.println("文件不存在"); // 文件不存在，正常流程
                } else {
                    throw e; // 其他异常重新抛出
                }
            }

            // 列出分片（无论文件是否存在都执行）
            List<Integer> uploadedChunks = getUploadedChunks(fileHash);

            return ResponseEntity.ok(Map.of(
                    "exists", exists,
                    "uploadedChunks", uploadedChunks.stream().sorted().collect(Collectors.toList())
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("检查失败: " + e.getMessage());
        }
    }

    // 上传分片
    @PostMapping("/upload")
    public R uploadChunk(
            @RequestParam("file") MultipartFile file,
            @RequestParam String hash,
            @RequestParam int index,
            // 移除 iv 参数
            @RequestParam(required = false) String salt,    // 仅保留 salt
            @RequestHeader(value = "X-Encrypted", defaultValue = "false") boolean encrypted
    ) {
        try {
            String objectName = hash + "/" + index;

            Map<String, String> userMetadata = new HashMap<>();
            if (encrypted) {
                if (index == 0) {
                    // 首分片必须包含 salt
                    if (salt == null) { // 移除对 iv 的检查
                        return R.FAIL("首分片必须包含 salt");
                    }
                    userMetadata.put("salt", salt);
                } else {
                    // 禁止非首分片携带参数
                    if (salt != null) { // 仅检查 salt
                        return R.FAIL("非首分片不得包含加密参数");
                    }
                }
            }
            // 创建带元数据的上传参数
            PutObjectArgs putArgs = PutObjectArgs.builder()
                    .bucket(minIOInfo.getTempBucket())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .userMetadata(userMetadata) // 注入元数据
                    .build();

            minioClient.putObject(putArgs);

            return R.OK();
        } catch (Exception e) {
            log.error("分片上传失败: {}", e.getMessage());
            return R.FAIL("分片上传失败");
        }
    }

    // 合并分片
    // 合并入口方法
    @PostMapping("/merge")
    public R mergeChunks(@RequestBody MergeRequest request) {
        try {
            R result = request.isEncrypted() ?
                    mergeFile(request, true) :
                    mergeFile(request, false);

            if (result.getCode() == 200) {
                cleanTempChunks(request);
            }

            return result;
        } catch (Exception e) {
            log.error("合并失败: ", e);
            return R.FAIL("合并失败: " + e.getMessage());
        }
    }

    private R mergeFile(MergeRequest request, boolean isEncrypted) throws Exception {
        int actualChunks = request.getTotalChunks();
        String finalObjectName = request.getFilename();
        String fileHash = request.getFileHash();

        // 获取已上传的分片列表
        List<Integer> uploadedChunks = getUploadedChunks(fileHash);
        if (!IntStream.range(0, actualChunks).allMatch(uploadedChunks::contains)) {
            return R.FAIL("分片缺失");
        }

        // =============== 加密文件专属逻辑 ===============
        SecretKeySpec secretKey = null;
        if (isEncrypted) {
            // 从首分片获取 salt
            StatObjectResponse firstChunkStat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minIOInfo.getTempBucket())
                            .object(fileHash + "/0")
                            .build()
            );
            String saltHex = firstChunkStat.userMetadata().get("salt");
            if (saltHex == null) {
                throw new IllegalArgumentException("加密文件缺失 salt 参数");
            }
            // 派生密钥
            secretKey = encryptionService.deriveKey(fileHash, saltHex);

            // 打印关键参数===========================================
            log.info("Salt Hex: {}", saltHex);
            if (secretKey != null) {
                log.info("Derived Key: {}", Hex.encodeHexString(secretKey.getEncoded()));
            }
        }
        // =============================================

        Path tempFile = Files.createTempFile("merge-", ".tmp");
        try (OutputStream os = Files.newOutputStream(tempFile)) {
            for (int i = 0; i < actualChunks; i++) {
                String objectName = fileHash + "/" + i;
                try (InputStream is = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(minIOInfo.getTempBucket())
                                .object(objectName)
                                .build())) {

                    if (isEncrypted) {
                        // 动态生成分片IV
                        String ivSource = getIvSource(fileHash, i);

                        // Hex 解码
                        char[] ivChars = ivSource.toCharArray();
                        byte[] ivBytes = Hex.decodeHex(ivChars);
                        IvParameterSpec iv = new IvParameterSpec(ivBytes);

                        // 打印 IV 生成过程=========================================
                        log.info("分片 {} - IV Source: {}", i, ivSource);
                        log.info("分片 {} - Decoded IV: {}", i, Hex.encodeHexString(ivBytes));

                        // 解密
                        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
                        try (CipherInputStream cis = new CipherInputStream(is, cipher)) {
                            IOUtils.copy(cis, os);
                        }
                    } else {
                        // 非加密文件直接合并
                        IOUtils.copy(is, os);
                    }
                }
            }
        }

        // 上传最终文件
        String contentType = getContentType(finalObjectName);
        InputStream is = Files.newInputStream(tempFile);
        try (is) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minIOInfo.getBucket())
                    .object(finalObjectName)
                    .stream(is, Files.size(tempFile), -1)
                    .contentType(contentType)
                    .build());
        }

        Files.deleteIfExists(tempFile);
//        String url = "http://192.168.1.11:9000/iams-main-files/" + finalObjectName;
        String url = minIOInfo.getEndpoint() +"/"+ minIOInfo.getBucket() +"/"+ finalObjectName;
        return R.OK(url);
    }

    @NotNull
    private static String getIvSource(String fileHash, int i) {
        String ivSource;
        try {
            if (fileHash == null) {
                throw new IllegalArgumentException("fileHash 不能为 null");
            }
            HashCode hashCode = Hashing.sha256()
                    .hashString(fileHash + i, StandardCharsets.UTF_8);
            String ivInput = fileHash + i;
            log.info("[后端 IV 输入] {}", ivInput); // 记录输入字符串
            ivSource = hashCode.toString().substring(0, 32);
            log.info("[后端 IV 源] {}", ivSource); // 记录生成的 IV 源
        } catch (Exception e) {
            throw new RuntimeException("生成分片 IV 失败", e);
        }
        return ivSource;
    }


    // FileController.java
    private void cleanTempChunks(MergeRequest request) {
        final String fileHash = request.getFileHash();
        final String objectPrefix = fileHash + "/";

        try {
            log.info("开始清理临时分片，文件哈希: {}", fileHash);

            // 1. 列出所有分片对象
            Iterable<Result<Item>> objects = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(minIOInfo.getTempBucket())
                            .prefix(objectPrefix)
                            .recursive(true)
                            .build()
            );

            // 2. 批量删除
            int deletedCount = 0;
            for (Result<Item> itemResult : objects) {
                String objectName = itemResult.get().objectName();
                try {
                    minioClient.removeObject(
                            RemoveObjectArgs.builder()
                                    .bucket(minIOInfo.getTempBucket())
                                    .object(objectName)
                                    .build()
                    );
                    deletedCount++;
//                    log.debug("成功删除分片: {}", objectName);
                } catch (Exception e) {
                    log.error("删除分片失败 [{}]: {}", objectName, e.getMessage());
                }
            }

            log.info("清理完成，共删除{}个临时分片", deletedCount);

        } catch (Exception e) {
            log.error("清理临时分片时发生严重错误: {}", e.getMessage());
        }
    }

    // 辅助方法：获取实际分片数量
    private int getExistedChunkCount(String fileHash) throws Exception {
        int count = 0;
        Iterable<Result<Item>> chunks = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(minIOInfo.getTempBucket())
                        .prefix(fileHash + "/")
                        .recursive(true)
                        .build()
        );
        for (Result<Item> chunk : chunks) {
            count++;
        }
        return count;
    }

    // 辅助方法：获取已上传分片列表
    private List<Integer> getUploadedChunks(String fileHash) throws Exception {
        List<Integer> chunks = new ArrayList<>();
        Iterable<Result<Item>> objects = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(minIOInfo.getTempBucket())
                        .prefix(fileHash + "/")
                        .recursive(true)
                        .build()
        );
        for (Result<Item> itemResult : objects) {
            String[] parts = itemResult.get().objectName().split("/");
            chunks.add(Integer.parseInt(parts[1]));
        }
        return chunks.stream().sorted().collect(Collectors.toList());
    }

    /**
     * 辅助方法：获取 MAIN_BUCKET 中已上传的文件列表（仅文件名）
     *
     * @return 文件名列表
     * @throws Exception 如果 MinIO 操作失败
     */
    @PostMapping("/getUploadedFiles")
    public List<String> getUploadedFiles() throws Exception {
        List<String> files = new ArrayList<>();
        Iterable<Result<Item>> objectResults = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(minIOInfo.getBucket())
                        .build()
        );

        for (Result<Item> result : objectResults) {
            Item item = result.get();
            if (item != null && !item.isDir()) { // 排除目录项
                if (item.objectName().endsWith(".pdf")) {
                    files.add(item.objectName());
                }
            }
        }

        return files;
    }


    // 辅助方法：获取最后一个完整块
    private byte[] getLastCipherBlock(byte[] encryptedData, int blockSize) {
        if (encryptedData == null || encryptedData.length < blockSize) {
            throw new IllegalArgumentException("加密数据不完整");
        }
        return Arrays.copyOfRange(
                encryptedData,
                encryptedData.length - blockSize,
                encryptedData.length
        );
    }

    // 辅助方法：获取文件URL
    private R getFileUrl(String bucketName, String fileName) throws Exception {
        try {
            String  url = (minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .method(Method.GET)
                            .build())
            );
            System.out.println("文件URL: " + url);
            return R.OK(url);
        } catch (Exception e) {
            log.error("文件不存在: {}", fileName);
            return R.FAIL("文件不存在");
        }
    }

    // 定义常见后缀名到 MIME 类型的映射
    private static final Map<String, String> MIME_TYPES = new HashMap<>();
    static {
        MIME_TYPES.put("txt", "text/plain");
        MIME_TYPES.put("html", "text/html");
        MIME_TYPES.put("css", "text/css");
        MIME_TYPES.put("js", "application/javascript");
        MIME_TYPES.put("json", "application/json");
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("pdf", "application/pdf");
        MIME_TYPES.put("doc", "application/msword");
        MIME_TYPES.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        MIME_TYPES.put("xls", "application/vnd.ms-excel");
        MIME_TYPES.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        MIME_TYPES.put("zip", "application/zip");
        MIME_TYPES.put("mp3", "audio/mpeg");
        MIME_TYPES.put("mp4", "video/mp4");
        // 可继续扩展其他类型
    }

    // 根据文件名获取 MIME 类型
    private String getContentType(String filename) {
        // 获取文件后缀名（如 "jpg"）
        String extension = "";
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex > 0) {
            extension = filename.substring(dotIndex + 1).toLowerCase();
        }
        // 从映射表中获取 MIME 类型，默认为 octet-stream
        return MIME_TYPES.getOrDefault(extension, "application/octet-stream");
    }

    // 删除文件
    @DeleteMapping("/delete")
    public R deleteFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return R.FAIL("文件名不能为空");
        }
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minIOInfo.getBucket())
                    .object(fileName)
                    .build());
            return R.OK();
        } catch (Exception e) {
            log.error("删除文件失败", e);
            return R.FAIL("删除文件失败");
        }
    }

    /**
     * 生成唯一的MinIO文件名
     * @param originalFileName 原始文件名
     * @param archiveId 文件所属档案的ID
     * @param projectId 文件所属项目的ID
     * @return 唯一的文件名
     */
    private String generateUniqueFileName(String originalFileName, String archiveId, String projectId) {
        // 生成13位时间戳
        long timestamp = System.currentTimeMillis();
        // 生成6位随机数
        int randomNum = (int) (Math.random() * 999999);
        String randomStr = String.format("%06d", randomNum);

        // 获取文件扩展名
        String extension = "";
        String nameWithoutExt = originalFileName;
        int lastDotIndex = originalFileName.lastIndexOf('.');
        if (lastDotIndex > -1) {
            extension = originalFileName.substring(lastDotIndex);
            nameWithoutExt = originalFileName.substring(0, lastDotIndex);
        }
        // 文件名：时间戳_随机数.扩展名
        String newFileName = timestamp + "_" + randomStr + extension;
        if (!archiveId.isEmpty())
            newFileName = archiveId + "_" + newFileName;
        if (!projectId.isEmpty())
            newFileName = projectId + "_" + newFileName;
        // 最终文件名：项目ID_档案ID_时间戳_随机数.扩展名
        return newFileName;
    }

    /**
     * 第三方文件上传接口
     */
    @PostMapping("/ocrUpload")
    public R ocrUpload(@RequestParam("file") MultipartFile file,
                       @RequestParam("name") String name,
                       @RequestParam("content") String content,
                       @RequestParam("archiveId") String archiveIdStr,
                       @RequestParam("projectId") String projectIdStr,
                       @RequestParam("category") String categoryCode
    ) {

        log.info("第三方文件上传 - file: {}, name: {}, content length: {}, archiveId: {}",
                file != null ? file.getOriginalFilename() : "null",
                name,
                content != null ? content.length() : 0,
                archiveIdStr);

        // 参数验证
        Long archiveId = null;
        Long projectId = null;
        try {
            archiveId = Long.parseLong(archiveIdStr);
            projectId = Long.parseLong(projectIdStr);
        } catch (NumberFormatException e) {
            return R.FAIL("Invalid archiveId or projectId format");
        }

        if (file == null || file.isEmpty()) {
            return R.FAIL("文件内容不能为空");
        }
        if (name == null || name.isEmpty()) {
            return R.FAIL("文件名不能为空");
        }
        if (categoryCode == null || categoryCode.isEmpty())
            return R.FAIL("文件分类不能为空");

        try {
            // 直接上传新文件，不做重复性检查
            log.info("上传新文件: archiveId={}, name={}", archiveId, name);

            // 生成唯一的MinIO文件名
            String minioFileName = generateUniqueFileName(name, archiveIdStr, projectIdStr);
            log.info("生成MinIO文件名: {} -> {}", name, minioFileName);

            // 上传到 MinIO（使用唯一文件名）
            String contentType = getContentType(name);
            PutObjectArgs putArgs = PutObjectArgs.builder()
                    .bucket(minIOInfo.getBucket())
                    .object(minioFileName)  // 使用唯一文件名
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(contentType)
                    .build();

            minioClient.putObject(putArgs);
            log.info("文件已上传到MinIO: {}", minioFileName);

            // 获取预签名 URL（使用MinIO文件名）
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(minIOInfo.getBucket())
                            .object(minioFileName)  // 使用唯一文件名
                            .method(Method.GET)
                            .build());

            // 构造 Document 对象并保存到数据库
            Document document = new Document();
            document.setName(name);  // 保存原始文件名
            document.setMinioName(minioFileName);  // 保存MinIO中的唯一文件名
            document.setFileLocation(minIOInfo.getBucket());  // 保存MinIO中的Bucket名称
            document.setFilePath(url);  // MinIO返回的URL
            document.setFileType(getFileTypeFromName(name));
            document.setFileSize(file.getSize());
            document.setContent(content);
            document.setArchiveId(archiveId);
            document.setCategoryCode(categoryCode);
            document.setCreateTime(new Date());
            document.setUpdateTime(new Date());

            documentService.insertDocument(document);
            log.info("文档记录已保存到数据库: id={}, name={}, minioName={}",
                    document.getId(), document.getName(), document.getMinioName());

            return R.OK("文件上传成功");

        } catch (Exception e) {
            log.error("上传文件失败: archiveId={}, name={}", archiveId, name, e);
            return R.FAIL("上传失败：" + e.getMessage());
        }
    }

    /**
     * 从文件名获取文件类型
     * @param fileName 文件名
     * @return 文件类型
     */
    private String getFileTypeFromName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "unknown";
        }

        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > -1 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }

        return "unknown";
    }

    @PostMapping("/uploadProjectCertificate")
    public R uploadProjectCertificate(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long projectId,
            @RequestParam String originalFileName) throws Exception {

        if (file.isEmpty()) {
            return R.FAIL("上传文件不能为空");
        }

        if (originalFileName != null) {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minIOInfo.getCertBucket())
                    .object(originalFileName)
                    .build());;
        }

        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = null;
            if (originalFilename != null) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String certificateName = "project_" + projectId + "_" + System.currentTimeMillis() + extension;

            // 上传到MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minIOInfo.getCertBucket())
                            .object(certificateName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType("application/pdf")
                            .build()
            );

//            String url = minioClient.getPresignedObjectUrl(
//                    GetPresignedObjectUrlArgs.builder()
//                            .bucket(minIOInfo.getCertBucket())
//                            .object(certificateName)
//                            .method(Method.GET)
//                            .build());

//            String url = "http:/192.168.1.11:9000/iams-project-cert/" + certificateName;
            String url = minIOInfo.getEndpoint() + "/" + minIOInfo.getCertBucket() + "/" + certificateName;
            Project project = projectService.selectProjectById(projectId);
            project.setCertUrl(url);
            project.setHasCertificate(true);
            projectService.updateProject(project);
            // TODO: 更新项目表，设置has_certificate为true（根据实际数据库设计）
            return R.OK(url); // 返回证书文件名

        } catch (Exception e) {
            log.error("项目证明书上传失败", e);
            return R.FAIL("上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/getProjectCertificateUrl")
    public R getProjectCertificateUrl(
            @RequestParam Long projectId,
            @RequestParam(required = false) String certificateName) {

        try {
            // 根据文件名或者项目ID查询证明书

            if (certificateName == null || certificateName.isEmpty()) {
                return R.FAIL("项目证明书不存在");
            }

            // 生成预签名URL（7天有效期）
//            String url = minioClient.getPresignedObjectUrl(
//                    GetPresignedObjectUrlArgs.builder()
//                            .bucket(minIOInfo.getCertBucket())
//                            .object(fileName)
//                            .method(Method.GET)
//                            .build());

//            String url = "http:/192.168.1.11:9000/iams-project-cert/" + certificateName;
            String url = minIOInfo.getEndpoint() + "/" + minIOInfo.getCertBucket() + "/" + certificateName;
            return R.OK(url);

        } catch (Exception e) {
            log.error("获取证明书失败", e);
            return R.FAIL("获取失败: " + e.getMessage());
        }
    }
    @GetMapping("/download")
    public void downloadFile(HttpServletResponse response) {

        String bucketName = "template";
        String fileName = "文档导入模板，上传前记得在这更新成您的项目名.zip";
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build())) {

            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

            // 流式传输文件内容
            IOUtils.copy(stream, response.getOutputStream());
            response.flushBuffer();

        } catch (Exception e) {
            throw new RuntimeException("下载失败", e);
        }
    }


    // 新增Excel档案模板下载接口
    @GetMapping("/download/archiveTemplate")
    public void downloadArchiveTemplate(HttpServletResponse response) {
        String bucketName = "template";
        String fileName = "档案添加模板.xlsx";

        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build())) {

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

            // 流式传输文件内容
            IOUtils.copy(stream, response.getOutputStream());
            response.flushBuffer();

        } catch (Exception e) {
            throw new RuntimeException("档案模板下载失败", e);
        }
    }
}

