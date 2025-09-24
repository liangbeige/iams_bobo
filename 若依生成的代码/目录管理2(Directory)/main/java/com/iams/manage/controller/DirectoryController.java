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
import com.iams.manage.domain.Directory;
import com.iams.manage.service.IDirectoryService;
import com.iams.common.utils.poi.ExcelUtil;
import com.iams.common.core.page.TableDataInfo;

/**
 * 目录管理Controller
 * 
 * @author zhjm
 * @date 2025-01-09
 */
@RestController
@RequestMapping("/manage/directory")
public class DirectoryController extends BaseController
{
    @Autowired
    private IDirectoryService directoryService;

    /**
     * 查询目录管理列表
     */
    @PreAuthorize("@ss.hasPermi('manage:directory:list')")
    @GetMapping("/list")
    public TableDataInfo list(Directory directory)
    {
        startPage();
        List<Directory> list = directoryService.selectDirectoryList(directory);
        return getDataTable(list);
    }

    /**
     * 导出目录管理列表
     */
    @PreAuthorize("@ss.hasPermi('manage:directory:export')")
    @Log(title = "目录管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Directory directory)
    {
        List<Directory> list = directoryService.selectDirectoryList(directory);
        ExcelUtil<Directory> util = new ExcelUtil<Directory>(Directory.class);
        util.exportExcel(response, list, "目录管理数据");
    }

    /**
     * 获取目录管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('manage:directory:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(directoryService.selectDirectoryById(id));
    }

    /**
     * 新增目录管理
     */
    @PreAuthorize("@ss.hasPermi('manage:directory:add')")
    @Log(title = "目录管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Directory directory)
    {
        return toAjax(directoryService.insertDirectory(directory));
    }

    /**
     * 修改目录管理
     */
    @PreAuthorize("@ss.hasPermi('manage:directory:edit')")
    @Log(title = "目录管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Directory directory)
    {
        return toAjax(directoryService.updateDirectory(directory));
    }

    /**
     * 删除目录管理
     */
    @PreAuthorize("@ss.hasPermi('manage:directory:remove')")
    @Log(title = "目录管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(directoryService.deleteDirectoryByIds(ids));
    }
}
