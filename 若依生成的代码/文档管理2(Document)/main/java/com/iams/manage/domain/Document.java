package com.iams.manage.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;

/**
 * 文档信息对象 tb_document
 * 
 * @author zhjm
 * @date 2025-01-10
 */
public class Document extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 所属档案 */
    @Excel(name = "所属档案")
    private Long archiveId;

    /** 目录内序号 */
    private Long xuhao;

    /** 文件名称 */
    @Excel(name = "文件名称")
    private String name;

    /** 文件类型 */
    @Excel(name = "文件类型")
    private String fileType;

    /** 文件大小(Kb) */
    @Excel(name = "文件大小(Kb)")
    private Long fileSize;

    /** 文件路径 */
    @Excel(name = "文件路径")
    private String filePath;

    /** 存放位置 */
    @Excel(name = "存放位置")
    private String fileLocation;

    /** 文件内容 */
    @Excel(name = "文件内容")
    private String content;

    /** 真实性 */
    private Integer authenticity;

    /** 完整性 */
    private Integer integrity;

    /** 可用性 */
    private Integer availability;

    /** 安全性 */
    private Integer security;

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
    public void setXuhao(Long xuhao) 
    {
        this.xuhao = xuhao;
    }

    public Long getXuhao() 
    {
        return xuhao;
    }
    public void setName(String name) 
    {
        this.name = name;
    }

    public String getName() 
    {
        return name;
    }
    public void setFileType(String fileType) 
    {
        this.fileType = fileType;
    }

    public String getFileType() 
    {
        return fileType;
    }
    public void setFileSize(Long fileSize) 
    {
        this.fileSize = fileSize;
    }

    public Long getFileSize() 
    {
        return fileSize;
    }
    public void setFilePath(String filePath) 
    {
        this.filePath = filePath;
    }

    public String getFilePath() 
    {
        return filePath;
    }
    public void setFileLocation(String fileLocation) 
    {
        this.fileLocation = fileLocation;
    }

    public String getFileLocation() 
    {
        return fileLocation;
    }
    public void setContent(String content) 
    {
        this.content = content;
    }

    public String getContent() 
    {
        return content;
    }
    public void setAuthenticity(Integer authenticity) 
    {
        this.authenticity = authenticity;
    }

    public Integer getAuthenticity() 
    {
        return authenticity;
    }
    public void setIntegrity(Integer integrity) 
    {
        this.integrity = integrity;
    }

    public Integer getIntegrity() 
    {
        return integrity;
    }
    public void setAvailability(Integer availability) 
    {
        this.availability = availability;
    }

    public Integer getAvailability() 
    {
        return availability;
    }
    public void setSecurity(Integer security) 
    {
        this.security = security;
    }

    public Integer getSecurity() 
    {
        return security;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("archiveId", getArchiveId())
            .append("xuhao", getXuhao())
            .append("name", getName())
            .append("fileType", getFileType())
            .append("fileSize", getFileSize())
            .append("filePath", getFilePath())
            .append("fileLocation", getFileLocation())
            .append("content", getContent())
            .append("authenticity", getAuthenticity())
            .append("integrity", getIntegrity())
            .append("availability", getAvailability())
            .append("security", getSecurity())
            .append("createTime", getCreateTime())
            .append("createBy", getCreateBy())
            .append("updateTime", getUpdateTime())
            .append("updateBy", getUpdateBy())
            .append("remark", getRemark())
            .toString();
    }
}
