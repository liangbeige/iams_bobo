package com.iams.manage.domain.workflow;

import java.io.Serial;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;

@Getter
@Setter
public class FilingRecord extends BaseEntity
{
    @Serial
    private static final long serialVersionUID = 1L;

    /** 自增ID */
    private Long id;

    /** 用户ID */
    @Excel(name = "用户ID")
    @NotNull
    private Long userId;

    /** 档案ID */
    @Excel(name = "档案ID")
    @NotNull
    private Long archiveId;

    /** 归档原因 */
    @Excel(name = "归档原因")
    private String purpose;

    /** 申请状态 */
    @Excel(name = "申请状态")
    private String status;

    /** 申请启动时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "申请启动时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date startApplyTime;

    /** 申请结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "申请结束时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date endApplyTime;

    /** 流程名 */
    @Excel(name = "流程名")
    private String processName;

    /** 业务键 */
    @Excel(name = "业务键")
    private String businessKey;

    /** 实例ID */
    @Excel(name = "实例ID")
    private String instanceId;

    public FilingRecord(){

    }

    public FilingRecord(String businessKey){
        this.businessKey = businessKey;
    }

    public FilingRecord(Long userId, Long archiveId){
        this.userId = userId;
        this.archiveId = archiveId;
    }
}
