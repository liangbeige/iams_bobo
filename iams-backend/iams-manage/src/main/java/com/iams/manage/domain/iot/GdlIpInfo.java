package com.iams.manage.domain.iot;

import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 固定列IP信息管理对象 gdl_ip_info
 *
 * @author likang
 * @date 2025-04-18
 */
public class GdlIpInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 列号（主键） */
    private Integer gdlNo;

    /** 名称 */
    @Excel(name = "名称")
    private String gdlName;

    /** IP地址 */
    @Excel(name = "IP地址")
    private String gdlIp;

    /** 端口号 */
    @Excel(name = "端口号")
    private Long gdlPort;

    /** 备注信息 */
    @Excel(name = "备注信息")
    private String gdlRemark;

    public void setGdlNo(Integer gdlNo)
    {
        this.gdlNo = gdlNo;
    }

    public Integer getGdlNo()
    {
        return gdlNo;
    }
    public void setGdlName(String gdlName)
    {
        this.gdlName = gdlName;
    }

    public String getGdlName()
    {
        return gdlName;
    }
    public void setGdlIp(String gdlIp)
    {
        this.gdlIp = gdlIp;
    }

    public String getGdlIp()
    {
        return gdlIp;
    }
    public void setGdlPort(Long gdlPort)
    {
        this.gdlPort = gdlPort;
    }

    public Long getGdlPort()
    {
        return gdlPort;
    }
    public void setGdlRemark(String gdlRemark)
    {
        this.gdlRemark = gdlRemark;
    }

    public String getGdlRemark()
    {
        return gdlRemark;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("gdlNo", getGdlNo())
            .append("gdlName", getGdlName())
            .append("gdlIp", getGdlIp())
            .append("gdlPort", getGdlPort())
            .append("gdlRemark", getGdlRemark())
            .toString();
    }
}
