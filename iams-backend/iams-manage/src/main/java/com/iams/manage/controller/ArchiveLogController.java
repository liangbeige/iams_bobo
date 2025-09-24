package com.iams.manage.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.iams.common.annotation.Log;
import com.iams.common.core.controller.BaseController;
import com.iams.common.core.domain.AjaxResult;
import com.iams.common.enums.BusinessType;
import com.iams.manage.domain.ArchiveLog;
import com.iams.manage.service.IArchiveLogService;
import com.iams.common.utils.poi.ExcelUtil;
import com.iams.common.core.page.TableDataInfo;

/**
 * 档案日志Controller
 * 
 * @author LiuTao
 * @date 2025-04-04
 */
@RestController
@RequestMapping("/manage/ArchiveLog")
public class ArchiveLogController extends BaseController
{
    @Autowired
    private IArchiveLogService archiveLogService;

    /**
     * 查询档案日志列表
     */
    //@PreAuthorize("@ss.hasPermi('manage:ArchiveLog:list')")
    @GetMapping("/list")
    public TableDataInfo list(ArchiveLog archiveLog)
    {
        startPage();
        List<ArchiveLog> list = archiveLogService.selectArchiveLogList(archiveLog);
        return getDataTable(list);
    }

    /**
     * 导出档案日志列表
     */
//    @PreAuthorize("@ss.hasPermi('manage:ArchiveLog:export')")
    @Log(title = "档案日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ArchiveLog archiveLog)
    {
        List<ArchiveLog> list = archiveLogService.selectArchiveLogList(archiveLog);
        ExcelUtil<ArchiveLog> util = new ExcelUtil<ArchiveLog>(ArchiveLog.class);
        util.exportExcel(response, list, "档案日志数据");
    }

    /**
     * 获取档案日志详细信息
     */
//    @PreAuthorize("@ss.hasPermi('manage:ArchiveLog:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(archiveLogService.selectArchiveLogById(id));
    }

    /**
     * 新增档案日志
     */
//    @PreAuthorize("@ss.hasPermi('manage:ArchiveLog:add')")
    @Log(title = "档案日志", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ArchiveLog archiveLog)
    {
        return toAjax(archiveLogService.insertArchiveLog(archiveLog));
    }

    /**
     * 修改档案日志
     */
//    @PreAuthorize("@ss.hasPermi('manage:ArchiveLog:edit')")
    @Log(title = "档案日志", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ArchiveLog archiveLog)
    {
        return toAjax(archiveLogService.updateArchiveLog(archiveLog));
    }

    /**
     * 删除档案日志
     */
//    @PreAuthorize("@ss.hasPermi('manage:ArchiveLog:remove')")
    @Log(title = "档案日志", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(archiveLogService.deleteArchiveLogByIds(ids));
    }

    @GetMapping("/opLogs")
    public TableDataInfo opLogs(@RequestParam Long archiveId) {
        startPage();
        List<ArchiveLog> list = archiveLogService.selectOpLogs(archiveId);
        return getDataTable(list);
    }

    @GetMapping("/borrowLogs")
    public TableDataInfo borrowLogs(@RequestParam Long archiveId) {
        startPage();
        List<ArchiveLog> list = archiveLogService.selectBorrowLogs(archiveId);
        return getDataTable(list);
    }
}
