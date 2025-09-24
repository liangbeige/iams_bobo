package com.iams.manage.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.TreeEntity;

/**
 * 树形目录对象 tb_directory
 *
 * @author zhjm
 * @date 2025-01-07
 */
public class TreeDirectory extends TreeEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @Excel(name = "主键")
    private Long id;

    /** 序号 */
    @Excel(name = "序号")
    private Long xuhao;

    /** 目录名称 */
    @Excel(name = "目录名称")
    private String name;

    /** 文件数量 */
    @Excel(name = "文件数量")
    private Long documentCount;

    /** 层级深度 */
    @Excel(name = "层级深度")
    private Long depth;

    /** 所属档案 */
    @Excel(name = "所属档案")
    private Long archiveId;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }
    public void setXuhao(Long xuhao)
    {
        this.xuhao = xuhao;
    }

    public Long getXuhao()
    {
        return xuhao;
    }
    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
    public void setDocumentCount(Long documentCount)
    {
        this.documentCount = documentCount;
    }

    public Long getDocumentCount()
    {
        return documentCount;
    }
    public void setDepth(Long depth)
    {
        this.depth = depth;
    }

    public Long getDepth()
    {
        return depth;
    }
    public void setArchiveId(Long archiveId)
    {
        this.archiveId = archiveId;
    }

    public Long getArchiveId()
    {
        return archiveId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("xuhao", getXuhao())
                .append("name", getName())
                .append("documentCount", getDocumentCount())
                .append("depth", getDepth())
                .append("parentId", getParentId())
                .append("archiveId", getArchiveId())
                .append("createTime", getCreateTime())
                .append("createBy", getCreateBy())
                .append("updateTime", getUpdateTime())
                .append("updateBy", getUpdateBy())
                .append("remark", getRemark())
                .toString();
    }
}
