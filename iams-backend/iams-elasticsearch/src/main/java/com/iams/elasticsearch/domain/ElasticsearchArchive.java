package com.iams.elasticsearch.domain;
import org.springframework.data.elasticsearch.annotations.DateFormat;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;


@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "archives")
public class ElasticsearchArchive {

    @Id
    private String id;

    // string类型，对应的是档号！！！
    @Field(type = FieldType.Keyword)
    private String mysqlDanghao;  // 对应MySQL中的档案ID

    @Field(type = FieldType.Text)
    private String title;  // 档案标题

    @JsonIgnore
    @Field(type = FieldType.Text)
    private String content;  // 文档内容

    @Field(type = FieldType.Text)
    private String project;

    @Field(type = FieldType.Keyword)
    public Long projectid;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String projectCode;


    @Field(type = FieldType.Keyword)
    private String fileType;  // 文件类型

    @Field(type = FieldType.Text)
    private String description;  // 档案描述

    @Field(type = FieldType.Keyword)
    private String secretLevel;  // 保密级别

//    @Field(type = FieldType.Date, format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss")
////    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")

//    @Field(type = FieldType.Date)
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
//    private Date createTime;  // 创建时间

    @Field(type = FieldType.Text)
    private String filePath;  // 文件路径

    @Field(type = FieldType.Nested)
    private List<ArchiveDocument> documents = new ArrayList<>();


    // 🔥 新增：档案状态相关字段
    @Transient  // 标记为非ES字段，从数据库获取
    private String status;           // 档案状态


    @Transient  // 标记为非ES字段，从数据库获取
    private String shitiLocation;    // 实体位置

    @Transient  // 标记为非ES字段，从数据库获取
    private String exactLocation;    // 精确位置





    @Transient
    private List<ArchiveDocument> matchedDocuments;

    // 新增：高亮结果字段
    private Map<String, List<String>> highlights;




    // getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMysqlDanghao() {
        return mysqlDanghao;
    }
    public void setMysqlDanghao(String mysqlDanghao) {
        this.mysqlDanghao = mysqlDanghao;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSecretLevel() {
        return secretLevel;
    }

    public void setSecretLevel(String secretLevel) {
        this.secretLevel = secretLevel;
    }

//    public Date getCreateTime() {
//        return createTime;
//    }
//
//    public void setCreateTime(Date createTime) {
//        this.createTime = createTime;
//    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public List<ArchiveDocument> getDocuments() {return documents;
    }

    public void setDocuments(List<ArchiveDocument> documents) {
        this.documents = documents;
    }

    public List<ArchiveDocument> getMatchedDocuments() {
        return matchedDocuments;
    }

    public void setMatchedDocuments(List<ArchiveDocument> matchedDocuments) {
        this.matchedDocuments = matchedDocuments;
    }

    // 新增：getter 和 setter 方法
    public Map<String, List<String>> getHighlights() {
        return highlights;
    }

    // 添加getter/setter
    public String getProject() {
        return project;
    }

    // Getter和Setter
    public String getProjectCode() {
        return projectCode;
    }


    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public void setProject(String project) {
        this.project = project;
    }


    public Long getProjectid() {
        return projectid;
    }
    public void setProjectid(Long projectid) {
        this.projectid = projectid;
    }

    public void setHighlights(Map<String, List<String>> highlights) {
        this.highlights = highlights;
    }

    @Override
    public String toString() {
        return "ElasticsearchArchive{" +
                "id='" + id + '\'' +
                ", mysqlDanghao='" + mysqlDanghao + '\'' +
                ", title='" + title + '\'' +
                ", fileType='" + fileType + '\'' +
                ", description='" + description + '\'' +
                ", secretLevel='" + secretLevel + '\'' +
                ", filePath='" + filePath + '\'' +
                ", status='" + status + '\'' +
                ", shitiLocation='" + shitiLocation + '\'' +
                ", exactLocation='" + exactLocation + '\'' +
                ", documents=" + documents +
                '}';
    }




    // 状态相关的getter/setter方法
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getShitiLocation() { return shitiLocation; }
    public void setShitiLocation(String shitiLocation) { this.shitiLocation = shitiLocation; }

    public String getExactLocation() { return exactLocation; }
    public void setExactLocation(String exactLocation) { this.exactLocation = exactLocation; }
}
