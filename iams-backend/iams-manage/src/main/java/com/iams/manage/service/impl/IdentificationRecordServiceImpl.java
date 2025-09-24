package com.iams.manage.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.iams.common.utils.SecurityUtils;
import com.iams.common.utils.StringUtils;
import com.iams.manage.mapper.IdentificationRecordMapper;
import com.iams.manage.domain.workflow.IdentificationRecord;
import com.iams.manage.domain.workflow.dto.IdentificationRecordDTO;
import com.iams.manage.service.IIdentificationRecordService;
import com.iams.manage.service.IArchiveService;
import com.iams.manage.domain.Archive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import static org.elasticsearch.rest.ChunkedRestResponseBodyPart.logger;

/**
 * 档案鉴定记录Service业务层处理
 *
 * @author LiuTao
 * @date 2025-06-26
 */
@Service
public class IdentificationRecordServiceImpl implements IIdentificationRecordService {

    @Autowired
    private IdentificationRecordMapper identificationRecordMapper;

    @Autowired
    private IArchiveService archiveService;

    // 添加正确的logger定义
    private static final Logger logger = LoggerFactory.getLogger(IdentificationRecordServiceImpl.class);

    /**
     * 查询鉴定记录
     *
     * @param id 鉴定记录主键
     * @return 鉴定记录
     */
    @Override
    public IdentificationRecord selectIdentificationRecordById(Long id) {
        return identificationRecordMapper.selectIdentificationRecordById(id);
    }

    /**
     * 查询鉴定记录列表
     *
     * @param identificationRecord 鉴定记录
     * @return 鉴定记录
     */
    @Override
    public List<IdentificationRecord> selectIdentificationRecordList(IdentificationRecord identificationRecord) {
        return identificationRecordMapper.selectIdentificationRecordList(identificationRecord);
    }

    /**
     * 提交档案鉴定（支持批量）
     *
     * @param identificationRecordDTO 鉴定记录DTO
     * @return 结果
     */
    @Override
    @Transactional
    public int submitIdentificationRecord(IdentificationRecordDTO identificationRecordDTO) {
        int result = 0;

        // 添加调试日志
        logger.info("=== Service层处理开始 ===");
        logger.info("DTO中的after字段:");
        logger.info("  afterRetentionPeriod: [{}]", identificationRecordDTO.getAfterRetentionPeriod());
        logger.info("  afterSecretLevel: [{}]", identificationRecordDTO.getAfterSecretLevel());
        logger.info("  afterSecretPeriod: [{}]", identificationRecordDTO.getAfterSecretPeriod());


        // 获取档案ID列表
        List<Long> archiveIds = identificationRecordDTO.getArchiveIds();
        if (archiveIds == null || archiveIds.isEmpty()) {
            if (identificationRecordDTO.getArchiveId() != null) {
                archiveIds = List.of(identificationRecordDTO.getArchiveId());
            } else {
                throw new RuntimeException("档案ID不能为空");
            }
        }

        for (Long archiveId : archiveIds) {
            // 1. 获取档案信息
            Archive archive = archiveService.selectArchiveById(archiveId);
            if (archive == null) {
                continue;
            }

            // 2. 创建鉴定记录
            IdentificationRecord record = new IdentificationRecord();
            record.setArchiveId(archiveId);
            record.setDanghao(archive.getDanghao());
            record.setArchiveName(archive.getName());
            record.setAppraiser(identificationRecordDTO.getAppraiser());
            record.setAppraisalTime(new Date());

            // 记录鉴定前状态
            record.setBeforeRetentionPeriod(archive.getRetentionPeriod());
            record.setBeforeSecretLevel(archive.getSecretLevel());
            record.setBeforeSecretPeriod(archive.getSecretPeroid());

            // 记录鉴定后状态
            record.setAfterRetentionPeriod(identificationRecordDTO.getAfterRetentionPeriod());
            record.setAfterSecretLevel(identificationRecordDTO.getAfterSecretLevel());
            record.setAfterSecretPeriod(identificationRecordDTO.getAfterSecretPeriod());

            record.setAppraisalResult(identificationRecordDTO.getAppraisalResult());
            record.setAppraisalReason(identificationRecordDTO.getAppraisalReason());

            // 在这里添加日志
            logger.info("即将插入的鉴定记录 after 字段:");
            logger.info("  afterRetentionPeriod: [{}]", record.getAfterRetentionPeriod());
            logger.info("  afterSecretLevel: [{}]", record.getAfterSecretLevel());
            logger.info("  afterSecretPeriod: [{}]", record.getAfterSecretPeriod());


            // 计算鉴定原因
            record.setPurpose(calculateAppraisalPurpose(archive));

            record.setCreateBy(SecurityUtils.getUsername());

            // 3. 保存鉴定记录
            result += identificationRecordMapper.insertIdentificationRecord(record);

            // 4. 更新档案信息
//            Archive updateArchive = new Archive();
//            updateArchive.setId(archiveId);
//
////            updateArchive.setRetentionPeriod(identificationRecordDTO.getAfterRetentionPeriod());
////            updateArchive.setSecretLevel(identificationRecordDTO.getAfterSecretLevel());
////            updateArchive.setSecretPeroid(identificationRecordDTO.getAfterSecretPeriod());
////            updateArchive.setUpdateBy(SecurityUtils.getUsername());
////            updateArchive.setUpdateTime(new Date());
//            // 保持原有字段不变
//            updateArchive.setDanghao(archive.getDanghao());
//            updateArchive.setName(archive.getName());
//            updateArchive.setCarrierType(archive.getCarrierType());
//            updateArchive.setDescription(archive.getDescription());
//            updateArchive.setProjectId(archive.getProjectId());
//
//            // 更新鉴定相关字段
//            updateArchive.setRetentionPeriod(identificationRecordDTO.getAfterRetentionPeriod());
//            updateArchive.setSecretLevel(identificationRecordDTO.getAfterSecretLevel());
//            updateArchive.setSecretPeroid(identificationRecordDTO.getAfterSecretPeriod());
//
////            // 更新审计字段
////            updateArchive.setUpdateBy(currentUsername);
////            updateArchive.setUpdateTime(currentTime);
//
//            // 如果鉴定结果是销毁，更新档案状态
//            if ("destroy".equals(identificationRecordDTO.getAppraisalResult())) {
//                updateArchive.setStatus("Destroyed");
//            }
//
//            archiveService.updateArchive(updateArchive);
//        }
//
//        return result;
            // 3. 构建完整的档案更新对象 - 关键修改点
            Archive updateArchive = new Archive();
            updateArchive.setId(archiveId);

            // 保持原有字段不变
            updateArchive.setDanghao(archive.getDanghao());
            updateArchive.setName(archive.getName());
            updateArchive.setCarrierType(archive.getCarrierType());
            updateArchive.setDescription(archive.getDescription());
            updateArchive.setProjectId(archive.getProjectId());

            // 更新鉴定相关字段
            updateArchive.setRetentionPeriod(identificationRecordDTO.getAfterRetentionPeriod());
            updateArchive.setSecretLevel(identificationRecordDTO.getAfterSecretLevel());
            updateArchive.setSecretPeroid(identificationRecordDTO.getAfterSecretPeriod());


            String currentUsername = SecurityUtils.getUsername();
            Date currentTime = new Date();
            updateArchive.setUpdateBy(currentUsername);
            updateArchive.setUpdateTime(currentTime);

            // 更新审计字段
            updateArchive.setUpdateBy(currentUsername);
            updateArchive.setUpdateTime(currentTime);

            // 如果鉴定结果是销毁，更新档案状态
            if ("destroy".equals(identificationRecordDTO.getAppraisalResult())) {
                updateArchive.setStatus("Destroyed");
            } else {
                updateArchive.setStatus(archive.getStatus()); // 保持原状态
            }

            // 添加调试日志
            logger.info("准备更新档案 - ID: {}, 保管期限: {} -> {}, 保密级别: {} -> {}, 保密期限: {} -> {}",
                    archiveId,
                    archive.getRetentionPeriod(), updateArchive.getRetentionPeriod(),
                    archive.getSecretLevel(), updateArchive.getSecretLevel(),
                    archive.getSecretPeroid(), updateArchive.getSecretPeroid());

            // 执行更新
            int updateResult = archiveService.updateArchive(updateArchive);
            logger.info("档案更新结果: {}", updateResult);

            if (updateResult <= 0) {
                logger.error("档案更新失败！档案ID: {}", archiveId);
                throw new RuntimeException("档案更新失败，档案ID: " + archiveId);
            }

            // 验证更新结果
            Archive updatedArchive = archiveService.selectArchiveById(archiveId);
            logger.info("更新后档案信息 - 保管期限: {}, 保密级别: {}, 保密期限: {}",
                    updatedArchive.getRetentionPeriod(),
                    updatedArchive.getSecretLevel(),
                    updatedArchive.getSecretPeroid());
        }

        return result;
    }

    /**
     * 修改鉴定记录
     *
     * @param identificationRecord 鉴定记录
     * @return 结果
     */
    @Override
    public int updateIdentificationRecord(IdentificationRecord identificationRecord) {
        identificationRecord.setUpdateBy(SecurityUtils.getUsername());
        identificationRecord.setUpdateTime(new Date());
        return identificationRecordMapper.updateIdentificationRecord(identificationRecord);
    }

    /**
     * 批量删除鉴定记录
     *
     * @param ids 需要删除的鉴定记录主键
     * @return 结果
     */
    @Override
    public int deleteIdentificationRecordByIds(Long[] ids) {
        return identificationRecordMapper.deleteIdentificationRecordByIds(ids);
    }

    /**
     * 删除鉴定记录信息
     *
     * @param id 鉴定记录主键
     * @return 结果
     */
    @Override
    public int deleteIdentificationRecordById(Long id) {
        return identificationRecordMapper.deleteIdentificationRecordById(id);
    }

    /**
     * 根据档案ID查询鉴定记录
     *
     * @param archiveId 档案ID
     * @return 鉴定记录集合
     */
    @Override
    public List<IdentificationRecord> selectIdentificationRecordByArchiveId(Long archiveId) {
        return identificationRecordMapper.selectIdentificationRecordByArchiveId(archiveId);
    }

    /**
     * 获取鉴定统计数据
     *
     * @param params 查询参数
     * @return 统计数据
     */
    @Override
    public Map<String, Object> getIdentificationStatistics(Map<String, Object> params) {
        Map<String, Object> statistics = new HashMap<>();

        String beginTime = (String) params.get("beginTime");
        String endTime = (String) params.get("endTime");

        // 获取鉴定结果统计
        List<Map<String, Object>> resultStats = identificationRecordMapper.selectAppraisalStatistics(beginTime, endTime);

        long totalCount = 0;
        long archiveCount = 0;
        long destroyCount = 0;

        for (Map<String, Object> stat : resultStats) {
            String result = (String) stat.get("result");
            Long count = (Long) stat.get("count");

            totalCount += count;
            if ("archive".equals(result)) {
                archiveCount = count;
            } else if ("destroy".equals(result)) {
                destroyCount = count;
            }
        }

        statistics.put("totalCount", totalCount);
        statistics.put("archiveCount", archiveCount);
        statistics.put("destroyCount", destroyCount);
        statistics.put("resultStats", resultStats);

        // 获取月度统计
        List<Map<String, Object>> monthlyStats = identificationRecordMapper.selectMonthlyAppraisalCount();
        statistics.put("monthlyStats", monthlyStats);

        return statistics;
    }

    /**
     * 计算档案需要鉴定的原因
     *
     * @param archive 档案信息
     * @return 鉴定原因
     */
    private String calculateAppraisalPurpose(Archive archive) {
        StringBuilder purpose = new StringBuilder();

        if (StringUtils.isEmpty(archive.getRetentionPeriod())) {
            purpose.append("保管期限缺失;");
        }
        if (StringUtils.isEmpty(archive.getSecretLevel())) {
            purpose.append("保密级别缺失;");
        }
        if (StringUtils.isEmpty(archive.getSecretPeroid())) {
            purpose.append("保密期限缺失;");
        }

        // 检查是否过期（简化的过期检查）
        if (isRetentionExpired(archive)) {
            purpose.append("保管期限过期;");
        }
        if (isSecretExpired(archive)) {
            purpose.append("保密期限过期;");
        }

        return purpose.length() > 0 ? purpose.substring(0, purpose.length() - 1) : "常规鉴定";
    }

    /**
     * 检查保管期限是否过期
     */
    private boolean isRetentionExpired(Archive archive) {
        if (StringUtils.isEmpty(archive.getRetentionPeriod()) || archive.getCreateTime() == null) {
            return false;
        }

        try {
            int retentionYears = Integer.parseInt(archive.getRetentionPeriod());
            long yearsDiff = (System.currentTimeMillis() - archive.getCreateTime().getTime()) / (365L * 24 * 60 * 60 * 1000);
            return yearsDiff > retentionYears;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 检查保密期限是否过期
     */
    private boolean isSecretExpired(Archive archive) {
        if (StringUtils.isEmpty(archive.getSecretPeroid()) || archive.getCreateTime() == null) {
            return false;
        }

        try {
            int secretYears = Integer.parseInt(archive.getSecretPeroid());
            long yearsDiff = (System.currentTimeMillis() - archive.getCreateTime().getTime()) / (365L * 24 * 60 * 60 * 1000);
            return yearsDiff > secretYears;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}