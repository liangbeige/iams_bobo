package com.iams.manage.domain;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;

public class ArchiveBrowseLog extends BaseEntity{
    private Long id; // 主键

    @Excel(name = "档案档号")
    @JsonProperty("archive_danghao") // 添加Jackson注解
    @TableField("archive_danghao")
    private String archiveDanghao; // 档案档号

    @Excel(name = "档案ID")
    @JsonProperty("archive_id") // 添加Jackson注解
    @TableField("archive_id")
    private Long archiveId;

    @Excel(name = "档案名称")// 所属档案ID
    @JsonProperty("archive_name") // 添加Jackson注解
    @TableField("archive_name")
    private String archiveName;

    @Excel(name = "查看人ID")// 所属档案名称
    @JsonProperty("viewer_id") // 添加Jackson注解
    @TableField("viewer_id")
    private Long viewerId;

    @Excel(name = "查看人名称")// 查看人ID
    @JsonProperty("viewer_name") // 添加Jackson注解
    @TableField("viewer_name")
    private String viewerName;

    @Excel(name = "查看开始时间")// 查看人名称
    @JsonProperty("start_time") // 添加Jackson注解
    @TableField("start_time")
    private Date startTime;

    @Excel(name = "查看结束时间")// 查看开始时间
    @JsonProperty("end_time") // 添加Jackson注解
    @TableField("end_time")
    private Date endTime;

    @Excel(name = "查看时长")// 查看结束时间
    private Integer duration;

    @Excel(name = "记录创建时间")// 查看时长（秒）
    @JsonProperty("created_at") // 添加Jackson注解
    @TableField("created_at")
    private Date createdAt;

    @Excel(name = "文档名称")// 记录创建时间
    @JsonProperty("document_name") // 添加Jackson注解
    @TableField("document_name")
    private String documentName;

    @Excel(name = "文档id")// 记录创建时间
    @JsonProperty("document_id") // 添加Jackson注解
    @TableField("document_id")
    private Long  documentId;

    // 必须有无参构造函数
    public ArchiveBrowseLog() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getArchiveDanghao() {
        return archiveDanghao;
    }

    public void setArchiveDanghao(String archiveDanghao) {
        this.archiveDanghao = archiveDanghao;
    }

    public Long getArchiveId() {
        return archiveId;
    }

    public void setArchiveId(Long archiveId) {
        this.archiveId = archiveId;
    }

    public String getArchiveName() {
        return archiveName;
    }

    public void setArchiveName(String archiveName) {
        this.archiveName = archiveName;
    }

    public Long getViewerId() {
        return viewerId;
    }

    public void setViewerId(Long viewerId) {
        this.viewerId = viewerId;
    }

    public String getViewerName() {
        return viewerName;
    }

    public void setViewerName(String viewerName) {
        this.viewerName = viewerName;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    // getter 和 setter
    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }
}
