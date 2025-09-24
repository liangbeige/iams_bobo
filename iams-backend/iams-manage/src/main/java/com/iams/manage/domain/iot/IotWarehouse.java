package com.iams.manage.domain.iot;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 仓库管理对象 iot_warehouse
 *
 * @author pangzhongzheng
 * @date 2025-02-27
 */
public class IotWarehouse extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 仓库编号 */
    private Long warehouseId;

    /** 仓库名称 */
    @Excel(name = "仓库名称")
    private String warehouseName;

    /** 仓库区域位置 */
    @Excel(name = "仓库区域位置")
    private String location;

    /** 仓库容量 */
    @Excel(name = "仓库容量")
    private Long capacity;

    /** 仓库温度 */
    @Excel(name = "仓库温度")
    private BigDecimal temperature;

    /** 仓库湿度 */
    @Excel(name = "仓库湿度")
    private BigDecimal humidity;

    /** 氧气含量 */
    @Excel(name = "氧气含量")
    private BigDecimal oxygenLevel;

    /** 氮气含量 */
    @Excel(name = "氮气含量")
    private BigDecimal nitrogenLevel;

    /** 是否火灾 */
    @Excel(name = "是否火灾")
    private Integer isFire;

    /** 最后更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最后更新时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date lastUpdated;

    public void setWarehouseId(Long warehouseId)
    {
        this.warehouseId = warehouseId;
    }

    public Long getWarehouseId()
    {
        return warehouseId;
    }
    public void setWarehouseName(String warehouseName)
    {
        this.warehouseName = warehouseName;
    }

    public String getWarehouseName()
    {
        return warehouseName;
    }
    public void setLocation(String location)
    {
        this.location = location;
    }

    public String getLocation()
    {
        return location;
    }
    public void setCapacity(Long capacity)
    {
        this.capacity = capacity;
    }

    public Long getCapacity()
    {
        return capacity;
    }
    public void setTemperature(BigDecimal temperature)
    {
        this.temperature = temperature;
    }

    public BigDecimal getTemperature()
    {
        return temperature;
    }
    public void setHumidity(BigDecimal humidity)
    {
        this.humidity = humidity;
    }

    public BigDecimal getHumidity()
    {
        return humidity;
    }
    public void setOxygenLevel(BigDecimal oxygenLevel)
    {
        this.oxygenLevel = oxygenLevel;
    }

    public BigDecimal getOxygenLevel()
    {
        return oxygenLevel;
    }
    public void setNitrogenLevel(BigDecimal nitrogenLevel)
    {
        this.nitrogenLevel = nitrogenLevel;
    }

    public BigDecimal getNitrogenLevel()
    {
        return nitrogenLevel;
    }
    public void setIsFire(Integer isFire)
    {
        this.isFire = isFire;
    }

    public Integer getIsFire()
    {
        return isFire;
    }
    public void setLastUpdated(Date lastUpdated)
    {
        this.lastUpdated = lastUpdated;
    }

    public Date getLastUpdated()
    {
        return lastUpdated;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("warehouseId", getWarehouseId())
            .append("warehouseName", getWarehouseName())
            .append("location", getLocation())
            .append("capacity", getCapacity())
            .append("temperature", getTemperature())
            .append("humidity", getHumidity())
            .append("oxygenLevel", getOxygenLevel())
            .append("nitrogenLevel", getNitrogenLevel())
            .append("isFire", getIsFire())
            .append("lastUpdated", getLastUpdated())
            .toString();
    }
}
