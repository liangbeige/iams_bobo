package com.iams.manage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;

/**
 * 项目管理对象 tb_project
 * 
 * @author zhld
 * @date 2025-06-01
 */
public class Project extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 项目ID */
    private Long id;

    /** 项目名称 */
    @Excel(name = "项目名称")
    private String name;

    /** 项目编码 */
    @Excel(name = "项目编码")
    private String bianhao;

    /** 项目描述 */
    @Excel(name = "项目描述")
    private String description;

    /** 项目负责人 */
    @Excel(name = "项目负责人")
    private String leader;

    /** 项目起始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "项目起始时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date startTime;

    /** 项目终止时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "项目终止时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date endTime;

    /** 档案数量 */
    @Excel(name = "档案数量")
    private Long archiveCount;

    private Boolean hasCertificate;

    private String certUrl;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setName(String name) 
    {
        this.name = name;
    }

    public String getName() 
    {
        return name;
    }
    public void setBianhao(String bianhao) 
    {
        this.bianhao = bianhao;
    }

    public String getBianhao() 
    {
        return bianhao;
    }
    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }
    public void setLeader(String leader) 
    {
        this.leader = leader;
    }

    public String getLeader() 
    {
        return leader;
    }
    public void setStartTime(Date startTime) 
    {
        this.startTime = startTime;
    }

    public Date getStartTime() 
    {
        return startTime;
    }
    public void setEndTime(Date endTime) 
    {
        this.endTime = endTime;
    }

    public Date getEndTime() 
    {
        return endTime;
    }
    public void setArchiveCount(Long archiveCount) 
    {
        this.archiveCount = archiveCount;
    }

    public Long getArchiveCount() 
    {
        return archiveCount;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("name", getName())
            .append("bianhao", getBianhao())
            .append("description", getDescription())
            .append("leader", getLeader())
            .append("startTime", getStartTime())
            .append("endTime", getEndTime())
            .append("archiveCount", getArchiveCount())
            .append("createTime", getCreateTime())
            .append("createBy", getCreateBy())
            .append("updateTime", getUpdateTime())
            .append("updateBy", getUpdateBy())
            .append("remark", getRemark())
            .append("hasCertificate", getHasCertificate())
            .toString();
    }

    public Boolean getHasCertificate() {
        return hasCertificate;
    }

    public void setHasCertificate(Boolean hasCertificate) {
        this.hasCertificate = hasCertificate;
    }

    public String getCertUrl() {
        return certUrl;
    }

    public void setCertUrl(String certUrl) {
        this.certUrl = certUrl;
    }
}
