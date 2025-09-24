package com.iams.elasticsearch.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import java.util.Date;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ArchiveDocument {
    @Field(type = FieldType.Keyword)
    private String docId;       // 文档唯一标识

    @Field(type = FieldType.Keyword)
    private String docType;     // 文档类型：入学/财务等

    @Field(type = FieldType.Text, analyzer = "chinese_analyzer", searchAnalyzer = "ik_smart")
    private String docContent;  // 文档内容

    @Field(type = FieldType.Text)
    private String docTitle;    // 文档标题

//    @Field(type = FieldType.Date,format = DateFormat.date_time)
//    private Date createTime;    // 文档创建时间

    @Field(type = FieldType.Text)
    private String filePath;    // 文件路径

    @Field(type = FieldType.Keyword)
    private String  mysqlDanghao; // 关联档案的数据库 ID


    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String number;

    @Field(type = FieldType.Text)
    private String unit;


    // Getter & Setter
    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getDocContent() {
        return docContent;
    }

    public void setDocContent(String docContent) {
        this.docContent = docContent;
    }

    public String getDocTitle() {
        return docTitle;
    }

    public void setDocTitle(String docTitle) {
        this.docTitle = docTitle;
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

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }


    @Override
    public String toString() {
        return "ArchiveDocument{" +
                "docId='" + docId + '\'' +
                ", docType='" + docType + '\'' +
                ", docContent='" + docContent + '\'' +
                ", docTitle='" + docTitle + '\'' +
//                ", createTime=" + createTime +
                ", filePath='" + filePath + '\'' +
                ", title='" + title + '\'' +
                ", number='" + number + '\'' +
                ", unit='" + unit + '\'' +
                '}';
    }
}
