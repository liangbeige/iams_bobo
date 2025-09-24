package com.iams.manage.service.impl;

import com.iams.common.utils.DateUtils;
import com.iams.common.utils.SecurityUtils;
import com.iams.manage.controller.ArchiveDestroyApprovalController;
import com.iams.manage.domain.Archive;
import com.iams.manage.domain.workflow.DestroyRecord;
import com.iams.manage.domain.workflow.dto.DestroyRecordDTO;
import com.iams.manage.mapper.ArchiveMapper;
import com.iams.manage.mapper.DestroyRecordMapper;
import com.iams.manage.service.IDestroyRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 销毁记录Service业务层处理
 *
 * 主要修改说明：
 * 1. 移除了工作流相关代码（business_key, instance_id等）
 * 2. 简化了字段映射，只保留数据表中存在的字段
 * 3. 将前端表单字段（reason, description, applicant）合并到purpose字段
 * 4. 保持核心业务流程：提交申请 -> 离线下架 -> 确认销毁
 */
@Slf4j
@Service
@Transactional
public class DestroyRecordServiceImpl implements IDestroyRecordService {

    @Autowired
    private DestroyRecordMapper destroyRecordMapper;

    @Autowired
    private ArchiveMapper archiveMapper;

    /**
     * 查询销毁记录（包含关联信息）
     */
    @Override
    public DestroyRecordDTO selectDestroyRecordById(Long id) {
        DestroyRecord entity = destroyRecordMapper.selectDestroyRecordById(id);
        return entity != null ? convertToDTO(entity) : null;
    }

    /**
     * 查询销毁记录列表（包含关联信息）
     */
    @Override
    public List<DestroyRecordDTO> selectDestroyRecordList(DestroyRecord destroyRecord) {
        List<DestroyRecord> list = destroyRecordMapper.selectDestroyRecordList(destroyRecord);
        return list.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 查询简单销毁记录（只包含基本信息）
     */
    @Override
    public DestroyRecordDTO selectSimpleDestroyRecordById(Long id) {
        DestroyRecord entity = destroyRecordMapper.selectDestroyRecordById(id);
        return entity != null ? convertToSimpleDTO(entity) : null;
    }

    /**
     * 查询简单销毁记录列表（只包含基本信息）
     */
    @Override
    public List<DestroyRecordDTO> selectSimpleDestroyRecordList(DestroyRecord destroyRecord) {
        List<DestroyRecord> list = destroyRecordMapper.selectDestroyRecordList(destroyRecord);
        return list.stream().map(this::convertToSimpleDTO).collect(Collectors.toList());
    }

    /**
     * 提交销毁申请（单个档案）
     * 修改说明：提交申请时同时更新档案状态为"销毁中"
     */
    @Override
    @Transactional
    public int submitDestroyApplication(DestroyRecordDTO dto) {
        log.info("开始处理销毁申请，档案ID: {}", dto.getArchiveId());

        // 获取当前用户信息
        Long currentUserId = SecurityUtils.getUserId();
        String currentUserName = SecurityUtils.getUsername();

        // 字段验证
        if (dto.getArchiveId() == null) {
            throw new RuntimeException("档案ID不能为空");
        }
        if (dto.getPurpose() == null || dto.getPurpose().trim().isEmpty()) {
            throw new RuntimeException("销毁原因不能为空");
        }

        // 检查档案状态
        Archive archive = archiveMapper.selectArchiveById(dto.getArchiveId());
        if (archive == null) {
            throw new RuntimeException("档案不存在，无法提交销毁申请");
        }

        // 检查档案状态
        if (!"Archived".equals(archive.getStatus())) {
            throw new RuntimeException("只能对已归档的档案提交销毁申请，当前档案状态：" +
                    (archive.getStatus() != null ? archive.getStatus() : "未知"));
        }

        // 修改可用性状态检查
        Integer availability = archive.getAvailability();
        if (availability == null) {
            throw new RuntimeException("档案状态异常（可用性状态未设置），无法提交销毁申请。请联系管理员检查档案状态");
        }

        // 根据 availability 状态给出具体提示
        switch (availability) {
            case 0:
                // 在架状态，可以提交销毁申请
                log.info("档案在架状态，允许提交销毁申请。档案ID: {}", dto.getArchiveId());
                break;
            case 1:
                throw new RuntimeException("该档案正在销毁流程中（待下架状态），无法重复提交销毁申请");
            case 2:
                throw new RuntimeException("该档案处于待上架状态，请等待档案上架后再提交销毁申请");
//            case 3:
//                throw new RuntimeException("该档案正在销毁流程中（待销毁状态），无法重复提交销毁申请");
            default:
                throw new RuntimeException("档案状态异常（可用性状态：" + availability + "），无法提交销毁申请。请联系管理员");
        }

        // 检查是否已存在销毁申请
        List<DestroyRecord> existingRecords = destroyRecordMapper.selectDestroyRecordList(
                new DestroyRecord(currentUserId, dto.getArchiveId()));

        for (DestroyRecord record : existingRecords) {
            if ("待下架".equals(record.getStatus())) {
                throw new RuntimeException("该档案已有待处理的销毁申请，请勿重复提交");
            }
        }

        // 创建销毁记录
        DestroyRecord destroyRecord = new DestroyRecord();
        destroyRecord.setUserId(currentUserId);
        destroyRecord.setArchiveId(dto.getArchiveId());
        destroyRecord.setPurpose(dto.getPurpose());
        destroyRecord.setStatus("待下架");
        destroyRecord.setStartApplyTime(DateUtils.getNowDate());

        // 设置备注信息
        String remarkInfo = String.format("档案销毁申请 - 档案：%s(%s) - 操作员：%s",
                archive.getName(), archive.getDanghao(), currentUserName);
        destroyRecord.setRemark(remarkInfo);

        // 🔥 关键修改：更新档案状态
        archive.setAvailability(1);           // 🔥 改为 1（待销毁）
        archive.setStatus("Destroying");      // 设置档案状态为"销毁中"
        // 修改RFID为05结尾，表示需要下架
        if (archive.getRfid() != null && archive.getRfid().length() >= 2) {
            String baseRfid = archive.getRfid().substring(0, archive.getRfid().length() - 2);
            archive.setRfid(baseRfid + "05");
        }

        archiveMapper.updateArchive(archive);

        int result = destroyRecordMapper.insertDestroyRecord(destroyRecord);
        log.info("销毁申请创建成功，档案状态已更新为销毁中（availability=1），记录ID: {}", destroyRecord.getId());

        return result;
    }

    /**
     * 批量提交销毁申请
     * 修改说明：简化了字段设置，保持核心逻辑
     */
    @Override
    @Transactional
    public int batchSubmitDestroyApplication(ArchiveDestroyApprovalController.BatchDestroyRequest request) {
        if (request.getArchiveIds() == null || request.getArchiveIds().length == 0) {
            throw new RuntimeException("请选择要销毁的档案");
        }
        if (request.getPurpose() == null || request.getPurpose().trim().isEmpty()) {
            throw new RuntimeException("销毁原因不能为空");
        }

        Long currentUserId = SecurityUtils.getUserId();
        String currentUserName = SecurityUtils.getUsername();

        List<String> errorMessages = new ArrayList<>();
        int successCount = 0;

        for (Long archiveId : request.getArchiveIds()) {
            try {
                // 检查档案状态
                Archive archive = archiveMapper.selectArchiveById(archiveId);
                if (archive == null) {
                    errorMessages.add("档案ID " + archiveId + " 不存在");
                    continue;
                }

                // 检查归档状态
                if (!"Archived".equals(archive.getStatus())) {
                    errorMessages.add("档案【" + archive.getName() + "】不是已归档状态（当前状态：" +
                            (archive.getStatus() != null ? archive.getStatus() : "未知") + "）");
                    continue;
                }


                Integer availability = archive.getAvailability();
                if (availability == null) {
                    errorMessages.add("档案【" + archive.getName() + "】状态异常（可用性状态未设置）");
                    continue;
                }

                // 根据 availability 状态给出具体提示
                switch (availability) {
                    case 0:
                        // 在架状态，可以提交销毁申请
                        break;
                    case 1:
                        errorMessages.add("档案【" + archive.getName() + "】正在销毁流程中（待下架状态）");
                        continue;
                    case 2:
                        errorMessages.add("档案【" + archive.getName() + "】处于待上架状态，请等待上架后再申请");
                        continue;
//                    case 3:
//                        errorMessages.add("档案【" + archive.getName() + "】正在销毁流程中（待销毁状态）");
//                        continue;
                    default:
                        errorMessages.add("档案【" + archive.getName() + "】状态异常（可用性状态：" + availability + "）");
                        continue;
                }

                // 检查是否已有销毁申请
                List<DestroyRecord> existingRecords = destroyRecordMapper.selectDestroyRecordList(
                        new DestroyRecord(currentUserId, archiveId));
                boolean hasPendingDestroy = existingRecords.stream()
                        .anyMatch(record -> "待下架".equals(record.getStatus()));
                if (hasPendingDestroy) {
                    errorMessages.add("档案【" + archive.getName() + "】已有待处理的销毁申请");
                    continue;
                }

                // 创建销毁记录
                DestroyRecord destroyRecord = new DestroyRecord();
                destroyRecord.setUserId(currentUserId);
                destroyRecord.setArchiveId(archiveId);
                destroyRecord.setPurpose(request.getPurpose());
                destroyRecord.setStatus("待下架");
                destroyRecord.setStartApplyTime(DateUtils.getNowDate());

                // 批量申请的备注
                String remarkInfo = String.format("批量销毁申请 - 档案：%s(%s) - 操作员：%s",
                        archive.getName(), archive.getDanghao(), currentUserName);
                destroyRecord.setRemark(remarkInfo);

                // 🔥 关键修改：更新档案状态
                archive.setAvailability(1);           // 🔥 改为 1（待销毁）
                archive.setStatus("Destroying");      // 设置为销毁中
                // 🔥 新增：修改RFID为05结尾，表示需要下架
                if (archive.getRfid() != null && archive.getRfid().length() >= 2) {
                    String baseRfid = archive.getRfid().substring(0, archive.getRfid().length() - 2);
                    archive.setRfid(baseRfid + "05");
                }
                archiveMapper.updateArchive(archive);

                destroyRecordMapper.insertDestroyRecord(destroyRecord);
                successCount++;

            } catch (Exception e) {
                errorMessages.add("档案ID " + archiveId + " 处理失败：" + e.getMessage());
            }
        }

        if (!errorMessages.isEmpty()) {
            String errorMsg = String.format("成功提交 %d 个，失败 %d 个。失败原因：%s",
                    successCount, errorMessages.size(), String.join("; ", errorMessages));
            throw new RuntimeException(errorMsg);
        }

        return successCount;
    }

    /**
     * 确认完成销毁（第二步：实际销毁操作）
     * 修改说明：确认销毁时将档案状态更新为"已销毁"
     */
    @Override
    @Transactional
    public int completeDestroy(Long archiveId) {
        log.info("开始确认完成销毁，档案ID: {}", archiveId);

        // 查找对应的销毁记录
        DestroyRecord queryRecord = new DestroyRecord();
        queryRecord.setArchiveId(archiveId);
        queryRecord.setStatus("待下架");

        List<DestroyRecord> records = destroyRecordMapper.selectDestroyRecordList(queryRecord);
        if (records.isEmpty()) {
            throw new RuntimeException("未找到对应的销毁申请记录");
        }

        // 检查档案状态
        Archive archive = archiveMapper.selectArchiveById(archiveId);
        if (archive == null) {
            throw new RuntimeException("档案不存在");
        }

        if (!"Destroying".equals(archive.getStatus())) {
            throw new RuntimeException("只能对销毁中状态的档案进行确认销毁操作，当前状态：" + archive.getStatus());
        }

        // 🔥 修改可用性状态检查
        if (archive.getAvailability() != 1) {
            throw new RuntimeException("只能确认待下架状态的档案，当前可用性状态：" + archive.getAvailability());
        }

        // 更新销毁记录状态
        DestroyRecord destroyRecord = records.get(0);
        destroyRecord.setStatus("已销毁");
        destroyRecord.setEndApplyTime(DateUtils.getNowDate());

        // 更新备注信息
        String originalRemark = destroyRecord.getRemark() != null ? destroyRecord.getRemark() : "";
        destroyRecord.setRemark(originalRemark + " [确认销毁时间：" + DateUtils.dateTimeNow() + "]");

        destroyRecordMapper.updateDestroyRecord(destroyRecord);

        // 🔥 关键修改：更新档案状态为"已销毁"
        archive.setAvailability(2);           // 设置为已下架（已销毁）
        archive.setStatus("Destroyed");
        archive.setShitiLocation("");        // 🔥 清空实体位置
        archive.setExactLocation("");  // 设置档案状态为"已销毁"
        archiveMapper.updateArchive(archive);

        log.info("销毁确认完成，档案状态已更新为已销毁，档案ID: {}", archiveId);
        return 1;
    }


    /**
     * 标记销毁失败
     * 修改说明：保持核心逻辑，简化备注更新
     */
    @Override
    @Transactional
    public int failDestroy(Long archiveId, String reason) {
        log.info("开始标记销毁失败，档案ID: {}, 原因: {}", archiveId, reason);

        // 查找对应的销毁记录
        DestroyRecord queryRecord = new DestroyRecord();
        queryRecord.setArchiveId(archiveId);
        queryRecord.setStatus("待下架");

        List<DestroyRecord> records = destroyRecordMapper.selectDestroyRecordList(queryRecord);
        if (records.isEmpty()) {
            throw new RuntimeException("未找到对应的销毁申请记录");
        }

        // 更新销毁记录状态
        DestroyRecord destroyRecord = records.get(0);
        destroyRecord.setStatus("销毁失败");
        destroyRecord.setEndApplyTime(DateUtils.getNowDate());

        // 更新备注信息，包含失败原因
        String originalRemark = destroyRecord.getRemark() != null ? destroyRecord.getRemark() : "";
        destroyRecord.setRemark(originalRemark + " [销毁失败：" + reason + " - 时间：" + DateUtils.dateTimeNow() + "]");

        destroyRecordMapper.updateDestroyRecord(destroyRecord);

        // 恢复档案状态
        Archive archive = archiveMapper.selectArchiveById(archiveId);
        if (archive != null) {
            archive.setAvailability(0); // 恢复为在架状态
            archiveMapper.updateArchive(archive);
        }

        log.info("销毁失败标记完成，档案ID: {}", archiveId);
        return 1;
    }

    /**
     * 取消销毁申请（仅限待下架状态）
     * 修改说明：保持核心逻辑不变
     */
    @Override
    @Transactional
    public int cancelDestroyApplication(Long id) {
        log.info("开始取消销毁申请，记录ID: {}", id);

        DestroyRecord destroyRecord = destroyRecordMapper.selectDestroyRecordById(id);
        if (destroyRecord == null) {
            throw new RuntimeException("销毁记录不存在");
        }
        if (!"待下架".equals(destroyRecord.getStatus())) {
            throw new RuntimeException("只能取消待下架状态的销毁申请");
        }

        // 恢复档案状态
        Archive archive = archiveMapper.selectArchiveById(destroyRecord.getArchiveId());
        if (archive != null) {
            archive.setAvailability(0); // 恢复为在架状态
            archiveMapper.updateArchive(archive);
        }

        // 删除销毁记录
        int result = destroyRecordMapper.deleteDestroyRecordById(id);
        log.info("销毁申请取消完成，记录ID: {}", id);
        return result;
    }

    /**
     * RFID同步完成销毁（盘点车离线操作后的同步）
     * 新增方法：处理离线下架操作的同步
     */
    @Override
    @Transactional
    public int syncOfflineDestroyOperation(Long archiveId) {
        log.info("开始同步离线销毁操作，档案ID: {}", archiveId);

        Archive archive = archiveMapper.selectArchiveById(archiveId);
        if (archive == null) {
            throw new RuntimeException("档案不存在");
        }

        // 更新RFID状态（05 -> 00，表示已下架）
        if (archive.getRfid() != null && archive.getRfid().endsWith("05")) {
            archive.setRfid(archive.getRfid().substring(0, archive.getRfid().length() - 2) + "00");
        }

        // 更新availability状态（1:待下架 -> 2:待上架，但这里是销毁流程，所以保持2）
        archive.setAvailability(2);

        archiveMapper.updateArchive(archive);
        log.info("离线销毁操作同步完成，档案ID: {}", archiveId);
        return 1;
    }

    /**
     * 撤销销毁申请
     * 修改说明：撤销时恢复档案状态为"已归档"
     */
    @Override
    @Transactional
    public int cancelDestroyByArchiveId(Long archiveId) {
        log.info("开始根据档案ID撤销销毁申请，档案ID: {}", archiveId);

        if (archiveId == null) {
            throw new RuntimeException("档案ID不能为空");
        }

        // 检查档案状态
        Archive archive = archiveMapper.selectArchiveById(archiveId);
        if (archive == null) {
            throw new RuntimeException("档案不存在");
        }

        // 🔥 修改状态检查逻辑
        if (!"Destroying".equals(archive.getStatus())) {
            throw new RuntimeException("只能撤销销毁中状态的申请，当前档案状态：" + archive.getStatus());
        }

        if (archive.getAvailability() != 1) {
            throw new RuntimeException("只能撤销待下架状态的销毁申请，当前可用性状态：" + archive.getAvailability());

        }

        // 查找对应的销毁记录
        DestroyRecord queryRecord = new DestroyRecord();
        queryRecord.setArchiveId(archiveId);
        queryRecord.setStatus("待下架");

        List<DestroyRecord> records = destroyRecordMapper.selectDestroyRecordList(queryRecord);
        if (records.isEmpty()) {
            throw new RuntimeException("未找到对应的销毁申请记录");
        }

        // 关键修改：恢复档案状态
        archive.setAvailability(0);           // 恢复为在架状态
        archive.setStatus("Archived");        // 增：恢复档案状态为"已归档"
        archiveMapper.updateArchive(archive);

        // 删除销毁记录
        int result = 0;
        for (DestroyRecord record : records) {
            result += destroyRecordMapper.deleteDestroyRecordById(record.getId());
        }

        log.info("撤销销毁申请完成，档案状态已恢复为已归档，档案ID: {}，删除记录数: {}", archiveId, result);
        return result;
    }

    /**
     * 修改销毁记录
     */
    @Override
    public int updateDestroyRecord(DestroyRecordDTO dto) {
        DestroyRecord entity = new DestroyRecord();
        BeanUtils.copyProperties(dto, entity);
        return destroyRecordMapper.updateDestroyRecord(entity);
    }

    /**
     * 删除销毁记录
     */
    @Override
    @Transactional
    public int deleteDestroyRecordById(Long id) {
        // 删除前检查状态，如果是待下架状态，需要恢复档案状态
        DestroyRecord destroyRecord = destroyRecordMapper.selectDestroyRecordById(id);
        if (destroyRecord != null && "待下架".equals(destroyRecord.getStatus())) {
            Archive archive = archiveMapper.selectArchiveById(destroyRecord.getArchiveId());
            if (archive != null) {
                archive.setAvailability(0); // 恢复为在架状态
                archiveMapper.updateArchive(archive);
            }
        }

        return destroyRecordMapper.deleteDestroyRecordById(id);
    }

    /**
     * 批量删除销毁记录
     */
    @Override
    public int deleteDestroyRecordByIds(Long[] ids) {
        for (Long id : ids) {
            deleteDestroyRecordById(id);
        }
        return ids.length;
    }

    /**
     * 转换为包含关联信息的DTO
     */
    private DestroyRecordDTO convertToDTO(DestroyRecord entity) {
        DestroyRecordDTO dto = new DestroyRecordDTO();
        BeanUtils.copyProperties(entity, dto);

        // 补充档案信息
        Archive archive = archiveMapper.selectArchiveById(entity.getArchiveId());
        if (archive != null) {
            dto.setArchiveName(archive.getName());
            dto.setArchiveDangHao(archive.getDanghao());
            dto.setArchiveLocation(archive.getShitiLocation() + "-" + archive.getExactLocation());
            dto.setArchiveRfid(archive.getRfid());
            dto.setArchiveAvailability(archive.getAvailability());
        }

        // 解析purpose字段到表单数据（用于编辑时回显）
        dto.parsePurposeToFormData();

        return dto;
    }

    /**
     * 转换为简化DTO（只包含基本信息）
     */
    private DestroyRecordDTO convertToSimpleDTO(DestroyRecord entity) {
        DestroyRecordDTO dto = new DestroyRecordDTO();
        BeanUtils.copyProperties(entity, dto);

        // 只补充必要的档案信息
        Archive archive = archiveMapper.selectArchiveById(entity.getArchiveId());
        if (archive != null) {
            dto.setArchiveName(archive.getName());
            dto.setArchiveDangHao(archive.getDanghao());
        }

        return dto;
    }
}