package com.iams.web.controller.common;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.nio.file.Paths;

import com.alibaba.fastjson2.JSON;
import com.iams.manage.domain.Archive;
import com.iams.manage.domain.JsonParser;
import com.iams.manage.service.IArchiveService;
import com.iams.manage.service.IFondsService;
import com.iams.manage.service.impl.CryptoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.iams.common.config.RuoYiConfig;
import com.iams.common.constant.Constants;
import com.iams.common.core.domain.AjaxResult;
import com.iams.common.utils.StringUtils;
import com.iams.common.utils.file.FileUploadUtils;
import com.iams.common.utils.file.FileUtils;
import com.iams.framework.config.ServerConfig;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 通用请求处理
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/common")
public class CommonController
{
    private static final Logger log = LoggerFactory.getLogger(CommonController.class);

    @Autowired
    private ServerConfig serverConfig;

    @Autowired
    private FileStorageService fileStorageService;//注入实列

    @Autowired
    private IArchiveService archiveService;

    @Autowired
    private IFondsService fondsService;




    private static final String FILE_DELIMETER = ",";

    private final Environment environment;

    // 构造函数注入Environment
    public CommonController(Environment environment) {
        this.environment = environment;
    }

    /**
     * 通用下载请求
     * 
     * @param fileName 文件名称
     * @param delete 是否删除
     */
    @GetMapping("/download")
    public void fileDownload(String fileName, Boolean delete, HttpServletResponse response, HttpServletRequest request)
    {
        try
        {
            if (!FileUtils.checkAllowDownload(fileName))
            {
                throw new Exception(StringUtils.format("文件名称({})非法，不允许下载。 ", fileName));
            }
            String realFileName = System.currentTimeMillis() + fileName.substring(fileName.indexOf("_") + 1);
            String filePath = RuoYiConfig.getDownloadPath() + fileName;

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, realFileName);
            FileUtils.writeBytes(filePath, response.getOutputStream());
            if (delete)
            {
                FileUtils.deleteFile(filePath);
            }
        }
        catch (Exception e)
        {
            log.error("下载文件失败", e);
        }
    }

    /**
     * 加密上传
     */
    @PostMapping("/secure/upload")
    public AjaxResult uploadFile(@RequestParam("file") MultipartFile file,
                                 @RequestParam("iv") String ivBase64,
                                 @RequestParam("password") String password,
                                 @RequestParam(value = "archiveId", required = false) Long archiveId,
                                 @RequestParam(value = "directory", required = false) Long directory) throws Exception
    {
        try {
            // 1. 解码IV
            byte[] iv = Base64.getDecoder().decode(ivBase64);

            // 2. 派生密钥（关键步骤）
            SecretKey key = CryptoService.deriveKey(password, iv);

            // 3. 初始化解密器
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));

            // 4. 解密文件流
            try (InputStream cipherStream = new CipherInputStream(file.getInputStream(), cipher)) {
                // 存储解密后的文件
                FileInfo fileInfo = fileStorageService.of(cipherStream)
                        .setPath(buildObjectName(archiveId, directory))
                        .upload();

                AjaxResult ajax = AjaxResult.success();
                ajax.put("url", fileInfo.getUrl());
                ajax.put("fileName", fileInfo.getUrl());
                ajax.put("newFileName", fileInfo.getUrl());
                ajax.put("originalFilename", file.getOriginalFilename());
                return ajax;
            }
        } catch (Exception e) {
            log.error("文件解密失败", e);
            return AjaxResult.error("文件处理失败");
        }
    }

    /**
     * 通用上传请求（单个）
     */
    @PostMapping("/upload")
    public AjaxResult uploadFile(MultipartFile file,
                                 @RequestParam(value = "archiveId", required = false) Long archiveId,
                                 @RequestParam(value = "directory", required = false) Long directory) throws Exception
    {
        try
        {
            /*// 上传文件路径
            String filePath = RuoYiConfig.getUploadPath();
            // 上传并返回新文件名称
            String fileName = FileUploadUtils.upload(filePath, file);
            String url = serverConfig.getUrl() + fileName;*/

            String objectName = buildObjectName(archiveId, directory);

            // 上传图片，返回文件信息
            FileInfo fileInfo = fileStorageService.of(file)
                    .setPath(objectName)
                    .upload();
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", fileInfo.getUrl());
            ajax.put("fileName", fileInfo.getUrl()); // 注意：这里的值需要改为URL，因为前端的访问地址会做一个判断，如果一http开头就直接显示此图片
            ajax.put("newFileName", fileInfo.getUrl());
            ajax.put("originalFilename", file.getOriginalFilename());
            return ajax;
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 通用上传请求（多个）
     */
    @PostMapping("/uploads")
    public AjaxResult uploadFiles(List<MultipartFile> files) throws Exception
    {
        try
        {
            // 上传文件路径
            String filePath = RuoYiConfig.getUploadPath();
            List<String> urls = new ArrayList<String>();
            List<String> fileNames = new ArrayList<String>();
            List<String> newFileNames = new ArrayList<String>();
            List<String> originalFilenames = new ArrayList<String>();
            for (MultipartFile file : files)
            {
                // 上传并返回新文件名称
                String fileName = FileUploadUtils.upload(filePath, file);
                String url = serverConfig.getUrl() + fileName;
                urls.add(url);
                fileNames.add(fileName);
                newFileNames.add(FileUtils.getName(fileName));
                originalFilenames.add(file.getOriginalFilename());
            }
            AjaxResult ajax = AjaxResult.success();
            ajax.put("urls", StringUtils.join(urls, FILE_DELIMETER));
            ajax.put("fileNames", StringUtils.join(fileNames, FILE_DELIMETER));
            ajax.put("newFileNames", StringUtils.join(newFileNames, FILE_DELIMETER));
            ajax.put("originalFilenames", StringUtils.join(originalFilenames, FILE_DELIMETER));
            return ajax;
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 本地资源通用下载
     */
    @GetMapping("/download/resource")
    public void resourceDownload(String resource, HttpServletRequest request, HttpServletResponse response)
            throws Exception
    {
        try
        {
            if (!FileUtils.checkAllowDownload(resource))
            {
                throw new Exception(StringUtils.format("资源文件({})非法，不允许下载。 ", resource));
            }
            // 本地资源路径
            String localPath = RuoYiConfig.getProfile();
            // 数据库资源地址
            String downloadPath = localPath + StringUtils.substringAfter(resource, Constants.RESOURCE_PREFIX);
            // 下载名称
            String downloadName = StringUtils.substringAfterLast(downloadPath, "/");
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, downloadName);
            FileUtils.writeBytes(downloadPath, response.getOutputStream());
        }
        catch (Exception e)
        {
            log.error("下载文件失败", e);
        }
    }

    private SecretKeySpec loadEncryptionKey() {
        // 1. 从配置获取Base64编码的密钥
        String base64Key = environment.getProperty("app.encryption.key");

        // 2. 检查密钥是否存在
        if (StringUtils.isBlank(base64Key)) {
            throw new IllegalStateException("未配置加密密钥：app.encryption.key");
        }

        // 3. Base64解码
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(base64Key);
        } catch (IllegalArgumentException e) {
            throw new SecurityException("加密密钥格式错误，必须是有效的Base64字符串", e);
        }

        // 4. 验证密钥长度（AES-256需要32字节）
        if (keyBytes.length != 32) {
            throw new SecurityException("加密密钥长度必须为32字节（256位），当前长度：" + keyBytes.length + "字节");
        }

        // 5. 创建密钥对象
        return new SecretKeySpec(keyBytes, "AES");
    }

    private String buildObjectName(Long archiveId, Long directory) {
        // 保持你原有的路径构建逻辑
        try {
            String objectName = "";
            // 如果archiveId存在
            if (archiveId != null)
            {
                Archive archive = archiveService.selectArchiveById(archiveId);
                Long fondsId = archive.getFondsId();
                // 得到全宗名称
                String fondsName = fondsService.selectFondsById(fondsId).getName();
//                Long categoryId = archiveService.selectArchiveById(archiveId).getCategoryId();
                // 得到门类名称
                String categoryName = archive.getCategoryId();
                // 如果directory存在
                if (directory != null)
                {
                    // 得到目录名称
                    String label = JsonParser.getLabelById(archive.getDirectory(), directory);
                    // 指定oss保存文件路径 全宗名称/门类名称/文件名
                    objectName = fondsName + "/" + categoryName + "/" + label + "/";
                }else {
                    objectName = fondsName + "/" + categoryName + "/";
                }
            }
            return objectName;
        }
        catch (Exception e)
        {
            return e.getMessage();
        }
    }
}
