package com.iams.activiti8.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;

/**
 * 表单模板对象 tb_form_templates
 *
 * @author LiuTao
 * @date 2025-03-27
 */
public class FormTemplates extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 表单键 */
    @Excel(name = "表单键")
    private String formKey;

    /** 表单名 */
    @Excel(name = "表单名")
    private String formName;

    /** 表单组件路径 */
    @Excel(name = "表单组件路径")
    private String templatePath;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date createdTime;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }
    public void setFormKey(String formKey)
    {
        this.formKey = formKey;
    }

    public String getFormKey()
    {
        return formKey;
    }
    public void setFormName(String formName)
    {
        this.formName = formName;
    }

    public String getFormName()
    {
        return formName;
    }
    public void setTemplatePath(String templatePath)
    {
        this.templatePath = templatePath;
    }

    public String getTemplatePath()
    {
        return templatePath;
    }
    public void setCreatedTime(Date createdTime)
    {
        this.createdTime = createdTime;
    }

    public Date getCreatedTime()
    {
        return createdTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("formKey", getFormKey())
                .append("formName", getFormName())
                .append("templatePath", getTemplatePath())
                .append("createdTime", getCreatedTime())
                .toString();
    }
}
