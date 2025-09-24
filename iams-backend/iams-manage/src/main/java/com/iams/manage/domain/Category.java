package com.iams.manage.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;

/**
 * 档案门类管理对象 tb_category
 * 
 * @author zhjm
 * @date 2025-01-05
 */
public class Category extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 门类ID */
    @Excel(name = "门类ID")
    private Long id;

    /** 门类名称 */
    @Excel(name = "门类名称")
    private String name;

    /** 门类简介 */
    @Excel(name = "门类简介")
    private String description;

    /** 档案数量 */
    @Excel(name = "档案数量")
    private Integer archiveCount;

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
    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }
    public void setArchiveCount(Integer archiveCount) 
    {
        this.archiveCount = archiveCount;
    }

    public Integer getArchiveCount() 
    {
        return archiveCount;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("name", getName())
            .append("description", getDescription())
            .append("archiveCount", getArchiveCount())
            .append("createTime", getCreateTime())
            .append("createBy", getCreateBy())
            .append("updateTime", getUpdateTime())
            .append("updateBy", getUpdateBy())
            .append("remark", getRemark())
            .toString();
    }
}
