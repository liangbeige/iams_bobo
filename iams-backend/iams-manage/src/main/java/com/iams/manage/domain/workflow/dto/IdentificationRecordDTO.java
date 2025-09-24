package com.iams.manage.domain.workflow.dto;

import com.iams.common.annotation.Excel;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 档案鉴定记录DTO
 *
 * @author LiuTao
 * @date 2025-06-26
 */
@Getter
@Setter
public class IdentificationRecordDTO {

    /** 档案ID（单个） */
    private Long archiveId;

    /** 档案ID列表（批量） */
    private List<Long> archiveIds;

    /** 鉴定人员 */
    private String appraiser;

    /** 鉴定后保管期限 */
    private String afterRetentionPeriod;

    /** 鉴定后保密级别 */
    private String afterSecretLevel;

    /** 鉴定后保密期限 */
    private String afterSecretPeriod;

    /** 鉴定结果：archive-保存，destroy-销毁 */
    private String appraisalResult;

    /** 鉴定依据 */
    private String appraisalReason;

    @Override
    public String toString() {
        return "IdentificationRecordDTO{" +
                "archiveIds=" + archiveIds +
                ", archiveId=" + archiveId +
                ", appraiser='" + appraiser + '\'' +
                ", afterRetentionPeriod='" + afterRetentionPeriod + '\'' +
                ", afterSecretLevel='" + afterSecretLevel + '\'' +
                ", afterSecretPeriod='" + afterSecretPeriod + '\'' +
                ", appraisalResult='" + appraisalResult + '\'' +
                ", appraisalReason='" + appraisalReason + '\'' +
                '}';
    }
}