package com.iams.activiti8.mapper;

import java.util.List;
import com.iams.activiti8.domain.FormTemplates;

/**
 * 表单模板Mapper接口
 *
 * @author LiuTao
 * @date 2025-03-27
 */
public interface FormTemplatesMapper
{
    /**
     * 查询表单模板
     *
     * @param id 表单模板主键
     * @return 表单模板
     */
    public FormTemplates selectFormTemplatesById(Long id);

    /**
     * 查询表单模板列表
     *
     * @param formTemplates 表单模板
     * @return 表单模板集合
     */
    public List<FormTemplates> selectFormTemplatesList(FormTemplates formTemplates);

    /**
     * 新增表单模板
     *
     * @param formTemplates 表单模板
     * @return 结果
     */
    public int insertFormTemplates(FormTemplates formTemplates);

    /**
     * 修改表单模板
     *
     * @param formTemplates 表单模板
     * @return 结果
     */
    public int updateFormTemplates(FormTemplates formTemplates);

    /**
     * 删除表单模板
     *
     * @param id 表单模板主键
     * @return 结果
     */
    public int deleteFormTemplatesById(Long id);

    /**
     * 批量删除表单模板
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteFormTemplatesByIds(Long[] ids);


    public FormTemplates selectFormTemplatesFormKey(String name);
}
