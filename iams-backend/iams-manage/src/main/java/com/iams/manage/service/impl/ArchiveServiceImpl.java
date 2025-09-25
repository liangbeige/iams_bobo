package com.iams.manage.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.iams.common.exception.ServiceException;
import com.iams.common.utils.SecurityUtils;
import com.iams.common.utils.file.FileUploadUtils;
import com.iams.elasticsearch.domain.ElasticsearchArchive;
import com.iams.elasticsearch.repository.ArchiveRepository;
import com.iams.elasticsearch.service.impl.DocumentContentExtractor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iams.common.utils.DateUtils;
import com.iams.manage.domain.*;
import com.iams.manage.domain.InventoryDTO.InventoryResultDTO;
import com.iams.manage.domain.workflow.BorrowRecord;
import com.iams.manage.domain.workflow.dto.BorrowRecordDTO;
import com.iams.manage.mapper.*;
import com.iams.manage.service.IBorrowRecordService;
import com.iams.manage.service.IFondsService;
import com.iams.manage.service.IProjectService;
import com.iams.minio.config.MinIOInfo;
import com.iams.system.service.ISysPostService;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.iams.manage.service.IArchiveService;
import org.springframework.transaction.annotation.Transactional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.iams.manage.domain.Archive;
import com.iams.manage.mapper.ArchiveMapper;
import com.iams.common.utils.file.FileUploadUtils;
import com.iams.common.config.RuoYiConfig; // 假设您有用于上传路径的配置
import com.iams.common.utils.StringUtils;


import com.iams.common.config.RuoYiConfig;
import com.iams.common.constant.Constants;
import com.iams.common.utils.file.FileUploadUtils;
import com.iams.common.utils.file.FileUtils;
import com.iams.manage.domain.Archive;
import com.iams.manage.mapper.ArchiveMapper;
import com.iams.manage.service.IArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


//import static org.elasticsearch.rest.ChunkedRestResponseBodyPart.logger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 档案列表Service业务层处理
 *
 * @author zhjm
 * @date 2025-01-10
 */
@Service
public class ArchiveServiceImpl implements IArchiveService {
    @Autowired
    private ArchiveMapper archiveMapper;

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private DocumentServiceImpl documentService;

    @Autowired
    private ArchiveRepository archiveRepository;

    @Autowired
    private DocumentContentExtractor documentContentExtractor;

    @Autowired
    private IBorrowRecordService borrowRecordService;

    @Autowired
    private PendingEvaluationMapper pendingEvaluationMapper;

    @Autowired
    private ISysPostService sysPostService;

    @Autowired
    private ArchiveHashUtil archiveHashUtil;

    @Autowired
    private ArchiveHashMapper archiveHashMapper;

    @Autowired
    private IProjectService projectService;

    @Autowired
    private IFondsService fondsService;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private ArchiveInventoryDetailMapper archiveInventoryDetailMapper;

    @Autowired
    private ArchiveInventoryMapper archiveInventoryMapper;

    @Resource
    private MinIOInfo minioInfo;

    @Autowired
    private ArchiveCategoryMapper archiveCategoryMapper;

    /**
     * 上传档案销毁佐证材料
     *
     * @param archiveId 档案ID
     * @param file      上传的文件
     * @return 文件访问URL
     * @throws Exception
     */
    @Override
    public String uploadDestructionCertificate(Long archiveId, MultipartFile file) throws Exception {
        // 1. 校验档案是否存在
        Archive archive = archiveMapper.selectArchiveById(archiveId);
        if (archive == null) {
            throw new RuntimeException("档案不存在");
        }

        // 2. [MODIFIED] 使用若依框架的工具类来处理文件上传
        //    它会自动处理路径创建、文件名生成等，并返回一个相对路径
        //    "destruction_certs" 是我们为这类文件指定的子目录
        String filePath = RuoYiConfig.getUploadPath();
        String relativePath = FileUploadUtils.upload(filePath + "/destruction_certs", file);

        // 3. 构造可以通过Web访问的URL
        //    Constants.RESOURCE_PREFIX 是 /profile
        String fileUrl = Constants.RESOURCE_PREFIX + relativePath;

        // 4. 更新数据库
        archive.setHasDestructionCertificate(true); // 标记为已有凭证
        archive.setDestructionCertificateUrl(fileUrl); // 存储Web访问URL
        archiveMapper.updateArchive(archive);

        // 5. 返回文件URL
        return fileUrl;
    }

    private static final Logger logger = LoggerFactory.getLogger(ArchiveServiceImpl.class);

    /**
     * 查询档案列表
     *
     * @param id 档案列表主键
     * @return 档案列表
     */
    @Override
    public Archive selectArchiveById(Long id) {
        return archiveMapper.selectArchiveById(id);
    }

    /**
     * 查询档案列表列表
     *
     * @param archive 档案列表： 获取一下登陆用户的信息。对结果筛选
     * @return 档案列表
     */
    @Override
    public List<Archive> selectArchiveList(Archive archive)
    {
        return archiveMapper.selectArchiveList(archive);
    }

    @Override
    public List<Archive> selectArchiveListAll(Archive archive){
        return archiveMapper.selectArchiveList(archive);
    }

    @Override
    public List<Archive> borrowArchiveInVolume(Archive archive)
    {
        return archiveMapper.selectBorrowArchiveInVolume(archive, SecurityUtils.getUserId());
    }

    /**
     * 新增档案列表
     *
     * @param archive 档案列表
     * @return 结果
     */
    @Override
    public int insertArchive(Archive archive) {

        System.out.println("==== 档案插入调试 ====");
        System.out.println("档号: " + archive.getDanghao());
        System.out.println("名称: " + archive.getName());
        System.out.println("项目ID: " + archive.getProjectId());
        System.out.println("项目编码值: " + archive.getProjectBianhao());
        System.out.println("载体类型: " + archive.getCarrierType());
        System.out.println("创建时间: " + archive.getCreateTime());


        // 🔥 新增：检查档号是否重复
        if (archive.getDanghao() != null && !archive.getDanghao().trim().isEmpty()) {
            Set<String> danghaos = new HashSet<>();
            danghaos.add(archive.getDanghao().trim());
            List<String> existingDanghaos = archiveMapper.getExistingDanghaos(danghaos);

            if (!existingDanghaos.isEmpty()) {
                throw new ServiceException("档号【" + archive.getDanghao() + "】已存在，请修改后重试");
            }
        }



        archive.setStatus("Unarchived");


        // 根据 projectId 查询项目名称
        String projectName = "";

        if (archive.getProjectId() != null) {
            Project project = projectMapper.selectProjectById(archive.getProjectId());
            if (project != null) {
                projectName = project.getName();
                archive.setProjectInfo(project);
            }
        }
        
        ElasticsearchArchive document = new ElasticsearchArchive();
        document.setId(archive.getDanghao());
        document.setMysqlDanghao(archive.getDanghao());
        document.setTitle(archive.getName());
//        document.setContent(getFileContent(filePath));
        document.setFileType(archive.getCarrierType());
        document.setDescription(archive.getDescription());
        document.setSecretLevel(archive.getSecretLevel());
        document.setProject(projectName); // 设置项目名称
        System.out.println("设置ES项目ID: " + archive.getProjectId());
        document.setProjectid(archive.getProjectId());
        System.out.println("设置ES项目编码: " + archive.getProjectBianhao());
        document.setProjectCode(archive.getProjectBianhao());

        System.out.println("完整ES文档: ");
        System.out.println(document); // 确保重写toString()

//        document.setCreateTime(archive.getCreateTime());
//        document.setFilePath(filePath);

        archiveRepository.save(document);

//        return archiveMapper.insertArchive(archive);
        int result = archiveMapper.insertArchive(archive);

        // 新增：插入档案成功后，更新对应全宗的档案数量
        if (result > 0 && archive.getFondsId() != null) {
            try {
                fondsService.updateFondsArchiveCount(archive.getFondsId());
                System.out.println("成功更新全宗[" + archive.getFondsId() + "]的档案数量");
            } catch (Exception e) {
                System.err.println("更新全宗档案数量失败: " + e.getMessage());
                // 注意：这里不抛出异常，避免影响主业务流程
            }
        }

        return result;
    }


    private String getFileContent(String filePath) {
        StringBuilder contentBuilder = new StringBuilder();
        File directory = new File(filePath);

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".docx"));
            if (files != null && files.length > 0) {
                for (File file : files) {
                    try {
                        contentBuilder.append(documentContentExtractor.extractContent(file, "docx"));
                    } catch (Exception e) {
                        System.err.println("Failed to extract content from: " + file.getName());
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("No .docx files found in the directory.");
            }
        } else {
            System.err.println("The specified path is not a valid directory.");
        }

        return contentBuilder.toString();
    }


    /**
     * 修改档案列表 - 添加ES同步更新
     *
     * @param archive 档案列表
     * @return 结果
     */
    @Override
    public int updateArchive(Archive archive) {
        // 获取原档案信息，检查是否更换了全宗和档号
        Archive oldArchive = archiveMapper.selectArchiveById(archive.getId());
        if (oldArchive == null) {
            throw new RuntimeException("档案不存在");
        }
//        Long oldFondsId = oldArchive != null ? oldArchive.getFondsId() : null;
//        String oldDanghao = oldArchive != null ? oldArchive.getDanghao() : null;
        Long oldFondsId = oldArchive.getFondsId();
        String oldDanghao = oldArchive.getDanghao();

        // 检查档号是否重复（参考添加时的逻辑）
        if (archive.getDanghao() != null && !archive.getDanghao().trim().isEmpty()) {
            Set<String> danghaos = new HashSet<>();
            danghaos.add(archive.getDanghao().trim());
            List<String> existingDanghaos = archiveMapper.getExistingDanghaosExcludeId(danghaos, archive.getId());

            if (!existingDanghaos.isEmpty()) {
                throw new ServiceException("档号【" + archive.getDanghao() + "】已存在，请修改后重试");
            }
        }

        if (archive.getDanghao() == null || archive.getDanghao().trim().isEmpty()) {
            archive.setDanghao(oldArchive.getDanghao());
        }
        if (archive.getName() == null || archive.getName().trim().isEmpty()) {
            archive.setName(oldArchive.getName());
        }
        if (archive.getCarrierType() == null || archive.getCarrierType().trim().isEmpty()) {
            archive.setCarrierType(oldArchive.getCarrierType());
        }
        if (archive.getDescription() == null) {
            archive.setDescription(oldArchive.getDescription());
        }
        if (archive.getSecretLevel() == null) {
            archive.setSecretLevel(oldArchive.getSecretLevel());
        }
        if (archive.getProjectId() == null) {
            archive.setProjectId(oldArchive.getProjectId());
        }
        if (archive.getProjectBianhao() == null || archive.getProjectBianhao().trim().isEmpty()) {
            archive.setProjectBianhao(oldArchive.getProjectBianhao());
        }
        if (archive.getProjectName() == null || archive.getProjectName().trim().isEmpty()) {
            archive.setProjectName(oldArchive.getProjectName());
        }



        archive.setUpdateTime(DateUtils.getNowDate());
        int result = archiveMapper.updateArchive(archive);

        // 更新成功后，同步更新ES
        if (result > 0) {
            try {
                // 同步更新ES索引
                syncElasticsearchDocument(oldDanghao, archive);

//            } catch (Exception e) {
            } catch (Throwable e) {
                logger.error("同步ES索引失败，档案ID: {}, 档号: {}", archive.getId(), archive.getDanghao(), e);
                // 注意：这里不抛出异常，避免影响主业务流程
            }

            // 原有的全宗数量更新逻辑
            try {
                if (oldFondsId != null && !oldFondsId.equals(archive.getFondsId())) {
                    fondsService.updateFondsArchiveCount(oldFondsId);
                    System.out.println("成功更新原全宗[" + oldFondsId + "]的档案数量");
                }
                if (archive.getFondsId() != null) {
                    fondsService.updateFondsArchiveCount(archive.getFondsId());
                    System.out.println("成功更新新全宗[" + archive.getFondsId() + "]的档案数量");
                }
            } catch (Exception e) {
                System.err.println("更新全宗档案数量失败: " + e.getMessage());
            }
        }

        return result;
    }



    /**
     * 同步ES文档（处理档号变更的情况）
     */
    private void syncElasticsearchDocument(String oldDanghao, Archive archive) {
        String newDanghao = archive.getDanghao();

        // 添加档号验证
        if (newDanghao == null || newDanghao.trim().isEmpty()) {
            logger.error("档号为空，无法同步ES文档，档案ID: {}", archive.getId());
            return;
        }

        // 情况1：档号没有变化（最常见）- 直接更新现有文档
        if (Objects.equals(oldDanghao, newDanghao)) {
            updateExistingElasticsearchDocument(archive);
            return;
        }

        // 情况2：档号发生了变化（少见）- 删除旧文档，创建新文档
        if (oldDanghao != null && !oldDanghao.equals(newDanghao)) {
            // 删除旧文档
            try {
                archiveRepository.deleteById(oldDanghao);
                logger.info("删除旧ES文档成功，档号: {}", oldDanghao);
            } catch (Exception e) {
                logger.warn("删除旧ES文档失败，档号: {}", oldDanghao, e);
            }
        }

        // 创建新文档
        createElasticsearchDocument(archive);
    }

    /**
     * 更新现有ES文档
     */
    private void updateExistingElasticsearchDocument(Archive archive) {
        try {
            Optional<ElasticsearchArchive> existingDocOpt = archiveRepository.findById(archive.getDanghao());

            ElasticsearchArchive doc;
            if (existingDocOpt.isPresent()) {
                doc = existingDocOpt.get();
            } else {
                // 如果ES中不存在，创建新的
                doc = new ElasticsearchArchive();
                doc.setId(archive.getDanghao());
                doc.setMysqlDanghao(archive.getDanghao());
            }

            // 更新字段
            doc.setTitle(archive.getName());
            doc.setDescription(archive.getDescription());
            doc.setSecretLevel(archive.getSecretLevel());
            doc.setFileType(archive.getCarrierType());

            // 更新项目信息
            if (archive.getProjectId() != null) {
                Project project = projectMapper.selectProjectById(archive.getProjectId());
                if (project != null) {
                    doc.setProject(project.getName());
                    doc.setProjectid(archive.getProjectId());
                    doc.setProjectCode(project.getBianhao()); // 假设Project有bianhao字段
                }
            } else {
                doc.setProject(null);
                doc.setProjectid(null);
                doc.setProjectCode(null);
            }

            // 保存文档（覆盖现有文档，不是新建）
            archiveRepository.save(doc);
            logger.debug("ES文档更新成功，档号: {}", archive.getDanghao());

        } catch (Exception e) {
            logger.error("更新ES文档失败，档号: {}", archive.getDanghao(), e);
            throw e;
        }
    }

    /**
     * 创建新的ES文档
     */
    private void createElasticsearchDocument(Archive archive) {
        try {
            ElasticsearchArchive newDoc = convertToElasticsearchDocument(archive);
            archiveRepository.save(newDoc);
            logger.info("创建ES文档成功，档号: {}", archive.getDanghao());
        } catch (Exception e) {
            logger.error("创建ES文档失败，档号: {}", archive.getDanghao(), e);
            throw e;
        }
    }

//    /**
//     * 更新ES文档
//     */
//    private void updateElasticsearchDocument(Archive archive) {
//        try {
//            // 根据danghao查找现有ES文档
//            Optional<ElasticsearchArchive> existingDocOpt = archiveRepository.findById(archive.getDanghao());
//
//            if (existingDocOpt.isPresent()) {
//                ElasticsearchArchive existingDoc = existingDocOpt.get();
//
//                // 更新基本字段
//                existingDoc.setTitle(archive.getName());
//                existingDoc.setDescription(archive.getDescription());
//                existingDoc.setSecretLevel(archive.getSecretLevel());
//                existingDoc.setFileType(archive.getCarrierType());
//
//                // 根据projectId更新项目相关信息
//                if (archive.getProjectId() != null) {
//                    Project project = projectMapper.selectProjectById(archive.getProjectId());
//                    if (project != null) {
//                        existingDoc.setProject(project.getName());
//                        existingDoc.setProjectid(archive.getProjectId());
//                        existingDoc.setProjectCode(project.getBianhao()); // 假设Project有bianhao字段
//                    }
//                } else {
//                    // 如果项目ID为空，清空项目相关字段
//                    existingDoc.setProject(null);
//                    existingDoc.setProjectid(null);
//                    existingDoc.setProjectCode(null);
//                }
//
//                // 保存更新后的文档
//                archiveRepository.save(existingDoc);
//
//            } else {
//                // 如果ES中不存在该文档，重新创建
//                ElasticsearchArchive newDoc = convertToElasticsearchDocument(archive);
//                archiveRepository.save(newDoc);
//            }
//
//        } catch (Exception e) {
//            logger.error("更新ES文档失败，档号: " + archive.getDanghao(), e);
//            throw e;
//        }
//    }

    /**
     * 批量删除档案列表
     *
     * @param ids 需要删除的档案列表主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteArchiveByIds(Long[] ids) {
//        return archiveMapper.deleteArchiveByIds(ids);

        // 新增：先收集要删除的档案所属的全宗ID
        Set<Long> fondsIds = new HashSet<>();
        // 1. 先查询要删除的档案（获取档号用于删除ES数据）
        List<Archive> archives = archiveMapper.selectArchiveListByIds(ids);

        // 2. 删除MySQL数据
        int mysqlResult = archiveMapper.deleteArchiveByIds(ids);

        // 3. 删除ES数据（档号作为ES的ID）
        if (mysqlResult > 0) {
            archives.forEach(archive -> {
                archiveRepository.deleteById(archive.getDanghao()); // 使用档号作为ES ID
            });
        }

        // 4. 删除档案下的文档
        for (Long id : ids)
            documentService.deleteDocumentByArchiveId(id);

        // 新增：删除成功后，更新相关全宗的档案数量
        if (mysqlResult > 0) {
            try {
                for (Long fondsId : fondsIds) {
                    fondsService.updateFondsArchiveCount(fondsId);
                    System.out.println("成功更新全宗[" + fondsId + "]的档案数量");
                }
            } catch (Exception e) {
                System.err.println("更新全宗档案数量失败: " + e.getMessage());
                // 注意：这里不抛出异常，避免影响主业务流程
            }
        }

        return mysqlResult;

    }

    /**
     * 删除档案列表信息
     *
     * @param id 档案列表主键
     * @return 结果
     */
    @Override
    public int deleteArchiveById(Long id) {
        return archiveMapper.deleteArchiveById(id);
    }

    /**
     * 修改档案列表
     *
     * @param document
     * @return 结果
     */
    @Override
    public int addDirectoryFiles(Document document) {
        Archive archive = archiveMapper.selectArchiveById(document.getArchiveId());
        String directory = archive.getDirectory();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            List<Map<String, Object>> categories = objectMapper.readValue(directory,
                    new TypeReference<List<Map<String, Object>>>() {});

            for (Map<String, Object> category : categories) {
                // 将 category.get("id") 转换为 Long 类型
                Long categoryId = Long.parseLong(category.get("id").toString());
                if (categoryId.equals(document.getDirectory())) {
                    List<?> filesList = (List<?>) category.get("files");
                    List<Long> files = filesList.stream()
                            .map(obj -> ((Number) obj).longValue())
                            .collect(Collectors.toList());
                    files.add(document.getId());
                    category.put("files", files);
                }
                // 递归处理子类别
                List<Map<String, Object>> children = (List<Map<String, Object>>) category.get("children");
                if (children != null) {
                    processChildrenAdd(children, document); // 新增的递归方法
                }
            }

            String updatedDirectory = objectMapper.writeValueAsString(categories);
            archive.setDirectory(updatedDirectory);
            return archiveMapper.addDirectoryFiles(archive);
        } catch (IOException e) {
            e.printStackTrace();
            return 0; // 或者抛出自定义异常
        }
    }

    /**
     * 删除档案目录中的文件
     *
     * @param document
     * @return 结果
     */
    @Override
    public int deleteDirectoryFiles(Document document) {
        return documentService.deleteDocumentById(document.getId());

    }

    private void processChildren(List<Map<String, Object>> children, Document document) {
        for (Map<String, Object> child : children) {
            // 将 child.get("id") 转换为 Long 类型
            Long childId = Long.parseLong(child.get("id").toString());
            if (childId.equals(document.getDirectory())) {
                List<?> filesList = (List<?>) child.get("files");
                List<Long> files = filesList.stream()
                        .map(obj -> ((Number) obj).longValue())
                        .collect(Collectors.toList());
                files.remove(document.getId());
                child.put("files", files);
            }
            // 递归处理子类别
            List<Map<String, Object>> subChildren = (List<Map<String, Object>>) child.get("children");
            if (subChildren != null) {
                processChildren(subChildren, document);
            }
        }
    }

    /**
     * 查询档案列表
     *
     * @param location 存放的档案柜的位置
     * @return 档案列表
     */
    @Override
    public List<Archive> getArchiveByLocation(String location) {
        String[] parts = location.split("-");
        String shitiLocation = parts[0] + "-" + parts[1] + "-" + parts[2];
        if (parts.length == 5) {
            String exactLocation = parts[3] + "-" + parts[4];
            return archiveMapper.getArchiveByLocation(shitiLocation ,exactLocation);
        } else {
            return archiveMapper.getListByLocation(shitiLocation);
        }
    }

    /**
     * 拖拽移动档案
     */
    @Override
    public int moveArchive(Long archiveId, String targetLocation) {
        Archive archive = archiveMapper.selectArchiveById(archiveId);
        if (archive == null) {
            throw new RuntimeException("档案不存在");
        }
        // 现在的targetLocation是’1-1-A-1-1‘，我需要shitiLocation = '1-1-A', exactLocation = '1-1'
         String[] parts = targetLocation.split("-");
         String shitiLocation = parts[0] + "-" + parts[1] + "-" + parts[2];
         String exactLocation = parts[3] + "-" + parts[4];
        archive.setShitiLocation(shitiLocation);
        archive.setExactLocation(exactLocation);
        return archiveMapper.updateArchive(archive);
    }

    /**
     * 通过上下架更新档案位置
     */
    @Override
    public int updateLocation(List<Archive> archives) {
        for (Archive archive : archives) {
            archiveMapper.updateArchive(archive);
        }
        return archives.size();
    }

    /**
     * 获取未上架的档案列表
     */
    @Override
    public List<Archive> getUpArchiveList(Map<String, Object> query) {
        return archiveMapper.selectUpArchiveList(query);
    }

    /**
     * 根据档案号获取档案ID
     */
    @Override
    public Long getArchiveIdByMysqlDanghao(String mysqlDanghao) {
        return archiveMapper.getArchiveIdByDanghao(mysqlDanghao);
    }

    @Override
    public Map<String, Object> getArchiveLocationByDanghao(String mysqlDanghao) {
        String danghao = mysqlDanghao;
        System.out.println("Service层接收参数: " + danghao);

//        return archiveMapper.getArchiveLocationByDanghao(danghao);
        Map<String, Object> result = archiveMapper.getArchiveLocationByDanghao(danghao);
        System.out.println("Mapper返回结果: " + result);

        return result;
    }


    /**
     * 检查用户是否具有查看档案的权限
     */
    @Override
    public boolean checkArchivePermission(Long archiveID) {

        Set<String> postCode = sysPostService.selectPostCodeByUserId(SecurityUtils.getLoginUser().getUserId());

        if(postCode != null && (postCode.contains("ceo") || postCode.contains("auditor") || postCode.contains("director"))) {
            return true;
        }

        List<BorrowRecordDTO> list = borrowRecordService.selectBorrowRecordList(new BorrowRecord(SecurityUtils.getUserId(), "已批准"));

        if (list == null || list.isEmpty()) {
            return false;
        }

        Set<Long> orderList = new HashSet<>();

        for (BorrowRecordDTO borrowRecordDTO : list) {
            if (borrowRecordDTO.getArchiveId() == null) {
                continue;
            }

            Archive archive = archiveMapper.selectArchiveById(borrowRecordDTO.getArchiveId());
            // 关键：检查archive是否为空
            if (archive == null) {
                continue;
            }

            List<Archive> archives = archiveMapper.selectArchiveList(new Archive(archive.getProjectId(), archive.getJuanhao()));
            if (archives != null) {
                orderList.addAll(archives.stream()
                        .map(Archive::getId)
                        .filter(Objects::nonNull)  // 过滤掉可能的null ID
                        .collect(Collectors.toSet()));
            }
        }
        return orderList.contains(archiveID);
    }

    /**
     * 定时任务：扫描档案状态，按需添加到待审批列表中
     */

    @Override
    @Scheduled(cron = "0 0 0 * * ?")
    public void insertPendingEvaluation() {
        List<Archive> archives = archiveMapper.selectArchiveList(new Archive());
        insertPendingEvaluation(archives);
    }

    /**
     * 根据文档id获取到档案danghao档号
     */
    @Override
    public String getDanghaoByDocumentId(Long documentId) {
        // 1. 根据文档ID获取档案ID
        Document document = documentMapper.selectDocumentById(documentId);
        if (document == null) {
            throw new RuntimeException("文档不存在");
        }

        // 2. 根据档案ID获取档号
        Archive archive = archiveMapper.selectArchiveById(document.getArchiveId());
        if (archive == null) {
            throw new RuntimeException("档案不存在");
        }
        return archive.getDanghao();
    }

    /*
    使用正则表达式解析时间，进行具体的时间检查
     */
    private boolean isExpired(String period, Date guidangTime) {
        if(guidangTime == null|| period == null|| period.isEmpty())
        {
            return false;
        }
        try {
            int years = 0;
            if("永久".equals(period))
            {
                return false;
            }else {
                years = Integer.parseInt(period.replaceAll("[^0-9]",""));
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(guidangTime);
            calendar.add(Calendar.YEAR, years);

            Date expirationDate = calendar.getTime();

            return new Date().after(expirationDate);

        }catch (Exception e){
            return false;
        }
    }

    /*
     * 遍历档案列表，先检查是否已加入到待审核的表中，如果已加入则跳过，如果未加入则进行后续的检查，保管期限到期、保密期限到期，则加入到待审核列表。
     */

    public void insertPendingEvaluation(List<Archive> archives) {
        for(Archive archive : archives){
            if(isAlreadyInPendingEvaluation(archive.getId()))
            {
                continue;
            }
            if(isExpired(archive.getRetentionPeriod(), archive.getGuidangTime()))
            {
                insertIntoPendingEvaluation(archive, "保管期限已过期");
            }
            if(isExpired(archive.getSecretPeroid(), archive.getGuidangTime()))
            {
                insertIntoPendingEvaluation(archive, "保密期限已过期");
            }
        }
    }

    /**
     * 具体操作数据库的方法
     */

    private void insertIntoPendingEvaluation(Archive archive, String reason) {

        PendingEvaluation pendingEvaluation = new PendingEvaluation();
        pendingEvaluation.setArchiveId(archive.getId());
        pendingEvaluation.setEvaluationReason(reason);
        pendingEvaluation.setStatus("待鉴定");

        //插入数据
        pendingEvaluationMapper.insertPendingEvaluation(pendingEvaluation);
    }

    /*
    检查是否已经存在
     */
    private boolean isAlreadyInPendingEvaluation(Long archiveId) {
        return pendingEvaluationMapper.selectPendingEvaluationList(new PendingEvaluation(archiveId)) != null;
    }

    private void processChildrenAdd(List<Map<String, Object>> children, Document document) {
        for (Map<String, Object> child : children) {
            // 将 child.get("id") 转换为 Long 类型
            Long childId = Long.parseLong(child.get("id").toString());
            if (childId.equals(document.getDirectory())) {
                List<?> filesList = (List<?>) child.get("files");
                List<Long> files = filesList.stream()
                        .map(obj -> ((Number) obj).longValue())
                        .collect(Collectors.toList());
                files.add(document.getId());
                child.put("files", files);
            }
            // 递归处理子类别
            List<Map<String, Object>> subChildren = (List<Map<String, Object>>) child.get("children");
            if (subChildren != null) {
                processChildrenAdd(subChildren, document);
            }
        }
    }

    /*
    统计档案 数量
     */
    @Override
    public Long countByProjectId(Long projectId) {
        return archiveMapper.countByProjectId(projectId);
    }

    @Override
    public Set<Long> getProjectIdsForArchives(Long[] archiveIds) {
        if (archiveIds == null || archiveIds.length == 0) {
            return Collections.emptySet();
        }

        // 查询这些档案对应的项目ID（过滤掉null值）
        return Arrays.stream(archiveIds)
                .map(archiveMapper::selectArchiveById)
                .filter(Objects::nonNull)
                .map(Archive::getProjectId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }


    @Override
    public Map<String, Object> generateFourCharacteristicsReport(Long id) {
        Map<String, Object> report = new HashMap<>();

        Archive archive = selectArchiveById(id);
        List<Document> documents = documentMapper.selectDocumentList(new Document(id));

        report.put("documentCount", documents.size());

        // 1. 真实性检查
        report.put("authenticity", checkAuthenticity(archive, documents));

        // 2. 完整性检查
        report.put("integrity", checkIntegrity(archive, documents));

        // 3. 可用性检查
        report.put("usability", checkUsability(documents));

        // 4. 安全性检查
        report.put("security", checkSecurity(archive, documents));

        // 5. 文档详情
        report.put("documents", buildDocumentDetails(documents));

        return report;
    }

    /**
     * 检查真实性
     */
    private Map<String, Object> checkAuthenticity(Archive archive, List<Document> documents) {
        Map<String, Object> authenticity = new HashMap<>();

        ArchiveHash archiveHash = archiveHashMapper.selectByArchiveId(archive.getId());

        try {
            // 计算当前哈希值
            String currentHash = archiveHashUtil.calculateAuthenticityHash(archive, documents, archiveHash.getConfigCharacteristics());

            // 从数据库获取原始哈希值（假设archive表有authenticityHash字段）
            String originalHash = archiveHash.getAuthenticityHash();


            authenticity.put("currentHash", currentHash);
            authenticity.put("originalHash", originalHash);

            if (originalHash == null || originalHash.isEmpty()) {
                authenticity.put("status", "warning");
                authenticity.put("message", "未找到原始哈希值，无法验证真实性");
            } else if (currentHash.equals(originalHash)) {
                authenticity.put("status", "passed");
                authenticity.put("message", "文档内容未被篡改");
            } else {
                authenticity.put("status", "failed");
                authenticity.put("message", "文档内容可能已被篡改");
            }

        } catch (Exception e) {
            authenticity.put("status", "failed");
            authenticity.put("currentHash", "");
            authenticity.put("originalHash", "");
            authenticity.put("message", "真实性检查失败：" + e.getMessage());
        }

        return authenticity;
    }

    /**
     * 检查完整性
     */
    private Map<String, Object> checkIntegrity(Archive archive, List<Document> documents) {
        Map<String, Object> integrity = new HashMap<>();
        ArchiveHash archiveHash = archiveHashMapper.selectByArchiveId(archive.getId());


        try {
            // 计算当前完整性哈希
            String currentHash = archiveHashUtil.calculateIntegrityHash(archive, documents, archiveHash.getConfigCharacteristics());

            // 从数据库获取原始完整性哈希（假设archive表有integrityHash字段）
            String originalHash = archiveHash.getIntegrityHash();

            integrity.put("currentHash", currentHash);
            integrity.put("originalHash", originalHash);

            if (originalHash == null || originalHash.isEmpty()) {
                integrity.put("status", "warning");
                integrity.put("message", "未找到原始完整性哈希，无法验证完整性");
            } else if (currentHash.equals(originalHash)) {
                integrity.put("status", "passed");
                integrity.put("message", "文件完整性检查通过");
            } else {
                integrity.put("status", "failed");
                integrity.put("message", "文件完整性检查失败，文件可能损坏或被修改");
            }

        } catch (Exception e) {
            integrity.put("status", "failed");
            integrity.put("currentHash", "");
            integrity.put("originalHash", "");
            integrity.put("message", "完整性检查失败：" + e.getMessage());
        }

        return integrity;
    }

    /**
     * 检查可用性
     */
    private Map<String, Object> checkUsability(List<Document> documents) {
        Map<String, Object> usability = new HashMap<>();

        int readableCount = 0;
        int totalCount = documents.size();

        for (Document doc : documents) {
            if (isDocumentReadable(doc)) {
                readableCount++;
            }
        }

        usability.put("readableCount", readableCount);

        if (readableCount == totalCount) {
            usability.put("status", "passed");
            usability.put("message", "所有文档均可正常访问");
        } else if (readableCount > 0) {
            usability.put("status", "warning");
            usability.put("message", String.format("部分文档无法访问（%d/%d 可访问）", readableCount, totalCount));
        } else {
            usability.put("status", "failed");
            usability.put("message", "所有文档均无法访问");
        }

        return usability;
    }

    /**
     * 检查安全性
     */
    private Map<String, Object> checkSecurity(Archive archive, List<Document> documents) {
        Map<String, Object> security = new HashMap<>();

        try {
            // 检查档案权限状态（根据你的业务逻辑调整）
            String permissionStatus = checkArchivePermissions(archive);

            security.put("permissionStatus", permissionStatus);

            if ("正常".equals(permissionStatus)) {
                security.put("status", "passed");
                security.put("message", "访问权限设置正确");
            } else if ("部分异常".equals(permissionStatus)) {
                security.put("status", "warning");
                security.put("message", "部分权限设置存在问题");
            } else {
                security.put("status", "failed");
                security.put("message", "访问权限设置异常");
            }

        } catch (Exception e) {
            security.put("status", "failed");
            security.put("permissionStatus", "检查失败");
            security.put("message", "安全性检查失败：" + e.getMessage());
        }

        return security;
    }

    /**
     * 构建文档详情列表
     */
    private List<Map<String, Object>> buildDocumentDetails(List<Document> documents) {
        return documents.stream().map(doc -> {
            Map<String, Object> docDetail = new HashMap<>();
            docDetail.put("documentName", doc.getName());
            docDetail.put("documentType", doc.getFileType());
            docDetail.put("fileSize", doc.getFileSize());

            // 检查单个文档状态
            String status = checkSingleDocumentStatus(doc);
            docDetail.put("status", status);

            return docDetail;
        }).collect(Collectors.toList());
    }

    /**
     * 检查单个文档是否可读
     */
    private boolean isDocumentReadable(Document document) {
        try {
            // 方法1：检查MinIO文件
            if (document.getMinioName() != null && !document.getMinioName().isEmpty()) {
                return checkMinioFileExists(document.getMinioName());
            }

            // 方法2：检查URL文件
            if (document.getFilePath() != null && !document.getFilePath().isEmpty()) {
                return checkUrlFileExists(document.getFilePath());
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查MinIO文件是否存在
     */
    private boolean checkMinioFileExists(String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioInfo.getBucket())
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查URL文件是否可访问
     */
    private boolean checkUrlFileExists(String fileUrl) {
        try {
            HttpResponse response = HttpUtil.createGet(fileUrl).execute();
            return response.getStatus() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查档案权限状态
     */
    private String checkArchivePermissions(Archive archive) {
        // 根据你的业务逻辑实现权限检查
        // 这里是示例实现
        try {
            // 检查档案状态、用户权限等
            if (archive.getStatus() == null) {
                return "异常";
            }

            // 可以添加更多权限检查逻辑
            // 例如：检查当前用户是否有访问权限等

            return "正常";
        } catch (Exception e) {
            return "检查失败";
        }
    }

    /**
     * 检查单个文档状态
     */
    private String checkSingleDocumentStatus(Document document) {
        // 综合检查文档的各种状态
        if (!isDocumentReadable(document)) {
            return "failed";
        }

        // 可以添加更多检查逻辑
        // 例如：文件大小是否合理、格式是否正确等

        return "passed";
    }



    @Override
    public boolean guidang(Long id, String config)
    {
        Archive archive = archiveMapper.selectArchiveById(id);

        List<Document> documents = documentMapper.selectDocumentList(new Document(archive.getId()));
        Archive archiveHashpart = archiveMapper.selectArchiveById(archive.getId());
        ArchiveHash archiveHash = new ArchiveHash();
        archiveHash.setArchiveId(archive.getId());
        archiveHash.setConfigCharacteristics(config);
        archiveHash.setAuthenticityHash(archiveHashUtil.calculateAuthenticityHash(archiveHashpart, documents, config));
        archiveHash.setIntegrityHash(archiveHashUtil.calculateIntegrityHash(archiveHashpart, documents, config));


        ArchiveHash tmp = archiveHashMapper.selectByArchiveId(archive.getId());
        if (tmp == null)
        {
            archiveHashMapper.insertArchiveHash(archiveHash);
        } else {
            archiveHash.setId(tmp.getId());
            archiveHashMapper.updateArchiveHash(archiveHash);
        }

        archive.setStatus("Archived");
        return archiveMapper.updateArchive(archive)==1;
    }

@Override
@Transactional(rollbackFor = Exception.class)
public int batchImportArchives(List<Archive> archives, Long projectId) {
    // 1. 提取所有档号并检查内部重复
    List<String> allDanghaos = archives.stream()
            .map(Archive::getDanghao)
            .filter(danghao -> danghao != null && !danghao.trim().isEmpty())
            .collect(Collectors.toList());

    // 检查批量数据内部重复
    Set<String> uniqueDanghaos = new HashSet<>(allDanghaos);
    if (uniqueDanghaos.size() != allDanghaos.size()) {
        // 找出重复的档号
        Set<String> duplicates = allDanghaos.stream()
                .collect(Collectors.groupingBy(d -> d, Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        throw new ServiceException("批量数据中存在重复档号：" + String.join(", ", duplicates));
    }

    // 2. 检查与数据库中已有数据的重复
    if (!uniqueDanghaos.isEmpty()) {
        List<String> existingDanghaos = archiveMapper.getExistingDanghaos(uniqueDanghaos);
        if (!existingDanghaos.isEmpty()) {
            throw new ServiceException("以下档号已存在：" + String.join(", ", existingDanghaos) +
                    "，共" + existingDanghaos.size() + "条重复");
        }
    }

    // 3. 预处理档案数据
    List<Archive> processedArchives = archives.stream()
            .peek(archive -> {
                archive.setCreateTime(DateUtils.getNowDate());
                archive.setStatus("Unarchived");
                // 确保项目ID设置正确
                if (archive.getProjectId() == null) {
                    archive.setProjectId(projectId);
                }
            })
            .collect(Collectors.toList());

    // 4. 批量插入档案
    int successCount = 0;
    try {
        successCount = archiveMapper.batchInsertArchives(processedArchives);
    } catch (Exception e) {
        // 检查是否是唯一约束冲突
        if (e.getMessage().contains("duplicate key") ||
                e.getMessage().contains("档号") ||
                e.getMessage().contains("Duplicate entry")) {
            throw new ServiceException("档号重复，请检查后重新提交");
        }
        throw new ServiceException("批量导入失败：" + e.getMessage());
    }

    // 5. 批量创建 Elasticsearch 文档
    try {
        List<ElasticsearchArchive> documents = processedArchives.stream()
                .map(this::convertToElasticsearchDocument)
                .collect(Collectors.toList());
        archiveRepository.saveAll(documents);
    } catch (Exception e) {
        logger.warn("ES索引创建失败，但数据库操作成功", e);
        // ES失败不影响主流程
    }

    // 6. 更新项目档案计数
    if (projectId != null) {
        projectService.updateArchiveCount(projectId);
    }

    return successCount;
}

    private ElasticsearchArchive convertToElasticsearchDocument(Archive archive) {
        // 根据 projectId 查询项目名称
        String projectName = "";

        if (archive.getProjectId() != null) {
            Project project = projectMapper.selectProjectById(archive.getProjectId());
            if (project != null) {
                projectName = project.getName();
                archive.setProjectInfo(project);
            }
        }


        ElasticsearchArchive document = new ElasticsearchArchive();
        document.setId(archive.getDanghao());
        document.setMysqlDanghao(archive.getDanghao());
        document.setTitle(archive.getName());
        document.setFileType(archive.getCarrierType());
        document.setDescription(archive.getDescription());
        document.setSecretLevel(archive.getSecretLevel());
        document.setProject(projectName); // 设置项目名称
        document.setProjectid(archive.getProjectId());
        document.setProjectCode(archive.getProjectBianhao());
        return document;
    }


//    public Map<String, Boolean> verifyArchiveIntegrity(Long archiveId) {
//        Map<String, Boolean> verificationResult = new HashMap<>();
//
//        // 1. 获取当前数据
//        Archive archive = selectArchiveById(archiveId);
//        List<Document> documents = documentMapper.selectDocumentList(new Document(archiveId));
//
//        // 2. 计算当前哈希值
//        String currentAuthenticityHash = archiveHashUtil.calculateAuthenticityHash(archive, documents);
//        String currentIntegrityHash = archiveHashUtil.calculateIntegrityHash(archive, documents);
//
//        // 3. 获取存储的哈希值（假设存储在archive表的某个字段中）
//        String storedAuthenticityHash = archive.getAuthenticityHash();
//        String storedIntegrityHash = archive.getIntegrityHash();
//
//        // 4. 比较哈希值
//        verificationResult.put("authenticity", currentAuthenticityHash.equals(storedAuthenticityHash));
//        verificationResult.put("integrity", currentIntegrityHash.equals(storedIntegrityHash));
//
//        return verificationResult;
//    }
    public List<Archive> selectByCategoryId(String categoryId, Long projectId) {
        return archiveMapper.selectByCategoryId(categoryId, projectId);
    }


    @Override
    public boolean checkRfidUnique(String rfid){
        return archiveMapper.checkRfidDuplicate(rfid) <= 0;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveInventoryMain(InventoryResultDTO inventoryResult) {
        ArchiveInventory inventory = new ArchiveInventory();
        inventory.setInventoryTime(inventoryResult.getInventoryTime());
        inventory.setTotalCount(inventoryResult.getTotalCount());
        inventory.setScannedCount(inventoryResult.getScannedCount());
        inventory.setCreateTime(DateUtils.getNowDate());
        inventory.setCreateBy(SecurityUtils.getUsername());

        archiveInventoryMapper.insertArchiveInventory(inventory);
        return inventory.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveInventoryDetails(Long inventoryId, List<InventoryResultDTO.InventoryRecord> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        List<ArchiveInventoryDetail> details = new ArrayList<>();
        for (InventoryResultDTO.InventoryRecord record : records) {
            ArchiveInventoryDetail detail = new ArchiveInventoryDetail();
            detail.setInventoryId(inventoryId);
            detail.setArchiveId(record.getArchiveId());
            detail.setRfid(record.getRfid());
            detail.setDanghao(record.getDanghao());
            detail.setScanTime(record.getScanTime());
            detail.setAntenna(record.getAntenna());
            detail.setRssi(record.getRssi());
            detail.setShitiLocation(record.getShitiLocation());
            detail.setExactLocation(record.getExactLocation());
            detail.setManual(Boolean.TRUE.equals(record.getManual()) ? 1 : 0);
            details.add(detail);
        }

        // 批量插入
        int batchSize = 500;
        for (int i = 0; i < details.size(); i += batchSize) {
            List<ArchiveInventoryDetail> batchList = details.subList(i,
                    Math.min(i + batchSize, details.size()));
            archiveInventoryDetailMapper.batchInsertDetails(batchList);
        }
    }

    @Override
    public List<Archive> selectArchiveListByStatuses(Archive archive, List<String> statuses) {
        return archiveMapper.selectArchiveListByStatuses(archive, statuses);
    }

    @Override
    public List<Map<String, Object>> getArchiveStatusByDanghaos(List<String> danghaos) {
        if (danghaos == null || danghaos.isEmpty()) {
            return new ArrayList<>();
        }
        return archiveMapper.selectArchiveStatusByDanghaos(danghaos);
    }

    @Override
    public Archive selectArchiveByRfid(String rfid) {
        return archiveMapper.selectArchiveByRfid(rfid);
    }

    @Override
    public List<Archive> getArchivedList() {
        return archiveMapper.getArchivedList();
    }

    // 档案添加大类字段(A0201,A0202)-->(A:工程准备,A0201,A0202)
    @Override
    @Transactional
    public  boolean addCategoryRootName(){

        List<Archive> archives = archiveMapper.selectArchiveList(new Archive());
        for (Archive archive : archives) {
            if (archive.getCategoryId() != null){
                String rootCategoryCode = String.valueOf(archive.getCategoryId().charAt(0));
                String rootName = archiveCategoryMapper.selectNameByCode(rootCategoryCode);

                String newCategoryId = rootCategoryCode + ":" + rootName +  "," + archive.getCategoryId();
                archive.setCategoryId(newCategoryId);
                archiveMapper.updateArchive(archive);
            }
        }

        return true;
    }

}
