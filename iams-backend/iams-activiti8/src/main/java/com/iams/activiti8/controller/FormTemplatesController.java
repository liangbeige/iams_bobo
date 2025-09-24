package com.iams.activiti8.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.iams.common.annotation.Log;
import com.iams.common.core.controller.BaseController;
import com.iams.common.core.domain.AjaxResult;
import com.iams.common.enums.BusinessType;
import com.iams.activiti8.domain.FormTemplates;
import com.iams.activiti8.service.IFormTemplatesService;
import com.iams.common.utils.poi.ExcelUtil;
import com.iams.common.core.page.TableDataInfo;

/**
 * 表单模板Controller
 * 
 * @author liutao
 * @date 2025-03-13
 */
@RestController
@RequestMapping("/activiti8/FormTemplates")
public class FormTemplatesController extends BaseController
{
    @Autowired
    private IFormTemplatesService formTemplatesService;

    /**
     * 查询表单模板列表
     */
    @PreAuthorize("@ss.hasPermi('activiti8:FormTemplates:list')")
    @GetMapping("/list")
    public TableDataInfo list(FormTemplates formTemplates)
    {
        startPage();
        List<FormTemplates> list = formTemplatesService.selectFormTemplatesList(formTemplates);
        return getDataTable(list);
    }

    /**
     * 导出表单模板列表
     */
    @PreAuthorize("@ss.hasPermi('activiti8:FormTemplates:export')")
    @Log(title = "表单模板", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, FormTemplates formTemplates)
    {
        List<FormTemplates> list = formTemplatesService.selectFormTemplatesList(formTemplates);
        ExcelUtil<FormTemplates> util = new ExcelUtil<FormTemplates>(FormTemplates.class);
        util.exportExcel(response, list, "表单模板数据");
    }

    /**
     * 获取表单模板详细信息
     */
    @PreAuthorize("@ss.hasPermi('activiti8:FormTemplates:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(formTemplatesService.selectFormTemplatesById(id));
    }

    /**
     * 新增表单模板
     */
    @PreAuthorize("@ss.hasPermi('activiti8:FormTemplates:add')")
    @Log(title = "表单模板", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody FormTemplates formTemplates)
    {
        return toAjax(formTemplatesService.insertFormTemplates(formTemplates));
    }

    /**
     * 修改表单模板
     */
    @PreAuthorize("@ss.hasPermi('activiti8:FormTemplates:edit')")
    @Log(title = "表单模板", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FormTemplates formTemplates)
    {
        return toAjax(formTemplatesService.updateFormTemplates(formTemplates));
    }

    /**
     * 删除表单模板
     */
    @PreAuthorize("@ss.hasPermi('activiti8:FormTemplates:remove')")
    @Log(title = "表单模板", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(formTemplatesService.deleteFormTemplatesByIds(ids));
    }
}
