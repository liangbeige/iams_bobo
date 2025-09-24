package com.iams.manage.domain;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 文档信息对象 tb_document
 * 
 * @author zhjm
 * @date 2025-01-10
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Document extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 所属档案 */
    @Excel(name = "所属档案")
    private Long archiveId;

    /** 所属档案档号 */
    @Excel(name = "所属档案档号")
    private String archivedanghao;

    /** 所在目录 */
    @Excel(name = "所在目录")
    private Long directory;

    /** 目录内序号 */
    private Long xuhao;

    /** 文件名称 */
    @Excel(name = "文件名称")
    private String name;

    /** 文件门类 */
    @Excel(name = "文件门类")
    private String categoryCode;

    /** 文件门类 */
    private String categoryName;

    /** 文件类型 */
    @Excel(name = "文件类型")
    private String fileType;

    /** 文件创始人 */
    @Excel(name = "文件创始人")
    private String createBy;

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

    @Excel(name = "额外信息")
    private String extraInfo;

    /**  minio 文件名*/
    private String minioName;

    private Long projectId;

    /**
     * 排序字段
     */
    private String orderByColumn;

    /**
     * 排序方向
     */
    private String isAsc;

    /**
     * 元数据ids
     */
    private String metadataIds;

    /**
     * 元数据title内容
     */
    private String title;

    /**
     * 元数据number内容
     */
    private String number;

    /**
     * 元数据unit内容
     */
    private String unit;

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("archiveId", getArchiveId())
            .append("projectId", getProjectId())
            .append("directory", getDirectory())
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
            .append("category", getCategoryName())
            .append("extraInfo", getExtraInfo())
            .append("metadataIds", getMetadataIds())
            .append("title", getTitle())
            .append("number", getNumber())
            .append("unit", getUnit())
            .toString();
    }

    public Document(){};

    public Document(Long id)
    {
        this.archiveId = id;
    }

}
