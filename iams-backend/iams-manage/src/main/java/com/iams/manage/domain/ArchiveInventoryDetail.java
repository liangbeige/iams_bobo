package com.iams.manage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;

/**
 * 档案盘点明细表对象 tb_archive_inventory_detail
 *
 * @author ruoyi
 * @date 2025-06-23
 */
public class ArchiveInventoryDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 盘点主表ID */
    @Excel(name = "盘点主表ID")
    private Long inventoryId;

    /** 档案ID */
    @Excel(name = "档案ID")
    private Long archiveId;

    /** RFID标签号 */
    @Excel(name = "RFID标签号")
    private String rfid;

    /** 档号 */
    @Excel(name = "档号")
    private String danghao;

    /** 扫描时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "扫描时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date scanTime;

    /** 天线编号 */
    @Excel(name = "天线编号")
    private String antenna;

    /** 信号强度 */
    @Excel(name = "信号强度")
    private String rssi;

    /** 实体位置 */
    @Excel(name = "实体位置")
    private String shitiLocation;

    /** 详细位置 */
    @Excel(name = "详细位置")
    private String exactLocation;

    /** 是否手动盘点(0-自动,1-手动) */
    @Excel(name = "是否手动盘点", readConverterExp = "0=自动,1=手动")
    private Integer manual;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setInventoryId(Long inventoryId)
    {
        this.inventoryId = inventoryId;
    }

    public Long getInventoryId()
    {
        return inventoryId;
    }

    public void setArchiveId(Long archiveId)
    {
        this.archiveId = archiveId;
    }

    public Long getArchiveId()
    {
        return archiveId;
    }

    public void setRfid(String rfid)
    {
        this.rfid = rfid;
    }

    public String getRfid()
    {
        return rfid;
    }

    public void setDanghao(String danghao)
    {
        this.danghao = danghao;
    }

    public String getDanghao()
    {
        return danghao;
    }

    public void setScanTime(Date scanTime)
    {
        this.scanTime = scanTime;
    }

    public Date getScanTime()
    {
        return scanTime;
    }

    public void setAntenna(String antenna)
    {
        this.antenna = antenna;
    }

    public String getAntenna()
    {
        return antenna;
    }

    public void setRssi(String rssi)
    {
        this.rssi = rssi;
    }

    public String getRssi()
    {
        return rssi;
    }

    public void setShitiLocation(String shitiLocation)
    {
        this.shitiLocation = shitiLocation;
    }

    public String getShitiLocation()
    {
        return shitiLocation;
    }

    public void setExactLocation(String exactLocation)
    {
        this.exactLocation = exactLocation;
    }

    public String getExactLocation()
    {
        return exactLocation;
    }

    public void setManual(Integer manual)
    {
        this.manual = manual;
    }

    public Integer getManual()
    {
        return manual;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("inventoryId", getInventoryId())
                .append("archiveId", getArchiveId())
                .append("rfid", getRfid())
                .append("danghao", getDanghao())
                .append("scanTime", getScanTime())
                .append("antenna", getAntenna())
                .append("rssi", getRssi())
                .append("shitiLocation", getShitiLocation())
                .append("exactLocation", getExactLocation())
                .append("manual", getManual())
                .toString();
    }
}