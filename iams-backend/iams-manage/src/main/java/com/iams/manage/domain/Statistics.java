package com.iams.manage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;

/**
 * 统计数据对象 tb_statistics
 * 
 * @author LiuTao
 * @date 2025-04-04
 */
public class Statistics extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 记录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "记录时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date statDate;

    /** 总借阅 */
    @Excel(name = "总借阅")
    private Long totalBorrows;

    /** 总归还 */
    @Excel(name = "总归还")
    private Long totalReturns;

    /** 总借出 */
    @Excel(name = "总借出")
    private Long totalLoans;

    /** 总丢失 */
    @Excel(name = "总丢失")
    private Long totalLost;

    /** 归档总数 */
    @Excel(name = "归档总数")
    private Long archivedCount;

    /** 待归档数 */
    @Excel(name = "待归档数")
    private Long pendingCount;

    /** 电子档 */
    @Excel(name = "电子档")
    private Long electronicCount;

    /** 物理档案 */
    @Excel(name = "物理档案")
    private Long physicalCount;

    /** 活跃用户 */
    @Excel(name = "活跃用户")
    private String activeUserList;

    /** 档案柜数量 */
    @Excel(name = "档案柜数量")
    private Long cabinetCount;

    /** 库房数量 */
    @Excel(name = "库房数量")
    private Long repositoryCount;

    /** 库房档案柜列表 */
    @Excel(name = "库房档案柜列表")
    private String seriesData;

    /** 统计日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "统计日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date createdAt;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setStatDate(Date statDate) 
    {
        this.statDate = statDate;
    }

    public Date getStatDate() 
    {
        return statDate;
    }
    public void setTotalBorrows(Long totalBorrows) 
    {
        this.totalBorrows = totalBorrows;
    }

    public Long getTotalBorrows() 
    {
        return totalBorrows;
    }
    public void setTotalReturns(Long totalReturns) 
    {
        this.totalReturns = totalReturns;
    }

    public Long getTotalReturns() 
    {
        return totalReturns;
    }
    public void setTotalLoans(Long totalLoans) 
    {
        this.totalLoans = totalLoans;
    }

    public Long getTotalLoans() 
    {
        return totalLoans;
    }
    public void setTotalLost(Long totalLost) 
    {
        this.totalLost = totalLost;
    }

    public Long getTotalLost() 
    {
        return totalLost;
    }
    public void setArchivedCount(Long archivedCount) 
    {
        this.archivedCount = archivedCount;
    }

    public Long getArchivedCount() 
    {
        return archivedCount;
    }
    public void setPendingCount(Long pendingCount) 
    {
        this.pendingCount = pendingCount;
    }

    public Long getPendingCount() 
    {
        return pendingCount;
    }
    public void setElectronicCount(Long electronicCount) 
    {
        this.electronicCount = electronicCount;
    }

    public Long getElectronicCount() 
    {
        return electronicCount;
    }
    public void setPhysicalCount(Long physicalCount) 
    {
        this.physicalCount = physicalCount;
    }

    public Long getPhysicalCount() 
    {
        return physicalCount;
    }

    public void setCreatedAt(Date createdAt) 
    {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() 
    {
        return createdAt;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("statDate", getStatDate())
            .append("totalBorrows", getTotalBorrows())
            .append("totalReturns", getTotalReturns())
            .append("totalLoans", getTotalLoans())
            .append("totalLost", getTotalLost())
            .append("archivedCount", getArchivedCount())
            .append("pendingCount", getPendingCount())
            .append("electronicCount", getElectronicCount())
            .append("physicalCount", getPhysicalCount())
            .append("activeUserList", getActiveUserList())
            .append("createdAt", getCreatedAt())
            .append("cabinetCount", getCabinetCount())
            .append("repositoryCount", getRepositoryCount())
            .append("seriesData", getSeriesData())
            .toString();
    }

    public String getActiveUserList() {
        return activeUserList;
    }

    public void setActiveUserList(String activeUserList) {
        this.activeUserList = activeUserList;
    }

    public Long getCabinetCount() {
        return cabinetCount;
    }

    public void setCabinetCount(Long cabinetCount) {
        this.cabinetCount = cabinetCount;
    }

    public Long getRepositoryCount() {
        return repositoryCount;
    }

    public void setRepositoryCount(Long repositoryCount) {
        this.repositoryCount = repositoryCount;
    }

    public String getSeriesData() {
        return seriesData;
    }

    public void setSeriesData(String seriesData) {
        this.seriesData = seriesData;
    }
}
