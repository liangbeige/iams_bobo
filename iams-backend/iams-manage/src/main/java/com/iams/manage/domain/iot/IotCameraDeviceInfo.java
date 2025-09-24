package com.iams.manage.domain.iot;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等对象 iot_camera_device_info
 *
 * @author likang
 * @date 2025-02-25
 */
public class IotCameraDeviceInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 摄像头编号 */
    private Integer deviceId;

    /** 生产厂家 */
    @Excel(name = "生产厂家")
    private String manufacturer;

    /** 运行状态 */
    @Excel(name = "运行状态")
    private Integer isNormal;

    /** 安装地点 */
    @Excel(name = "安装地点")
    private String installationLocation;

    /** 具体型号 */
    @Excel(name = "具体型号")
    private String deviceModel;

    /** 安装时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "安装时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date installationDate;

    /** 上次维护时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "上次维护时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date lastMaintenanceDate;

    /**  IP 地址 */
    @Excel(name = " IP 地址")
    private String ipAddress;

    /** 端口 */
    @Excel(name = "端口")
    private Long portNumber;

    /** 密码 */
    @Excel(name = "密码")
    private String password;

    /** 备注说明 */
    @Excel(name = "备注说明")
    private String remarks;

    public void setDeviceId(Integer deviceId)
    {
        this.deviceId = deviceId;
    }

    public Integer getDeviceId()
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
    public void setIsNormal(Integer isNormal)
    {
        this.isNormal = isNormal;
    }

    public Integer getIsNormal()
    {
        return isNormal;
    }
    public void setInstallationLocation(String installationLocation)
    {
        this.installationLocation = installationLocation;
    }

    public String getInstallationLocation()
    {
        return installationLocation;
    }
    public void setDeviceModel(String deviceModel)
    {
        this.deviceModel = deviceModel;
    }

    public String getDeviceModel()
    {
        return deviceModel;
    }
    public void setInstallationDate(Date installationDate)
    {
        this.installationDate = installationDate;
    }

    public Date getInstallationDate()
    {
        return installationDate;
    }
    public void setLastMaintenanceDate(Date lastMaintenanceDate)
    {
        this.lastMaintenanceDate = lastMaintenanceDate;
    }

    public Date getLastMaintenanceDate()
    {
        return lastMaintenanceDate;
    }
    public void setIpAddress(String ipAddress)
    {
        this.ipAddress = ipAddress;
    }

    public String getIpAddress()
    {
        return ipAddress;
    }
    public void setPortNumber(Long portNumber)
    {
        this.portNumber = portNumber;
    }

    public Long getPortNumber()
    {
        return portNumber;
    }
    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getPassword()
    {
        return password;
    }
    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }

    public String getRemarks()
    {
        return remarks;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("deviceId", getDeviceId())
            .append("manufacturer", getManufacturer())
            .append("isNormal", getIsNormal())
            .append("installationLocation", getInstallationLocation())
            .append("deviceModel", getDeviceModel())
            .append("installationDate", getInstallationDate())
            .append("lastMaintenanceDate", getLastMaintenanceDate())
            .append("ipAddress", getIpAddress())
            .append("portNumber", getPortNumber())
            .append("password", getPassword())
            .append("remarks", getRemarks())
            .toString();
    }
}
