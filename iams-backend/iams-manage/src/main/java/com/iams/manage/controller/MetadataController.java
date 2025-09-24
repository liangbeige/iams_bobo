package com.iams.manage.controller;

import com.iams.common.annotation.Log;
import com.iams.common.core.controller.BaseController;
import com.iams.common.core.domain.AjaxResult;
import com.iams.common.core.page.TableDataInfo;
import com.iams.common.enums.BusinessType;
import com.iams.manage.domain.Metadata;
import com.iams.manage.service.IMetadataService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 元数据Controller
 *
 * @author iams
 * @date 2024-01-01
 */
@RestController
@RequestMapping("/manage/document/metadata")
public class MetadataController extends BaseController {

    @Autowired
    private IMetadataService metadataService;

    /**
     * 查询元数据列表
     */
    @PreAuthorize("@ss.hasPermi('manage:metadata:list')")
    @GetMapping("/list")
    public TableDataInfo list(Metadata metadata) {
        startPage();
        List<Metadata> list = metadataService.selectMetadataList(metadata);
        return getDataTable(list);
    }

    /**
     * 查询所有元数据列表（不分页）
     */
    @PreAuthorize("@ss.hasPermi('manage:metadata:list')")
    @GetMapping("/list-all")
    public AjaxResult listAll(Metadata metadata) {
        List<Metadata> list = metadataService.selectMetadataList(metadata);
        return AjaxResult.success(list);
    }

    /**
     * 导出元数据列表
     */
//    @PreAuthorize("@ss.hasPermi('manage:metadata:export')")
//    @Log(title = "元数据", businessType = BusinessType.EXPORT)
//    @PostMapping("/export")
//    public void export(HttpServletResponse response, Metadata metadata) {
//        List<Metadata> list = metadataService.selectMetadataList(metadata);
//        ExcelUtil<Metadata> util = new ExcelUtil<Metadata>(Metadata.class);
//        util.exportExcel(response, list, "元数据数据");
//    }

    /**
     * 获取元数据详细信息
     */
    @PreAuthorize("@ss.hasPermi('manage:metadata:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Integer id) {
        return AjaxResult.success(metadataService.selectMetadataById(id));
    }

    /**
     * 新增元数据
     */
    @PreAuthorize("@ss.hasPermi('manage:metadata:add')")
    @Log(title = "元数据", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody Metadata metadata) {
        return toAjax(metadataService.insertMetadata(metadata));
    }

    /**
     * 修改元数据
     */
    @PreAuthorize("@ss.hasPermi('manage:metadata:edit')")
    @Log(title = "元数据", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody Metadata metadata) {
        return toAjax(metadataService.updateMetadata(metadata));
    }

    /**
     * 删除元数据
     */
    @PreAuthorize("@ss.hasPermi('manage:metadata:remove')")
    @Log(title = "元数据", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Integer[] ids) {
        return toAjax(metadataService.deleteMetadataByIds(ids));
    }

    /**
     * 复制元数据
     */
    @PreAuthorize("@ss.hasPermi('manage:metadata:copy')")
    @Log(title = "元数据", businessType = BusinessType.INSERT)
    @PostMapping("/copy/{id}")
    public AjaxResult copy(@PathVariable("id") Integer id) {
        return toAjax(metadataService.copyMetadata(id));
    }

    /**
     * 应用元数据到文档
     */
    @PreAuthorize("@ss.hasPermi('manage:metadata:apply')")
    @Log(title = "元数据", businessType = BusinessType.UPDATE)
    @PostMapping("/apply")
    public AjaxResult apply(@RequestBody Map<String, String> params) {
        String metadataIds = params.get("metadataIds");
        String documentIds = params.get("documentIds");
        return toAjax(metadataService.applyMetadataToDocuments(metadataIds, documentIds));
    }
}
