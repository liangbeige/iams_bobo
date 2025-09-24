package com.iams.manage.service;


import com.iams.manage.controller.ArchiveDestroyApprovalController;
import com.iams.manage.controller.ArchiveDestroyApprovalController;
import com.iams.manage.controller.ArchiveDestroyApprovalController;
import com.iams.manage.domain.workflow.DestroyRecord;
import com.iams.manage.domain.workflow.dto.DestroyRecordDTO;

import java.util.List;

/**
 * 档案销毁Service接口
 *
 * @author LiuTao
 * @date 2025-03-18
 */
public interface IDestroyRecordService {

    /**
     * 查询销毁记录（返回DTO，包含关联信息）
     */
    DestroyRecordDTO selectDestroyRecordById(Long id);

    /**
     * 查询销毁记录列表（返回DTO列表，包含关联信息）
     */
    List<DestroyRecordDTO> selectDestroyRecordList(DestroyRecord destroyRecord);

    /**
     * 查询简单销毁记录（只包含基本信息）
     */
    DestroyRecordDTO selectSimpleDestroyRecordById(Long id);

    /**
     * 查询简单销毁记录列表（只包含基本信息）
     */
    List<DestroyRecordDTO> selectSimpleDestroyRecordList(DestroyRecord destroyRecord);

    /**
     * 提交销毁申请（单个档案）
     * - 将档案状态设置为待下架 (availability = 1)
     * - 创建销毁记录，状态为"待下架"
     */
    int submitDestroyApplication(DestroyRecordDTO destroyRecordDTO);

    /**
     * 批量提交销毁申请
     * - 批量处理多个档案的销毁申请
     */
    int batchSubmitDestroyApplication(ArchiveDestroyApprovalController.BatchDestroyRequest request);

    /**
     * 确认完成销毁（第二步：实际销毁操作）
     * - 将销毁记录状态改为"已销毁"
     * - 将档案状态改为"Destroyed"
     * - 更新档案的availability状态
     */
    int completeDestroy(Long archiveId);

    /**
     * 标记销毁失败
     * - 将销毁记录状态改为"销毁失败"
     * - 恢复档案的availability状态
     */
    int failDestroy(Long archiveId, String reason);

    /**
     * 取消销毁申请（仅限待下架状态）
     * - 删除销毁记录
     * - 恢复档案的availability状态为在架(0)
     */
    int cancelDestroyApplication(Long id);

    /**
     * 修改销毁记录
     */
    int updateDestroyRecord(DestroyRecordDTO destroyRecordDTO);

    /**
     * 删除销毁记录
     */
    int deleteDestroyRecordById(Long id);

    /**
     * 批量删除销毁记录
     */
    int deleteDestroyRecordByIds(Long[] ids);

    /**
     * RFID同步完成销毁（盘点车离线操作后的同步）
     * - 更新档案的RFID状态（05->00）
     * - 更新档案的availability状态
     * 注：此方法可能需要在RFID同步模块中调用
     */
    int syncOfflineDestroyOperation(Long archiveId);

    /**
     * 根据档案ID撤销销毁申请
     * @param archiveId 档案ID
     * @return 结果
     */
    int cancelDestroyByArchiveId(Long archiveId);
}
