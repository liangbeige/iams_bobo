package com.iams.manage.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;

/**
 * 档案柜管理对象 tb_cabinet
 * 
 * @author zhjm
 * @date 2025-01-06
 */
public class Cabinet extends BaseEntity
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

    /** 所在库房 */
    @Excel(name = "所在库房")
    private Long repositoryId;

    /** 排号 */
    @Excel(name = "区号")
    private Long quNo;

    /** 列号 */
    @Excel(name = "列号")
    private Long colNo;

    /** 节数 */
    @Excel(name = "节数")
    private Long leNo;

    /** 层数 */
    @Excel(name = "层数")
    private Long divNo;

    /** 左右 */
    @Excel(name = "左右")
    private String zyNo;

    /** 容量 */
    @Excel(name = "容量")
    private Long capacity;

    /** 已有档案数 */
    @Excel(name = "已有档案数")
    private Long size;

    /** 温度 */
    private Long temperature;

    /** 湿度 */
    private Long moisture;

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
    public void setRepositoryId(Long repositoryId) 
    {
        this.repositoryId = repositoryId;
    }

    public Long getRepositoryId() 
    {
        return repositoryId;
    }

    public void setCapacity(Long capacity) 
    {
        this.capacity = capacity;
    }

    public Long getCapacity() 
    {
        return capacity;
    }
    public void setSize(Long size) 
    {
        this.size = size;
    }

    public Long getSize() 
    {
        return size;
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
            .append("repositoryId", getRepositoryId())
            .append("quNo", getQuNo())
            .append("colNo", getColNo())
            .append("leNo", getLeNo())
            .append("divNo", getDivNo())
            .append("zyNo", getZyNo())
            .append("capacity", getCapacity())
            .append("size", getSize())
            .append("temperature", getTemperature())
            .append("moisture", getMoisture())
            .append("oxygen", getOxygen())
            .append("waterlogging", getWaterlogging())
            .append("createTime", getCreateTime())
            .append("createBy", getCreateBy())
            .append("updateTime", getUpdateTime())
            .append("updateBy", getUpdateBy())
            .append("remark", getRemark())
            .toString();
    }

    public Long getQuNo() {
        return quNo;
    }

    public void setQuNo(Long quNo) {
        this.quNo = quNo;
    }

    public Long getLeNo() {
        return leNo;
    }

    public void setLeNo(Long leNo) {
        this.leNo = leNo;
    }

    public Long getDivNo() {
        return divNo;
    }

    public void setDivNo(Long divNo) {
        this.divNo = divNo;
    }

    public String getZyNo() {
        return zyNo;
    }

    public void setZyNo(String zyNo) {
        this.zyNo = zyNo;
    }

    public Long getColNo() {
        return colNo;
    }

    public void setColNo(Long colNo) {
        this.colNo = colNo;
    }
}
