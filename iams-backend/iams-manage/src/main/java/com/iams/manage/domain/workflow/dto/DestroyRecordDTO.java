package com.iams.manage.domain.workflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 销毁记录DTO类 - 用于前端交互
 *
 * 修改说明：
 * 1. 保留核心数据库字段
 * 2. 添加前端显示需要的扩展字段（从关联表查询获取）
 * 3. 添加前端提交表单需要的临时字段
 */
@Getter
@Setter
public class DestroyRecordDTO {

    /**
     * 销毁记录ID
     */
    private Long id;

    /**
     * 申请用户的 ID
     */
    private Long userId;

    /**
     * 档案ID
     */
    @NotNull(message = "档案ID不能为空")
    private Long archiveId;

    /**
     * 销毁原因（包含合并后的完整信息）
     */
    @NotNull(message = "销毁原因不能为空")
    private String purpose;

    /**
     * 销毁状态：待下架、已销毁、销毁失败
     */
    private String status;

    /**
     * 销毁启动时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startApplyTime;

    /**
     * 销毁完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endApplyTime;

    /**
     * 备注
     */
    private String remark;

    // ========== 前端表单字段（用于提交申请时的临时数据） ==========

    /**
     * 销毁原因类型（前端选择框的值，如：保管期限到期、档案损坏等）
     */
    private String reason;

    /**
     * 详细说明（前端文本框输入）
     */
    private String description;

    /**
     * 申请人姓名（前端选择框的值）
     */
    private String applicant;

    /**
     * 预计销毁时间（前端日期选择器的值）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date scheduledDate;

    // ========== 前端显示扩展字段（从关联表查询） ==========

    /**
     * 用户姓名（从用户表查询）
     */
    private String userName;

    /**
     * 用户部门（从用户表查询）
     */
    private String userDepartment;

    /**
     * 档案名称（从档案表查询）
     */
    private String archiveName;

    /**
     * 档案档号（从档案表查询）
     */
    private String archiveDangHao;

    /**
     * 档案数量（用于批量销毁时的统计）
     */
    private Integer archiveCount;

    /**
     * 档案位置信息（从档案表查询）
     */
    private String archiveLocation;

    /**
     * 档案RFID信息（从档案表查询）
     */
    private String archiveRfid;

    /**
     * 档案可用性状态（从档案表查询）
     */
    private Integer archiveAvailability;

    /**
     * 构造函数
     */
    public DestroyRecordDTO() {
    }

    /**
     * 从前端表单数据构建purpose字段的工具方法
     */
    public void buildPurposeFromFormData() {
        if (reason != null && description != null && applicant != null) {
            this.purpose = String.format("销毁原因：%s\n详细说明：%s\n申请人：%s",
                    reason, description, applicant);

            if (scheduledDate != null) {
                this.purpose += String.format("\n预计销毁时间：%s",
                        new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(scheduledDate));
            }
        }
    }

    /**
     * 解析purpose字段到表单数据的工具方法（用于编辑时回显）
     */
    public void parsePurposeToFormData() {
        if (purpose != null && purpose.contains("销毁原因：")) {
            String[] lines = purpose.split("\n");
            for (String line : lines) {
                if (line.startsWith("销毁原因：")) {
                    this.reason = line.substring(5);
                } else if (line.startsWith("详细说明：")) {
                    this.description = line.substring(5);
                } else if (line.startsWith("申请人：")) {
                    this.applicant = line.substring(4);
                }
            }
        }
    }
}