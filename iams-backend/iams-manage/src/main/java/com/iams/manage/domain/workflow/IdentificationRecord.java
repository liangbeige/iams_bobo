package com.iams.manage.domain.workflow;

import java.io.Serial;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;

/**
 * 档案鉴定记录对象 tb_identification_record
 *
 * @author LiuTao
 * @date 2025-06-26
 */
@Setter
@Getter
public class IdentificationRecord extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 自增ID */
    private Long id;

    /** 档案ID */
    @Excel(name = "档案ID")
    private Long archiveId;

    /** 档号 */
    @Excel(name = "档号")
    private String danghao;

    /** 档案名称 */
    @Excel(name = "档案名称")
    private String archiveName;

    /** 鉴定人员 */
    @Excel(name = "鉴定人员")
    private String appraiser;

    /** 鉴定时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "鉴定时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date appraisalTime;

    /** 鉴定前保管期限 */
    @Excel(name = "鉴定前保管期限")
    private String beforeRetentionPeriod;

    /** 鉴定前保密级别 */
    @Excel(name = "鉴定前保密级别")
    private String beforeSecretLevel;

    /** 鉴定前保密期限 */
    @Excel(name = "鉴定前保密期限")
    private String beforeSecretPeriod;

    /** 鉴定后保管期限 */
    @Excel(name = "鉴定后保管期限")
    private String afterRetentionPeriod;

    /** 鉴定后保密级别 */
    @Excel(name = "鉴定后保密级别")
    private String afterSecretLevel;

    /** 鉴定后保密期限 */
    @Excel(name = "鉴定后保密期限")
    private String afterSecretPeriod;

    /** 鉴定结果：archive-保存，destroy-销毁 */
    @Excel(name = "鉴定结果")
    private String appraisalResult;

    /** 鉴定依据 */
    @Excel(name = "鉴定依据")
    private String appraisalReason;

    /** 鉴定原因（系统生成的原因：如缺失字段、过期等） */
    @Excel(name = "鉴定原因")
    private String purpose;

    /** 查询开始时间（非数据库字段） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date beginAppraisalTime;
    /** 查询结束时间（非数据库字段） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endAppraisalTime;


    public IdentificationRecord() {
    }

    public IdentificationRecord(Long archiveId) {
        this.archiveId = archiveId;
    }

    public IdentificationRecord(Long archiveId, String appraiser) {
        this.archiveId = archiveId;
        this.appraiser = appraiser;
    }
}