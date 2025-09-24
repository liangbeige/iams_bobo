package com.iams.manage.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iams.common.utils.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;

/**
 * 全宗管理对象 tb_fonds
 * 
 * @author zhjm
 * @date 2025-01-05
 */
public class Fonds extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 全宗ID */
    @Excel(name = "全宗ID")
    private Long id;

    /** 全宗编号 */
    @Excel(name = "全宗编号")
    private String bianhao;

    /** 全宗名称 */
    @Excel(name = "全宗名称")
    private String name;

    /** 起始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "起始时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date startDate;

    /** 终止时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "终止时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date endDate;

    /** 全宗简介 */
    private String description;

    /** 形成单位 */
    @Excel(name = "形成单位")
    private String formationUnit;

    /** 移交单位 */
    private String transferUnit;

    /** 保管期限 */
    @Excel(name = "保管期限")
    private String retentionPeriod;

    /** 档案数量 */
    @Excel(name = "档案数量")
    private Long archiveCount;

    /** 全宗状态 */
    @Excel(name = "全宗状态")
    private String status;

    /** 门类代码(逗号分隔) */
    private String categoryCodes;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setBianhao(String bianhao) 
    {
        this.bianhao = bianhao;
    }

    public String getBianhao() 
    {
        return bianhao;
    }
    public void setName(String name) 
    {
        this.name = name;
    }

    public String getName() 
    {
        return name;
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
    public void setRetentionPeriod(String retentionPeriod) 
    {
        this.retentionPeriod = retentionPeriod;
    }

    public String getRetentionPeriod() 
    {
        return retentionPeriod;
    }
    public void setArchiveCount(Long archiveCount) 
    {
        this.archiveCount = archiveCount;
    }

    public Long getArchiveCount()
    {
        return archiveCount == null ? 0L : archiveCount;
    }
    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("bianhao", getBianhao())
            .append("name", getName())
            .append("startDate", getStartDate())
            .append("endDate", getEndDate())
            .append("description", getDescription())
            .append("formationUnit", getFormationUnit())
            .append("transferUnit", getTransferUnit())
            .append("retentionPeriod", getRetentionPeriod())
            .append("archiveCount", getArchiveCount())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .append("createBy", getCreateBy())
            .append("updateTime", getUpdateTime())
            .append("updateBy", getUpdateBy())
            .append("remark", getRemark())
            .toString();
    }

    // getter 和 setter 方法
    public String getCategoryCodes() {
        return categoryCodes;
    }

    public void setCategoryCodes(String categoryCodes) {
        this.categoryCodes = categoryCodes;
    }

    // 工具方法：获取门类代码列表
    public List<String> getCategoryCodeList() {
        if (StringUtils.isBlank(categoryCodes)) {
            return new ArrayList<>();
        }

        return Arrays.stream(categoryCodes.split(","))
                .filter(StringUtils::isNotBlank)
                .map(item -> item.split(":")[0].trim()) // 只提取code部分
                .collect(Collectors.toList());
    }

    // 新增方法：获取完整的配置项（code:name）
    public List<String> getCategoryConfigItems() {
        if (StringUtils.isBlank(categoryCodes)) {
            return new ArrayList<>();
        }

        return Arrays.stream(categoryCodes.split(","))
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .collect(Collectors.toList());
    }

    // 工具方法：设置门类代码列表
    public void setCategoryCodeList(List<String> codeList) {
        if (codeList == null || codeList.isEmpty()) {
            this.categoryCodes = null;
        } else {
            this.categoryCodes = String.join(",", codeList);
        }
    }
}
