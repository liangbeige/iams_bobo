package com.iams.manage.domain.iot;

import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 温度感知对象 iot_temperature_threshold
 *
 * @author pangzhongzheng
 * @date 2025-03-12
 */
public class IotTemperatureThreshold extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long id;

    /** 仓库名称 */
    @Excel(name = "仓库名称")
    private String warehouseName;

    /** 仓库位置 */
    @Excel(name = "仓库位置")
    private String location;

    /** 阈值_min */
    @Excel(name = "阈值_min")
    private Long thresholdMin;

    /** 阈值_max */
    @Excel(name = "阈值_max")
    private Long thresholdMax;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
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
    public void setThresholdMin(Long thresholdMin)
    {
        this.thresholdMin = thresholdMin;
    }

    public Long getThresholdMin()
    {
        return thresholdMin;
    }
    public void setThresholdMax(Long thresholdMax)
    {
        this.thresholdMax = thresholdMax;
    }

    public Long getThresholdMax()
    {
        return thresholdMax;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("warehouseName", getWarehouseName())
            .append("location", getLocation())
            .append("thresholdMin", getThresholdMin())
            .append("thresholdMax", getThresholdMax())
            .toString();
    }
}
