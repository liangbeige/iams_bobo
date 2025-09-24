package com.iams.manage.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;

/**
 * 档案盘点主表对象 tb_archive_inventory
 *
 * @author ruoyi
 * @date 2025-06-23
 */
public class ArchiveInventory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 盘点时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "盘点时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date inventoryTime;

    /** 应盘点总数 */
    @Excel(name = "应盘点总数")
    private Integer totalCount;

    /** 实际盘点数 */
    @Excel(name = "实际盘点数")
    private Integer scannedCount;

    /** 盘点率(%) */
    @Excel(name = "盘点率(%)")
    private BigDecimal inventoryRate;

    /** 盘点人员 */
    @Excel(name = "盘点人员")
    private String operator;

    /** 备注 */
    @Excel(name = "备注")
    private String remark;

    /** 盘点明细列表 */
    private List<ArchiveInventoryDetail> archiveInventoryDetailList;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setInventoryTime(Date inventoryTime)
    {
        this.inventoryTime = inventoryTime;
    }

    public Date getInventoryTime()
    {
        return inventoryTime;
    }

    public void setTotalCount(Integer totalCount)
    {
        this.totalCount = totalCount;
    }

    public Integer getTotalCount()
    {
        return totalCount;
    }

    public void setScannedCount(Integer scannedCount)
    {
        this.scannedCount = scannedCount;
    }

    public Integer getScannedCount()
    {
        return scannedCount;
    }

    public void setInventoryRate(BigDecimal inventoryRate)
    {
        this.inventoryRate = inventoryRate;
    }

    public BigDecimal getInventoryRate()
    {
        return inventoryRate;
    }

    public void setOperator(String operator)
    {
        this.operator = operator;
    }

    public String getOperator()
    {
        return operator;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public String getRemark()
    {
        return remark;
    }

    public List<ArchiveInventoryDetail> getArchiveInventoryDetailList()
    {
        return archiveInventoryDetailList;
    }

    public void setArchiveInventoryDetailList(List<ArchiveInventoryDetail> archiveInventoryDetailList)
    {
        this.archiveInventoryDetailList = archiveInventoryDetailList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("inventoryTime", getInventoryTime())
                .append("totalCount", getTotalCount())
                .append("scannedCount", getScannedCount())
                .append("inventoryRate", getInventoryRate())
                .append("operator", getOperator())
                .append("remark", getRemark())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("archiveInventoryDetailList", getArchiveInventoryDetailList())
                .toString();
    }
}