package com.iams.manage.controller;

import com.iams.common.annotation.Log;
import com.iams.common.core.controller.BaseController;
import com.iams.common.core.domain.AjaxResult;
import com.iams.common.core.page.TableDataInfo;
import com.iams.common.enums.BusinessType;
import com.iams.manage.domain.ArchiveBrowseLog;
import com.iams.manage.service.IArchiveBrowseLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manage/browseLog")
public class ArchiveBrowseLogController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(ArchiveBrowseLogController.class);

    @Autowired
    private IArchiveBrowseLogService browseLogService;

    /**
     * 新增档案浏览日志
     */
    @Log(title = "档案浏览日志", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ArchiveBrowseLog browseLog) {
        logger.info("接收到浏览日志创建请求: {}", browseLog.toString());
        // 检查所有字段是否都有值
        logger.info("字段检查 - archive_danghao: {}", browseLog.getArchiveDanghao());
        logger.info("字段检查 - archive_id: {}", browseLog.getArchiveId());
        logger.info("字段检查 - archive_name: {}", browseLog.getArchiveName());
        logger.info("字段检查 - viewer_id: {}", browseLog.getViewerId());
        logger.info("字段检查 - viewer_name: {}", browseLog.getViewerName());
        logger.info("字段检查 - document_name: {}", browseLog.getDocumentName());
        logger.info("字段检查 - document_id: {}", browseLog.getDocumentId());
        int rows = browseLogService.insertArchiveBrowseLog(browseLog);
        if (rows > 0) {
            // 返回包含ID的对象
            return AjaxResult.success(browseLog);
        }
        return AjaxResult.error("新增失败");
    }

    /**
     * 修改档案浏览日志
     */
    @Log(title = "档案浏览日志", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public AjaxResult update(@PathVariable Long id, @RequestBody ArchiveBrowseLog browseLog) {
        browseLog.setId(id);
        return toAjax(browseLogService.updateArchiveBrowseLog(browseLog));
    }

    /**
     * 根据ID获取档案浏览日志详情
     */
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return AjaxResult.success(browseLogService.selectArchiveBrowseLogById(id));
    }

    /**
     * 特殊更新接口（用于页面关闭时的sendBeacon请求）
     */
    @PostMapping("/update")
    public AjaxResult beaconUpdate(@RequestBody ArchiveBrowseLog browseLog) {
        if (browseLog.getId() == null) {
            return AjaxResult.error("ID不能为空");
        }
        return toAjax(browseLogService.updateArchiveBrowseLog(browseLog));
    }

    /**
     * 获取文档的浏览日志列表
     */
    @GetMapping("/document/{documentId}")
    public TableDataInfo listByDocument(@PathVariable Long documentId,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        startPage();
        List<ArchiveBrowseLog> list = browseLogService.selectBrowseLogByDocumentId(documentId);
        return getDataTable(list);
    }
    /**
     * 获取档案的浏览日志列表
     */
    @GetMapping("/archive/{archiveId}")
    public TableDataInfo getBrowseLogByArchiveId(@PathVariable Long archiveId,
                                                 @RequestParam(defaultValue = "1") Integer pageNum,
                                                 @RequestParam(defaultValue = "10") Integer pageSize) {
        startPage();
        List<ArchiveBrowseLog> list = browseLogService.selectBrowseLogByArchiveId(archiveId);
        return getDataTable(list);
    }

    /**
     * 获取用户的浏览日志列表
     */
    @GetMapping("/viewer/{viewerId}")
    public TableDataInfo listByViewer(@PathVariable Long viewerId) {
        startPage();
        List<ArchiveBrowseLog> list = browseLogService.selectBrowseLogByViewerId(viewerId);
        return getDataTable(list);
    }

    /**
     * 删除档案浏览日志
     */
    @Log(title = "档案浏览日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        return toAjax(browseLogService.deleteArchiveBrowseLogById(id));
    }

    /**
     * 批量删除档案浏览日志
     */
    @Log(title = "档案浏览日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/batch/{ids}")
    public AjaxResult batchRemove(@PathVariable Long[] ids) {
        return toAjax(browseLogService.deleteArchiveBrowseLogByIds(ids));
    }

}
