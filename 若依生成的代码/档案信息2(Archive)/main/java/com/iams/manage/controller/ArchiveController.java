package com.iams.manage.controller;

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
import com.iams.manage.domain.Archive;
import com.iams.manage.service.IArchiveService;
import com.iams.common.utils.poi.ExcelUtil;
import com.iams.common.core.page.TableDataInfo;

/**
 * 档案列表Controller
 * 
 * @author zhjm
 * @date 2025-01-10
 */
@RestController
@RequestMapping("/manage/archive")
public class ArchiveController extends BaseController
{
    @Autowired
    private IArchiveService archiveService;

    /**
     * 查询档案列表列表
     */
    @PreAuthorize("@ss.hasPermi('manage:archive:list')")
    @GetMapping("/list")
    public TableDataInfo list(Archive archive)
    {
        startPage();
        List<Archive> list = archiveService.selectArchiveList(archive);
        return getDataTable(list);
    }

    /**
     * 导出档案列表列表
     */
    @PreAuthorize("@ss.hasPermi('manage:archive:export')")
    @Log(title = "档案列表", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Archive archive)
    {
        List<Archive> list = archiveService.selectArchiveList(archive);
        ExcelUtil<Archive> util = new ExcelUtil<Archive>(Archive.class);
        util.exportExcel(response, list, "档案列表数据");
    }

    /**
     * 获取档案列表详细信息
     */
    @PreAuthorize("@ss.hasPermi('manage:archive:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(archiveService.selectArchiveById(id));
    }

    /**
     * 新增档案列表
     */
    @PreAuthorize("@ss.hasPermi('manage:archive:add')")
    @Log(title = "档案列表", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Archive archive)
    {
        return toAjax(archiveService.insertArchive(archive));
    }

    /**
     * 修改档案列表
     */
    @PreAuthorize("@ss.hasPermi('manage:archive:edit')")
    @Log(title = "档案列表", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Archive archive)
    {
        return toAjax(archiveService.updateArchive(archive));
    }

    /**
     * 删除档案列表
     */
    @PreAuthorize("@ss.hasPermi('manage:archive:remove')")
    @Log(title = "档案列表", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(archiveService.deleteArchiveByIds(ids));
    }
}
