package com.iams.manage.service.impl;

import java.text.SimpleDateFormat;
import java.util.*;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.iams.common.exception.ServiceException;
import com.iams.common.utils.DateUtils;
import com.iams.common.utils.StringUtils;
import com.iams.elasticsearch.domain.ArchiveDocument;
import com.iams.elasticsearch.domain.ElasticsearchArchive;
import com.iams.elasticsearch.repository.ArchiveRepository;
import com.iams.manage.domain.*;
import com.iams.manage.mapper.ArchiveMapper;
import com.iams.minio.config.MinIOInfo;
import com.iams.minio.result.R;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import jakarta.annotation.Resource;
import com.iams.manage.service.ArchiveSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.ScriptType;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Service;
import com.iams.manage.mapper.DocumentMapper;
import com.iams.manage.service.IDocumentService;
import org.springframework.transaction.annotation.Transactional;

import javax.print.Doc;
import java.io.File;
import java.util.stream.Collectors;

import static com.iams.framework.datasource.DynamicDataSourceContextHolder.log;

/**
 * 文档信息Service业务层处理
 * 
 * @author zhjm
 * @date 2025-01-10
 */
@Service
public class DocumentServiceImpl implements IDocumentService
{
    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations; // 替换或补充原有的 archiveRepository

    @Autowired
    private ArchiveMapper archiveMapper; // 新增：查询档案表

    @Autowired
    private ArchiveRepository archiveRepository;


    @Autowired
    private ArchiveCategoryServiceImpl archiveCategoryService;

    @Resource
    private MinIOInfo minIOInfo;

    @Value("${ruoyi.profile}")
    private String profile;

//    private final String TEMP_BUCKET = "iams-tmp-files";
//    private final String MAIN_BUCKET = "iams-main-files";
    // 添加新的服务依赖
    private final ArchiveSyncService archiveSyncService;

    // 构造函数注入（如果使用构造器注入）
    public DocumentServiceImpl(ArchiveSyncService archiveSyncService) {
        // ...
        this.archiveSyncService = archiveSyncService;
    }

    @Resource
    private MinioClient minioClient;

    /**
     * 查询文档信息
     * 
     * @param id 文档信息主键
     * @return 文档信息
     */
    @Override
    public Document selectDocumentById(Long id) {
        Document document = documentMapper.selectDocumentById(id);

        // 检查ID是否为0
        if (document.getCategoryCode()==null) {
            document.setCategoryName("未指定门类");
        } else {
            fileInfo(document);
        }
        return document;
    }

    /**
     * 查询文档信息列表
     *
     * @param document 文档信息
     * @return 文档信息
     */
    @Override
    public List<Document> selectDocumentList(Document document) {
        List<Document> list = documentMapper.selectDocumentList(document);

        // 直接操作原集合（保持Page类型）
        for (Document doc : list) {
            if (doc.getCategoryCode() == null) {
                doc.setCategoryName("未指定门类");
            } else {
                fileInfo(doc);
            }
        }

        return list; // 保持Page类型
    }


    private void fileInfo(Document document){

        // 先拿到档案的父类的大类信息
        Archive archive = archiveMapper.selectArchiveById(document.getArchiveId());
        String[] parts = archive.getCategoryId().split(",");

        // 解析大类的字段
        String[] codeName = parts[0].split(":");
        String code = codeName[0]; // 取出code
        String name = codeName[1]; // 取出name

        // 查询大类root的ID
        Long rootId = archiveCategoryService.getCategoryRootIdByCodeAndName(code, name);

        // 设定大类信息
        ArchiveCategory archiveCategory = new ArchiveCategory();
        archiveCategory.setRootId(rootId);

        String[] documentCodeParts = document.getCategoryCode().split(",");
        for (String documentCode : documentCodeParts) {
            archiveCategory.setCode(documentCode);

            if(document.getCategoryName() == null)
                document.setCategoryName(archiveCategoryService.selectCategoryList(archiveCategory).get(0).getName());
            else
                document.setCategoryName(document.getCategoryName() + "、" + archiveCategoryService.selectCategoryList(archiveCategory).get(0).getName());
        }

    }

    /**
     * 查询文档信息列表
     *
     * @param ids 需要查询的文档信息主键
     * @return 文档信息
     */
    @Override
    public List<Document> selectDocumentListByIds(Long[] ids)
    {
        return documentMapper.selectDocumentListByIds(ids).stream()
                .peek(doc -> {
                    if (doc.getCategoryCode() == null) {
                        doc.setCategoryName("未指定门类");
                    } else {
                        fileInfo(doc);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 新增文档信息
     * 
     * @param document 文档信息
     * @return 结果
     */
    @Override
    @Transactional
    public Long insertDocument(Document document)
    {
        // 1. 设置文档的创建时间
//        document.setCreateTime(DateUtils.getNowDate());
        Date createTime = DateUtils.getNowDate();   // 假设该方法返回当前时间字符串，比如 "2025-04-02 14:30:00"
        document.setCreateTime(createTime);
        document.setUpdateTime(createTime);

        // 使用 SimpleDateFormat 将 Date 转换为只包含年月日的字符串
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = dateFormat.format(createTime);  // 转换后的日期格式 "2025-04-02"

        // 2. 插入文档到数据库
        int rows = documentMapper.insertDocument(document);

        // 3. 获取文档所属的档案ID，假设文档对象中已经有 archiveId 字段
        Long archiveId = document.getArchiveId();

        // 4. 根据档案ID查询对应的档案信息
        Archive archive = archiveMapper.selectArchiveById(archiveId);

        if (archive != null) {
            // 5. 查询ES中是否存在该档案的索引
            Optional<ElasticsearchArchive> optional = archiveRepository.findById(archive.getDanghao());

            if (optional.isPresent()) {
                // 6. 获取ES中的档案数据
                ElasticsearchArchive esArchive = optional.get();

                // 7. 构建一个 ArchiveDocument 对象，用来封装文档数据
                ArchiveDocument archiveDocument = new ArchiveDocument();
                archiveDocument.setDocId(document.getId().toString());
                archiveDocument.setDocType(document.getFileType());
                archiveDocument.setDocTitle(document.getName());
//                archiveDocument.setCreateTime(document.getCreateTime());
                archiveDocument.setFilePath(document.getFilePath());
                archiveDocument.setDocContent(document.getContent()); // 文档内容同步

                archiveDocument.setTitle(document.getTitle());
                archiveDocument.setNumber(document.getNumber());
                archiveDocument.setUnit(document.getUnit());

                // 8. 将该文档添加到档案的 documents 列表中
                if (esArchive.getDocuments() == null) {
                    esArchive.setDocuments(new ArrayList<>());
                }
                esArchive.getDocuments().add(archiveDocument);

                // 9. 保存更新的档案到 Elasticsearch
                archiveRepository.save(esArchive);
            }
        }

        // 10. 返回文档的ID
        return document.getId();
    }
    /**
     * 修改文档信息
     * 
     * @param document 文档信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateDocument(Document document) {
        // 1. 更新MySQL
        document.setUpdateTime(DateUtils.getNowDate());
        int rows = documentMapper.updateDocument(document);

        // 2. 同步到Elasticsearch
        Archive archive = archiveMapper.selectArchiveById(document.getArchiveId());
        if (archive != null) {
            updateOrReplaceEsDocument(
                    archive.getDanghao(),
                    document.getId().toString(),
                    document.getName(),
                    document.getFileType(),
                    document.getFilePath(),
                    document.getContent(),
                    document.getTitle(),
                    document.getNumber(),
                    document.getUnit()
            );
        }
        return rows;
    }

    private void updateOrReplaceEsDocument(String archiveId, String docId,
                                           String title, String type,
                                           String path, String content,
                                           String docTitle, String number, String unit) {
        // 1. 查询ES档案
        Optional<ElasticsearchArchive> optional = archiveRepository.findById(archiveId);
        if (!optional.isPresent()) {
            return; // 档案不存在则不处理
        }

        // 2. 获取现有文档列表
        ElasticsearchArchive esArchive = optional.get();
        List<ArchiveDocument> documents = esArchive.getDocuments() != null ?
                new ArrayList<>(esArchive.getDocuments()) : new ArrayList<>();

        // 3. 删除旧文档（如果存在）
        documents.removeIf(doc -> doc.getDocId().equals(docId));

        // 4. 添加新文档
        ArchiveDocument newDoc = new ArchiveDocument();
        newDoc.setDocId(docId);
        newDoc.setDocTitle(title);
        newDoc.setDocType(type);
        newDoc.setFilePath(path);
        newDoc.setDocContent(content);

        newDoc.setTitle(docTitle);
        newDoc.setNumber(number);
        newDoc.setUnit(unit);
        documents.add(newDoc);

        // 5. 更新ES档案
        esArchive.setDocuments(documents);
        archiveRepository.save(esArchive);
    }

    /**
     * 批量删除文档信息 - 修复版本
     *
     * @param ids 需要删除的文档信息主键
     * @return 结果
     */
    @Override
    public int deleteDocumentByIds(Long[] ids) {
        // 1. 查询文档列表
        List<Document> documents = new ArrayList<>();
        for (Long id : ids) {
            Document document = documentMapper.selectDocumentById(id);
            if (document != null) {
                String fileName = document.getMinioName();
                R r = deleteFile(fileName);
                documents.add(document);
                log.debug("准备删除文档 | ID={} | 档案ID={} | 文件路径={}",
                        document.getId(), document.getArchiveId(), document.getFilePath());
            } else {
                log.warn("未找到文档 | ID={}", id);
            }
        }

        // 2. 删除数据库记录
        int dbResult = documentMapper.deleteDocumentByIds(
                documents.stream().map(Document::getId).toArray(Long[]::new)
        );
        log.warn("数据库删除结果 | 期望删除数={} | 实际删除数={}", documents.size(), dbResult);

        // 3. ES文档删除 - 使用最稳定的方案
        if (dbResult > 0 && !documents.isEmpty()) {
            updateElasticsearchAfterDelete(documents);
        }
        return dbResult;
    }

    /**
     * 删除后更新 Elasticsearch - 使用最稳定的查询-修改-保存方式
     */
    private void updateElasticsearchAfterDelete(List<Document> documents) {
        // 按档案ID分组
        Map<Long, List<Document>> docsByArchiveId = documents.stream()
                .filter(doc -> doc.getArchiveId() != null)
                .collect(Collectors.groupingBy(Document::getArchiveId));

        // 为每个档案处理文档删除
        docsByArchiveId.forEach((archiveId, docs) -> {
            try {
                // 1. 查询档案信息获取档号
                Archive archive = archiveMapper.selectArchiveById(archiveId);
                if (archive == null) {
                    log.warn("未找到档案信息 | 档案ID={}", archiveId);
                    return;
                }

                String danghao = archive.getDanghao();

                // 2. 收集要删除的文档ID
                List<String> docIdsToDelete = docs.stream()
                        .map(doc -> doc.getId().toString())
                        .collect(Collectors.toList());

                // 3. 从ES中删除文档（使用传统方式，最稳定）
                deleteDocumentsFromElasticsearch(danghao, docIdsToDelete);

                log.info("ES文档删除成功 | 档号={} | 删除文档数={}", danghao, docIdsToDelete.size());

            } catch (Exception e) {
                log.error("ES删除失败 | 档案ID={}", archiveId, e);
                throw new RuntimeException("ES同步失败", e);
            }
        });
    }

    /**
     * 从 Elasticsearch 中删除文档 - 传统稳定方式
     */
    private void deleteDocumentsFromElasticsearch(String danghao, List<String> docIdsToDelete) {
        try {
            // 1. 根据档号查询ES中的档案
            Optional<ElasticsearchArchive> optionalArchive = archiveRepository.findById(danghao);

            if (!optionalArchive.isPresent()) {
                log.warn("ES中未找到档案 | 档号={}", danghao);
                return; // 档案不存在，视为删除成功
            }

            ElasticsearchArchive esArchive = optionalArchive.get();

            // 2. 检查档案是否有文档
            if (esArchive.getDocuments() == null || esArchive.getDocuments().isEmpty()) {
                log.warn("档案中没有文档 | 档号={}", danghao);
                return; // 没有文档，视为删除成功
            }

            // 3. 记录删除前的文档数量
            int beforeCount = esArchive.getDocuments().size();

            // 4. 过滤掉要删除的文档
            List<ArchiveDocument> remainingDocuments = esArchive.getDocuments().stream()
                    .filter(doc -> !docIdsToDelete.contains(doc.getDocId()))
                    .collect(Collectors.toList());

            // 5. 更新文档列表
            esArchive.setDocuments(remainingDocuments);

            // 6. 保存更新后的档案
            archiveRepository.save(esArchive);

            int afterCount = remainingDocuments.size();
            log.debug("文档删除详情 | 档号={} | 删除前={} | 删除后={} | 实际删除={}",
                    danghao, beforeCount, afterCount, beforeCount - afterCount);

        } catch (Exception e) {
            log.error("ES删除失败 | 档号={} | 错误: {}", danghao, e.getMessage(), e);
            throw new RuntimeException("ES删除失败: " + e.getMessage(), e);
        }
    }



    /**
     * 删除文档信息信息
     *
     * @param id 文档信息主键
     * @return 结果
     */
    @Override
    public int deleteDocumentById(Long id)
    {
        Document document = documentMapper.selectDocumentById(id);
        String fileName = document.getMinioName();
        R r = deleteFile(fileName);
        return documentMapper.deleteDocumentById(id);
    }

    /**
     * 查询文档最大序号
     *
     * @param id 文档信息主键
     * @return 最大序号
     */
    @Override
    public Long selectMaxXuhaoByArchiveId(Long id)
    {
        return documentMapper.selectMaxXuhaoByArchiveId(id);
    }

    /**
     * 修改文档额外信息
     *
     * @param extraInfo 文档信息主键
     * @return 结果
     */
    @Override
    public int updateDocExtraInfo(ExtraInfo extraInfo) {
        ObjectMapper objectMapper = new ObjectMapper();

        // 获取文档信息
        Document document = documentMapper.selectDocumentById(extraInfo.getId());
        if (document == null) {
            throw new IllegalArgumentException("文档不存在，ID: " + extraInfo.getId());
        }

        // 获取现有的额外信息
        String docExtraInfo = document.getExtraInfo();

        try {
            ArrayNode arrayNode;
            if (docExtraInfo == null || docExtraInfo.trim().isEmpty()) {
                // 创建一个新的JSON数组
                arrayNode = objectMapper.createArrayNode();
            } else {
                // 解析现有的JSON字符串为JsonNode
                JsonNode jsonNode = objectMapper.readTree(docExtraInfo);

                // 检查是否是ArrayNode
                if (jsonNode instanceof ArrayNode) {
                    arrayNode = (ArrayNode) jsonNode;
                } else if (jsonNode instanceof ObjectNode) {
                    // 如果是ObjectNode，将其转换为ArrayNode
                    arrayNode = objectMapper.createArrayNode();
                    arrayNode.add(jsonNode);
                } else {
                    // 如果既不是ObjectNode也不是ArrayNode，可以抛出异常或处理其他情况
                    throw new IllegalArgumentException("Unsupported JSON node type: " + jsonNode.getNodeType());
                }
            }

            // 检查是否已经存在具有相同 label 的 ObjectNode
            boolean updated = false;
            for (JsonNode node : arrayNode) {
                if (node instanceof ObjectNode) {
                    ObjectNode objectNode = (ObjectNode) node;
                    if (objectNode.has("label") && objectNode.get("label").asText().equals(extraInfo.getLabel())) {
                        // 更新 value
                        objectNode.put("value", extraInfo.getValue());
                        updated = true;
                        break;
                    }
                }
            }

            // 如果没有找到具有相同 label 的 ObjectNode，则添加新的 ObjectNode
            if (!updated) {
                ObjectNode newObjectNode = objectMapper.createObjectNode();
                newObjectNode.put("label", extraInfo.getLabel());
                newObjectNode.put("value", extraInfo.getValue());
                arrayNode.add(newObjectNode);
            }

            docExtraInfo = arrayNode.toString();
            System.out.println(docExtraInfo);
            document.setExtraInfo(docExtraInfo);

            // 更新数据库中的文档信息
            return documentMapper.updateDocExtraInfo(document);
        } catch (JsonProcessingException e) {
            // 记录异常日志
            e.printStackTrace();
            throw new RuntimeException("解析或生成 JSON 时出错：" + e.getMessage(), e);
        } catch (Exception e) {
            // 记录其他异常日志
            e.printStackTrace();
            throw new RuntimeException("更新文档额外信息失败：" + e.getMessage(), e);
        }
    }

    @Override
    public List<Document> selectListByArchiveId(Long archiveId) {
        return documentMapper.selectListByArchiveId(archiveId);
    }

    @Override
    public Document selectDocumentByArchiveIdAndName(Long archiveId, String name) {
        return documentMapper.selectDocumentByArchiveIdAndName(archiveId, name);
    }

    public void FileDeletion(String filePath) {
        // 指定要删除的文件路径
        File file = new File(filePath);

        // 检查文件是否存在
        if (file.exists()) {
            // 尝试删除文件
            boolean isDeleted = file.delete();
            if (isDeleted) {
                System.out.println("文件已成功删除: " + filePath);
            } else {
                System.out.println("文件删除失败: " + filePath);
            }
        } else {
            System.out.println("文件不存在: " + filePath);
        }
    }

    public static String extractProfilePath(String url) {
        // 定义要查找的前缀
        String prefix = "profile/";

        // 找到前缀在URL中的位置
        int prefixIndex = url.indexOf(prefix);

        if (prefixIndex == -1) {
            throw new IllegalArgumentException("URL does not contain the prefix 'profile/'");
        }

        // 计算前缀结束的位置
        int startIndex = prefixIndex + prefix.length();

        // 截取前缀后面的部分
        return url.substring(startIndex);
    }

    public R deleteFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return R.FAIL("文件名不能为空");
        }
        try {
            System.out.println("minIOInfo.getBucket(): "+minIOInfo.getBucket());
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
     * 批量更新文档门类
     *
     * @param dto 批量更新参数
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateDocumentCategory(BatchUpdateCategoryDto dto)
    {
        if (dto.getDocIds() == null || dto.getDocIds().isEmpty())
        {
            throw new ServiceException("请选择要更新的文档");
        }

        if (StringUtils.isEmpty(dto.getCategoryCode()))
        {
            throw new ServiceException("门类代码不能为空");
        }
        return documentMapper.batchUpdateCategory(dto.getDocIds(),
                dto.getCategoryCode(),
                dto.getCategoryName());
    }

    @Override
    public List<Document> selectMinioNameList() {
        return documentMapper.selectMinioNameList();
    }

    public List<Document> selectOcredDocumentList(Document document) {
        return documentMapper.selectOcredDocumentList(document);
    }

    public List<Document> selectNoOcrDocumentList(Document document) {
        return documentMapper.selectNoOcrDocumentList(document);
    }

    public List<Document> selectByProjectId(Long projectId) {
        return documentMapper.selectByProjectId(projectId);
    }

    public int deleteDocumentByArchiveId(Long archiveId) {
        return documentMapper.deleteDocumentByArchiveId(archiveId);
    }
}
