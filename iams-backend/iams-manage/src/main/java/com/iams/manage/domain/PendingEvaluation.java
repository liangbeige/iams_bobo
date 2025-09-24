package com.iams.manage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;

/**
 * 待鉴定信息对象 tb_pending_evaluation
 *
 * @author LiuTao
 * @date 2025-04-14
 */
public class PendingEvaluation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 档案ID */
    @Excel(name = "档案ID")
    private Long archiveId;

    /** 鉴定原因 */
    @Excel(name = "鉴定原因")
    private String evaluationReason;

    /** 任务状态 */
    @Excel(name = "任务状态")
    private String status;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date createdAt;

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
    public void setEvaluationReason(String evaluationReason)
    {
        this.evaluationReason = evaluationReason;
    }

    public String getEvaluationReason()
    {
        return evaluationReason;
    }
    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }
    public void setCreatedAt(Date createdAt)
    {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt()
    {
        return createdAt;
    }


    public PendingEvaluation()
    {

    }
    public PendingEvaluation(Long archiveId)
    {
        this.archiveId = archiveId;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("archiveId", getArchiveId())
                .append("evaluationReason", getEvaluationReason())
                .append("status", getStatus())
                .append("createdAt", getCreatedAt())
                .toString();
    }
}
