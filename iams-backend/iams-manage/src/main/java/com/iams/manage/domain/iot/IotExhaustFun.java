package com.iams.manage.domain.iot;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 排风扇设备信息对象 iot_exhaust_fun
 *
 * @author liuziqi
 * @date 2025-02-28
 */
public class IotExhaustFun extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 设备编号 */
    @Excel(name = "设备编号")
    private String deviceId;

    /** 生产厂商 */
    @Excel(name = "生产厂商")
    private String manufacturer;

    /** 抽风机型号 */
    @Excel(name = "抽风机型号")
    private String model;

    /** 安装位置 */
    @Excel(name = "安装位置")
    private String location;

    /** 安装日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "安装日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date installDate;

    /** 额定功率 */
    @Excel(name = "额定功率")
    private Long powerRating;

    /** 最大排风量 */
    @Excel(name = "最大排风量")
    private Long airflowCapacity;

    /** 防火等级 */
    @Excel(name = "防火等级")
    private String fireproofLevel;

    public void setDeviceId(String deviceId)
    {
        this.deviceId = deviceId;
    }

    public String getDeviceId()
    {
        return deviceId;
    }
    public void setManufacturer(String manufacturer)
    {
        this.manufacturer = manufacturer;
    }

    public String getManufacturer()
    {
        return manufacturer;
    }
    public void setModel(String model)
    {
        this.model = model;
    }

    public String getModel()
    {
        return model;
    }
    public void setLocation(String location)
    {
        this.location = location;
    }

    public String getLocation()
    {
        return location;
    }
    public void setInstallDate(Date installDate)
    {
        this.installDate = installDate;
    }

    public Date getInstallDate()
    {
        return installDate;
    }
    public void setPowerRating(Long powerRating)
    {
        this.powerRating = powerRating;
    }

    public Long getPowerRating()
    {
        return powerRating;
    }
    public void setAirflowCapacity(Long airflowCapacity)
    {
        this.airflowCapacity = airflowCapacity;
    }

    public Long getAirflowCapacity()
    {
        return airflowCapacity;
    }
    public void setFireproofLevel(String fireproofLevel)
    {
        this.fireproofLevel = fireproofLevel;
    }

    public String getFireproofLevel()
    {
        return fireproofLevel;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("deviceId", getDeviceId())
            .append("manufacturer", getManufacturer())
            .append("model", getModel())
            .append("location", getLocation())
            .append("installDate", getInstallDate())
            .append("powerRating", getPowerRating())
            .append("airflowCapacity", getAirflowCapacity())
            .append("fireproofLevel", getFireproofLevel())
            .toString();
    }
}
