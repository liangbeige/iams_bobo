package com.iams.manage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;

/**
 * 档案日志对象 tb_archive_log
 * 
 * @author LiuTao
 * @date 2025-04-04
 */
public class ArchiveLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 档案ID */
    @Excel(name = "档案ID")
    private Long archiveId;

    /** 档案类型 */
    @Excel(name = "备注")
    // 新增字段：存储借阅记录的备注
    private String borrowRemark;

    /** 任务类型 */
    @Excel(name = "任务类型")
    private String taskType;

    /** 发起人 */
    @Excel(name = "发起人")
    private String initiator;

    /** 经办人 */
    @Excel(name = "审核员")
    private String handler;

    /** 起始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @Excel(name = "起始时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date startDate;

    /** 终止时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @Excel(name = "终止时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date endDate;

    /** 任务状态 */
    @Excel(name = "任务状态")
    private String taskStatus;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setArchiveId(Long archiveId) 
    {
        this.archiveId = archiveId;
    }

    public Long getArchiveId() 
    {
        return archiveId;
    }
    public void setTaskType(String taskType) 
    {
        this.taskType = taskType;
    }

    public String getTaskType() 
    {
        return taskType;
    }
    public void setInitiator(String initiator) 
    {
        this.initiator = initiator;
    }

    public String getInitiator() 
    {
        return initiator;
    }
    public void setHandler(String handler) 
    {
        this.handler = handler;
    }

    public String getHandler() 
    {
        return handler;
    }
    public void setStartDate(Date startDate) 
    {
        this.startDate = startDate;
    }

    public Date getStartDate() 
    {
        return startDate;
    }
    public void setEndDate(Date endDate) 
    {
        this.endDate = endDate;
    }

    public Date getEndDate() 
    {
        return endDate;
    }
    public void setTaskStatus(String taskStatus) 
    {
        this.taskStatus = taskStatus;
    }

    public String getTaskStatus() 
    {
        return taskStatus;
    }
    // getter 和 setter
    public String getBorrowRemark() {
        return borrowRemark;
    }

    public void setBorrowRemark(String borrowRemark) {
        this.borrowRemark = borrowRemark;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("archiveId", getArchiveId())
            .append("taskType", getTaskType())
            .append("initiator", getInitiator())
            .append("handler", getHandler())
            .append("startDate", getStartDate())
            .append("endDate", getEndDate())
            .append("taskStatus", getTaskStatus())
            .append("remark", getRemark())
            .toString();
    }
}
