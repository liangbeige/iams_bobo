package com.iams.manage.controller;

import java.util.List;

import com.iams.common.utils.SecurityUtils;
import com.iams.manage.domain.workflow.dto.FilingRecordDTO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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

import com.iams.manage.domain.workflow.FilingRecord;
import com.iams.manage.service.IFilingRecordService;

import com.iams.common.utils.poi.ExcelUtil;
import com.iams.common.core.page.TableDataInfo;

/**
 * 归档申请Controller
 * 
 * @author LiuTao
 * @date 2025-03-26
 */
@RestController
@RequestMapping("/manage/archiveFilingApproval")
public class ArchiveFilingApprovalController extends BaseController
{
    @Autowired
    private IFilingRecordService filingRecordService;

    /**
     * 查询归档申请列表
     */
    @PreAuthorize("@ss.hasPermi('manage:archiveFilingApproval:list')")
    @GetMapping("/list")
    public TableDataInfo list(FilingRecord filingRecord)
    {
        startPage();
        List<FilingRecordDTO> list = filingRecordService.selectFilingRecordList(filingRecord);
        return getDataTable(list);
    }

    /**
     * 导出归档申请列表
     */
    @PreAuthorize("@ss.hasPermi('manage:archiveFilingApproval:export')")
    @Log(title = "归档申请", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, FilingRecord filingRecord)
    {
        List<FilingRecordDTO> list = filingRecordService.selectFilingRecordList(filingRecord);
        ExcelUtil<FilingRecordDTO> util = new ExcelUtil<FilingRecordDTO>(FilingRecordDTO.class);
        util.exportExcel(response, list, "归档申请数据");
    }

    /**
     * 获取归档申请详细信息
     */
    @PreAuthorize("@ss.hasPermi('manage:archiveFilingApproval:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(filingRecordService.selectFilingRecordById(id));
    }

    /**
     * 新增归档申请
     */
    @PreAuthorize("@ss.hasPermi('manage:archiveFilingApproval:add')")
    @Log(title = "归档申请", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody @Valid FilingRecordDTO filingRecordDTO)
    {
        return toAjax(filingRecordService.insertFilingRecord(filingRecordDTO));
    }

    /**
     * 修改归档申请
     */
    @PreAuthorize("@ss.hasPermi('manage:archiveFilingApproval:edit')")
    @Log(title = "归档申请", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FilingRecordDTO filingRecordDTO)
    {
        return toAjax(filingRecordService.updateFilingRecord(filingRecordDTO));
    }

    /**
     * 删除归档申请
     */
    @PreAuthorize("@ss.hasPermi('manage:archiveFilingApproval:remove')")
    @Log(title = "归档申请", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(filingRecordService.deleteFilingRecordByIds(ids));
    }
}
