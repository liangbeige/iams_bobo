package com.iams.manage.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;
import java.util.ArrayList;
import java.util.List;

/**
 * 档案分类对象 archive_category
 *
 * @author ruoyi
 */
@Setter
@Getter
public class ArchiveCategory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 分类ID */
    private Long id;

    /** 分类编码 */
    @Excel(name = "分类编码")
    @NotBlank(message = "分类编码不能为空")
    @Size(min = 0, max = 50, message = "分类编码长度不能超过50个字符")
    private String code;

    /** 分类名称 */
    @Excel(name = "分类名称")
    @NotBlank(message = "分类名称不能为空")
    @Size(min = 0, max = 100, message = "分类名称长度不能超过100个字符")
    private String name;

    /** 父分类ID */
    private Long parentId;

    /** 祖级列表 */
    private String ancestors;

    /** 显示顺序 */
    @Excel(name = "显示顺序")
    private Integer sortOrder;

    /** 根节点ID */
    private Long rootId;

    /** 子分类 */
    private List<ArchiveCategory> children = new ArrayList<ArchiveCategory>();

    /** 子分类数量（非数据库字段） */
    private Integer childCount;

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("code", getCode())
                .append("name", getName())
                .append("parentId", getParentId())
                .append("ancestors", getAncestors())
                .append("sortOrder", getSortOrder())
                .append("rootId", getRootId())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}