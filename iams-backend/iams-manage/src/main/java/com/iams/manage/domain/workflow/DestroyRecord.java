package com.iams.manage.domain.workflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.Date;

/**
 * 销毁记录实体类 - 匹配简化后的数据表 tb_destroy_record
 *
 * 修改说明：
 * 1. 移除了 business_key, instance_id 字段（工作流相关）
 * 2. 移除了 applicant, scheduled_date, description, archive_count 字段（简化流程）
 * 3. 移除了 user_name, user_department, archive_name, archive_dang_hao 字段（冗余信息）
 * 4. 保留核心字段：id, user_id, archive_id, purpose, status, start_apply_time, end_apply_time, remark
 */
@Getter
@Setter
public class DestroyRecord extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 销毁记录ID
     */
    private Long id;

    /**
     * 申请用户的 ID
     */
    @Excel(name = "用户ID")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 档案ID
     */
    @Excel(name = "档案ID")
    @NotNull(message = "档案ID不能为空")
    private Long archiveId;

    /**
     * 销毁原因（包含原因类型、详细说明、申请人等合并信息）
     */
    @NotNull(message = "销毁原因不能为空")
    @Excel(name = "销毁原因")
    private String purpose;

    /**
     * 销毁状态：待下架、已销毁、销毁失败
     */
    @Excel(name = "销毁状态")
    private String status;

    /**
     * 销毁启动时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "销毁启动时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date startApplyTime;

    /**
     * 销毁完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "销毁完成时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date endApplyTime;

    /**
     * 备注
     */
    @Excel(name = "备注")
    private String remark;

    /**
     * 构造函数
     */
    public DestroyRecord() {
    }

    public DestroyRecord(Long userId, Long archiveId) {
        this.userId = userId;
        this.archiveId = archiveId;
    }

    public DestroyRecord(Long userId, Long archiveId, String purpose) {
        this.userId = userId;
        this.archiveId = archiveId;
        this.purpose = purpose;
        this.status = "待下架";
        this.startApplyTime = new Date();
    }
}