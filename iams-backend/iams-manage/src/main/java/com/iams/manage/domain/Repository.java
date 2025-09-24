package com.iams.manage.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;

/**
 * 库房管理对象 tb_repository
 * 
 * @author zhjm
 * @date 2025-01-06
 */
public class Repository extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 编号 */
    @Excel(name = "编号")
    private String bianhao;

    /** 名称 */
    @Excel(name = "名称")
    private String name;

    /** 档案柜数量 */
    @Excel(name = "档案柜数量")
    private Long cabinetCount;

    /** 档案柜排数 */
    @Excel(name = "档案柜排数")
    private Long rowCount;

    /** 档案柜列数 */
    @Excel(name = "档案柜列数")
    private Long columnCount;

    /** 已有档案数 */
    @Excel(name = "已有档案数")
    private Long archiveCount;

    /** 温度 */
    @Excel(name = "温度")
    private Long temperature;

    /** 湿度 */
    @Excel(name = "湿度")
    private Long moisture;

    /** 颗粒物 */
    private Long particles;

    /** 氧气浓度 */
    private Long oxygen;

    /** 水浸 */
    private Long waterlogging;

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
    public void setCabinetCount(Long cabinetCount) 
    {
        this.cabinetCount = cabinetCount;
    }

    public Long getCabinetCount() 
    {
        return cabinetCount;
    }
    public void setRowCount(Long rowCount) 
    {
        this.rowCount = rowCount;
    }

    public Long getRowCount() 
    {
        return rowCount;
    }
    public void setColumnCount(Long columnCount) 
    {
        this.columnCount = columnCount;
    }

    public Long getColumnCount() 
    {
        return columnCount;
    }
    public void setArchiveCount(Long archiveCount) 
    {
        this.archiveCount = archiveCount;
    }

    public Long getArchiveCount() 
    {
        return archiveCount;
    }
    public void setTemperature(Long temperature) 
    {
        this.temperature = temperature;
    }

    public Long getTemperature() 
    {
        return temperature;
    }
    public void setMoisture(Long moisture) 
    {
        this.moisture = moisture;
    }

    public Long getMoisture() 
    {
        return moisture;
    }
    public void setParticles(Long particles) 
    {
        this.particles = particles;
    }

    public Long getParticles() 
    {
        return particles;
    }
    public void setOxygen(Long oxygen) 
    {
        this.oxygen = oxygen;
    }

    public Long getOxygen() 
    {
        return oxygen;
    }
    public void setWaterlogging(Long waterlogging) 
    {
        this.waterlogging = waterlogging;
    }

    public Long getWaterlogging() 
    {
        return waterlogging;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("bianhao", getBianhao())
            .append("name", getName())
            .append("cabinetCount", getCabinetCount())
            .append("rowCount", getRowCount())
            .append("columnCount", getColumnCount())
            .append("archiveCount", getArchiveCount())
            .append("temperature", getTemperature())
            .append("moisture", getMoisture())
            .append("particles", getParticles())
            .append("oxygen", getOxygen())
            .append("waterlogging", getWaterlogging())
            .append("createTime", getCreateTime())
            .append("createBy", getCreateBy())
            .append("updateTime", getUpdateTime())
            .append("updateBy", getUpdateBy())
            .append("remark", getRemark())
            .toString();
    }
}
