package com.iams.manage.domain.iot;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 空调设备信息，存储空调的静态属性及实时状态对象 iot_air_conditioner
 *
 * @author liuziqi
 * @date 2025-02-26
 */
public class IotAirConditioner extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 序列号 */
    @Excel(name = "序列号")
    private String id;

    /** 品牌名称 */
    @Excel(name = "品牌名称")
    private String brand;

    /** 空调型号 */
    @Excel(name = "空调型号")
    private String model;

    /** 能效等级 */
    @Excel(name = "能效等级")
    private String energyEfficiency;

    /** 功率 */
    @Excel(name = "功率")
    private Long power;

    /** 生产日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "生产日期（yyyy-MM-dd）", width = 30, dateFormat = "yyyy-MM-dd")
    private Date productionDate;

    /** 保修期 */
    @Excel(name = "保修期")
    private Long warrantyPeriod;

    /** 价格 */
    @Excel(name = "价格")
    private BigDecimal price;

    /** 滤网更换周期 */
    @Excel(name = "滤网更换周期")
    private Long filterReplacementCycle;

    public void setId(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }
    public void setBrand(String brand)
    {
        this.brand = brand;
    }

    public String getBrand()
    {
        return brand;
    }
    public void setModel(String model)
    {
        this.model = model;
    }

    public String getModel()
    {
        return model;
    }
    public void setEnergyEfficiency(String energyEfficiency)
    {
        this.energyEfficiency = energyEfficiency;
    }

    public String getEnergyEfficiency()
    {
        return energyEfficiency;
    }
    public void setPower(Long power)
    {
        this.power = power;
    }

    public Long getPower()
    {
        return power;
    }
    public void setProductionDate(Date productionDate)
    {
        this.productionDate = productionDate;
    }

    public Date getProductionDate()
    {
        return productionDate;
    }
    public void setWarrantyPeriod(Long warrantyPeriod)
    {
        this.warrantyPeriod = warrantyPeriod;
    }

    public Long getWarrantyPeriod()
    {
        return warrantyPeriod;
    }
    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    public BigDecimal getPrice()
    {
        return price;
    }
    public void setFilterReplacementCycle(Long filterReplacementCycle)
    {
        this.filterReplacementCycle = filterReplacementCycle;
    }

    public Long getFilterReplacementCycle()
    {
        return filterReplacementCycle;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("brand", getBrand())
            .append("model", getModel())
            .append("energyEfficiency", getEnergyEfficiency())
            .append("power", getPower())
            .append("productionDate", getProductionDate())
            .append("warrantyPeriod", getWarrantyPeriod())
            .append("price", getPrice())
            .append("filterReplacementCycle", getFilterReplacementCycle())
            .toString();
    }
}
