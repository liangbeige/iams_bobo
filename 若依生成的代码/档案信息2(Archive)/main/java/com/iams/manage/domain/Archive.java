package com.iams.manage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;

/**
 * 档案列表对象 tb_archive
 * 
 * @author zhjm
 * @date 2025-01-10
 */
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
    private String jianhao;

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
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "起始时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date startDate;

    /** 终止时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "终止时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date endDate;

    /** 归档时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "归档时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date guidangTime;

    /** 实体档案位置 */
    private String shitiLocation;

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

    /** 档案门类 */
    @Excel(name = "档案门类")
    private Long categoryId;

    /** 档案全宗 */
    @Excel(name = "档案全宗")
    private Long fondsId;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setDanghao(String danghao) 
    {
        this.danghao = danghao;
    }

    public String getDanghao() 
    {
        return danghao;
    }
    public void setName(String name) 
    {
        this.name = name;
    }

    public String getName() 
    {
        return name;
    }
    public void setRfid(String rfid) 
    {
        this.rfid = rfid;
    }

    public String getRfid() 
    {
        return rfid;
    }
    public void setJianhao(String jianhao) 
    {
        this.jianhao = jianhao;
    }

    public String getJianhao() 
    {
        return jianhao;
    }
    public void setSecretLevel(String secretLevel) 
    {
        this.secretLevel = secretLevel;
    }

    public String getSecretLevel() 
    {
        return secretLevel;
    }
    public void setSecretPeroid(String secretPeroid) 
    {
        this.secretPeroid = secretPeroid;
    }

    public String getSecretPeroid() 
    {
        return secretPeroid;
    }
    public void setDesecretDate(Date desecretDate) 
    {
        this.desecretDate = desecretDate;
    }

    public Date getDesecretDate() 
    {
        return desecretDate;
    }
    public void setRetentionPeriod(String retentionPeriod) 
    {
        this.retentionPeriod = retentionPeriod;
    }

    public String getRetentionPeriod() 
    {
        return retentionPeriod;
    }
    public void setCarrierType(String carrierType) 
    {
        this.carrierType = carrierType;
    }

    public String getCarrierType() 
    {
        return carrierType;
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
    public void setGuidangTime(Date guidangTime) 
    {
        this.guidangTime = guidangTime;
    }

    public Date getGuidangTime() 
    {
        return guidangTime;
    }
    public void setShitiLocation(String shitiLocation) 
    {
        this.shitiLocation = shitiLocation;
    }

    public String getShitiLocation() 
    {
        return shitiLocation;
    }
    public void setDianziLocation(String dianziLocation) 
    {
        this.dianziLocation = dianziLocation;
    }

    public String getDianziLocation() 
    {
        return dianziLocation;
    }
    public void setDirectory(String directory) 
    {
        this.directory = directory;
    }

    public String getDirectory() 
    {
        return directory;
    }
    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }
    public void setFormationUnit(String formationUnit) 
    {
        this.formationUnit = formationUnit;
    }

    public String getFormationUnit() 
    {
        return formationUnit;
    }
    public void setTransferUnit(String transferUnit) 
    {
        this.transferUnit = transferUnit;
    }

    public String getTransferUnit() 
    {
        return transferUnit;
    }
    public void setDocumentCount(Long documentCount) 
    {
        this.documentCount = documentCount;
    }

    public Long getDocumentCount() 
    {
        return documentCount;
    }
    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
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
    public void setCategoryId(Long categoryId) 
    {
        this.categoryId = categoryId;
    }

    public Long getCategoryId() 
    {
        return categoryId;
    }
    public void setFondsId(Long fondsId) 
    {
        this.fondsId = fondsId;
    }

    public Long getFondsId() 
    {
        return fondsId;
    }

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
            .toString();
    }
}
