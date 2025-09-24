package com.iams.activiti8.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.iams.activiti8.mapper.FormTemplatesMapper;
import com.iams.activiti8.domain.FormTemplates;
import com.iams.activiti8.service.IFormTemplatesService;

/**
 * 表单模板Service业务层处理
 * 
 * @author liutao
 * @date 2025-03-13
 */
@Service
public class FormTemplatesServiceImpl implements IFormTemplatesService 
{
    @Autowired
    private FormTemplatesMapper formTemplatesMapper;

    /**
     * 查询表单模板
     * 
     * @param id 表单模板主键
     * @return 表单模板
     */
    @Override
    public FormTemplates selectFormTemplatesById(Long id)
    {
        return formTemplatesMapper.selectFormTemplatesById(id);
    }

    /**
     * 查询表单模板列表
     * 
     * @param formTemplates 表单模板
     * @return 表单模板
     */
    @Override
    public List<FormTemplates> selectFormTemplatesList(FormTemplates formTemplates)
    {
        return formTemplatesMapper.selectFormTemplatesList(formTemplates);
    }

    /**
     * 新增表单模板
     * 
     * @param formTemplates 表单模板
     * @return 结果
     */
    @Override
    public int insertFormTemplates(FormTemplates formTemplates)
    {
        return formTemplatesMapper.insertFormTemplates(formTemplates);
    }

    /**
     * 修改表单模板
     * 
     * @param formTemplates 表单模板
     * @return 结果
     */
    @Override
    public int updateFormTemplates(FormTemplates formTemplates)
    {
        return formTemplatesMapper.updateFormTemplates(formTemplates);
    }

    /**
     * 批量删除表单模板
     * 
     * @param ids 需要删除的表单模板主键
     * @return 结果
     */
    @Override
    public int deleteFormTemplatesByIds(Long[] ids)
    {
        return formTemplatesMapper.deleteFormTemplatesByIds(ids);
    }

    /**
     * 删除表单模板信息
     * 
     * @param id 表单模板主键
     * @return 结果
     */
    @Override
    public int deleteFormTemplatesById(Long id)
    {
        return formTemplatesMapper.deleteFormTemplatesById(id);
    }


    @Override
    public FormTemplates selectFormTemplatesFormKey(String name)
    {
        return formTemplatesMapper.selectFormTemplatesFormKey(name);
    }
}
