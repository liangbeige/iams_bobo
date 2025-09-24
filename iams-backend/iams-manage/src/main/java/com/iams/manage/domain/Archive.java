package com.iams.manage.domain;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;
import org.springframework.data.annotation.Id;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 档案信息对象 tb_archive
 * 
 * @author zhjm
 * @date 2025-01-10
 */
@Setter
@Getter
public class Archive extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @Excel(name = "主键ID")
    private Long id;

    /** 档号 */
    @Excel(name = "档号")
    private String danghao;

    /** 档案名称 */
    @Excel(name = "档案名称")
    private String name;

    /** 射频标签号 */
    @Excel(name = "射频标签号")
    private String rfid;

    /** 件号 */
    private Long jianhao;

    /** 保密级别 */
    @Excel(name = "保密级别")
    private String secretLevel;

    /** 保密期限 */
    @Excel(name = "保密期限")
    private String secretPeroid;

    /** 解密日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "解密日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date desecretDate;

    /** 保管期限 */
    @Excel(name = "保管期限")
    private String retentionPeriod;

    /** 载体类型 */
    @Excel(name = "载体类型")
    private String carrierType;

    /** 起始时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date createTime;

    /** 起始时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Excel(name = "起始时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date startDate;

    /** 终止时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Excel(name = "终止时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date endDate;

    /** 归档时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Excel(name = "归档时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date guidangTime;

    /** 实体档案的档案柜位置 */
    private String shitiLocation;

    /** 档案的具体位置 */
    private String exactLocation;

    /** 电子档案位置 */
    private String dianziLocation;

    /** 档案目录 */
    private String directory;

    /** 档案简介 */
    private String description;

    /** 形成单位 */
    @Excel(name = "形成单位")
    private String formationUnit;

    /** 移交单位 */
    @Excel(name = "移交单位")
    private String transferUnit;

    /** 文件(档)数量 */
    private Long documentCount;

    /** 档案状态 */
    @Excel(name = "档案状态")
    private String status;

    /** 真实性 */
    private Integer authenticity;

    /** 完整性 */
    private Integer integrity;

    /** 可用性 */
    private Integer availability;

    /** 安全性 */
    private Integer security;

    /** 档案门类 --应该用categoryCode，但是改起来好像有些麻烦*/
    @Excel(name = "档案门类")
    private String categoryId;// 实际数据库字段

    /** 大门类编码（用于搜索） */
    private String rootCategoryCode;

    /** 小门类编码（用于搜索） */
    private String categoryCode;

    /** 档案全宗 */
    @Excel(name = "档案全宗id")
    private Long fondsId;

    /** 全宗名称（关联查询字段） */
    private String fondsName;

    /** 额外信息 */
    @Excel(name = "额外信息")
    private String extraInfo;

    /** 所属项目ID */
    @Excel(name = "所属项目ID")
    private Long projectId;

    @Excel(name = "项目名称")
    @TableField(exist = false)
    private String projectName;

    @Excel(name = "项目代号")
    @TableField(exist = false)  // 重要：这不是数据库字段
    private String projectBianhao;

    /** 是否有销毁佐证材料 (0=否, 1=是) */
    private Boolean hasDestructionCertificate;

    /** 销毁佐证材料存储路径/URL */
    private String destructionCertificateUrl;


    private String juanhao;

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("danghao", getDanghao())
            .append("name", getName())
            .append("rfid", getRfid())
            .append("jianhao", getJianhao())
            .append("secretLevel", getSecretLevel())
            .append("secretPeroid", getSecretPeroid())
            .append("desecretDate", getDesecretDate())
            .append("retentionPeriod", getRetentionPeriod())
            .append("carrierType", getCarrierType())
            .append("startDate", getStartDate())
            .append("endDate", getEndDate())
            .append("guidangTime", getGuidangTime())
            .append("shitiLocation", getShitiLocation())
            .append("exactLocation", getExactLocation())
            .append("dianziLocation", getDianziLocation())
            .append("directory", getDirectory())
            .append("description", getDescription())
            .append("formationUnit", getFormationUnit())
            .append("transferUnit", getTransferUnit())
            .append("documentCount", getDocumentCount())
            .append("status", getStatus())
            .append("authenticity", getAuthenticity())
            .append("integrity", getIntegrity())
            .append("availability", getAvailability())
            .append("security", getSecurity())
            .append("categoryId", getCategoryId())
            .append("fondsId", getFondsId())
            .append("createTime", getCreateTime())
            .append("createBy", getCreateBy())
            .append("updateTime", getUpdateTime())
            .append("updateBy", getUpdateBy())
            .append("remark", getRemark())
            .append("extraInfo", getExtraInfo())
            .toString();
    }

    public Archive(){}

    public Archive(Long projectId, String juanhao){
        this.projectId = projectId;
        this.juanhao = juanhao;
    }

    public void setProjectInfo(Project project) {
        if (project != null) {
            this.projectName = project.getName();
            this.projectBianhao = project.getBianhao();
        }
    }

    // getter和setter方法
    public String getRootCategoryCode() {
        return rootCategoryCode;
    }

    public void setRootCategoryCode(String rootCategoryCode) {
        this.rootCategoryCode = rootCategoryCode;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    // Getter 和 Setter 方法
    public String getFondsName() {
        return fondsName;
    }

    public void setFondsName(String fondsName) {
        this.fondsName = fondsName;
    }


    public Boolean getHasDestructionCertificate() {
        return hasDestructionCertificate;
    }

    public void setHasDestructionCertificate(Boolean hasDestructionCertificate) {
        this.hasDestructionCertificate = hasDestructionCertificate;
    }

    public String getDestructionCertificateUrl() {
        return destructionCertificateUrl;
    }

    public void setDestructionCertificateUrl(String destructionCertificateUrl) {
        this.destructionCertificateUrl = destructionCertificateUrl;
    }
}
