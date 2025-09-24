package com.iams.manage.domain.workflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iams.manage.domain.Archive;
import com.iams.common.annotation.Excel;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;


@Getter
@Setter
public class PendingEvaluationDTO {

    /** 档案ID */
    @Excel(name = "档案ID")
    private Long archiveId;

    /** 档案名 */
    @Excel(name = "档案名")
    private String archiveName;

    @Excel(name = "档案档号")
    private String archiveDangHao;

    /** 归档时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "归档时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date guidangTime;

    /** 保密级别 */
    @Excel(name = "保密级别")
    private String secretLevel;

    /** 保密期限 */
    @Excel(name = "保密期限")
    private String secretPeroid;

    /** 保管期限 */
    @Excel(name = "保管期限")
    private String retentionPeriod;

    /** 鉴定原因 */
    @Excel(name = "鉴定原因")
    private String evaluationReason;

    /** 任务状态 */
    @Excel(name = "任务状态")
    private String status;


}
