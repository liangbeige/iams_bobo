package com.iams.manage.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;

/**
 * 组卷管理对象 tb_volume
 * 
 * @author zhld
 * @date 2025-06-06
 */
public class Volume extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 卷宗名称 */
    @Excel(name = "卷宗名称")
    private String name;

    /** 卷宗编号 */
    @Excel(name = "卷宗编号")
    private String volumeNumber;

    /** 档案总数 */
    @Excel(name = "档案总数")
    private Long totalArchives;

    /** 现存档案数量 */
    @Excel(name = "现存档案数量")
    private Long currentArchives;

    /** 卷宗描述 */
    private String description;

    /** 状态: ACTIVE/ARCHIVED/SEALED */
    @Excel(name = "状态: ACTIVE/ARCHIVED/SEALED")
    private String status;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setName(String name) 
    {
        this.name = name;
    }

    public String getName() 
    {
        return name;
    }
    public void setVolumeNumber(String volumeNumber) 
    {
        this.volumeNumber = volumeNumber;
    }

    public String getVolumeNumber() 
    {
        return volumeNumber;
    }
    public void setTotalArchives(Long totalArchives) 
    {
        this.totalArchives = totalArchives;
    }

    public Long getTotalArchives() 
    {
        return totalArchives;
    }
    public void setCurrentArchives(Long currentArchives) 
    {
        this.currentArchives = currentArchives;
    }

    public Long getCurrentArchives() 
    {
        return currentArchives;
    }
    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }
    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("name", getName())
            .append("volumeNumber", getVolumeNumber())
            .append("totalArchives", getTotalArchives())
            .append("currentArchives", getCurrentArchives())
            .append("description", getDescription())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .append("createBy", getCreateBy())
            .append("updateTime", getUpdateTime())
            .append("updateBy", getUpdateBy())
            .toString();
    }
}
