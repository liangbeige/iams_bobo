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
import com.iams.manage.domain.Document;
import com.iams.manage.service.IDocumentService;
import com.iams.common.utils.poi.ExcelUtil;
import com.iams.common.core.page.TableDataInfo;

/**
 * 文档信息Controller
 * 
 * @author zhjm
 * @date 2025-01-10
 */
@RestController
@RequestMapping("/manage/document")
public class DocumentController extends BaseController
{
    @Autowired
    private IDocumentService documentService;

    /**
     * 查询文档信息列表
     */
    @PreAuthorize("@ss.hasPermi('manage:document:list')")
    @GetMapping("/list")
    public TableDataInfo list(Document document)
    {
        startPage();
        List<Document> list = documentService.selectDocumentList(document);
        return getDataTable(list);
    }

    /**
     * 导出文档信息列表
     */
    @PreAuthorize("@ss.hasPermi('manage:document:export')")
    @Log(title = "文档信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Document document)
    {
        List<Document> list = documentService.selectDocumentList(document);
        ExcelUtil<Document> util = new ExcelUtil<Document>(Document.class);
        util.exportExcel(response, list, "文档信息数据");
    }

    /**
     * 获取文档信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('manage:document:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(documentService.selectDocumentById(id));
    }

    /**
     * 新增文档信息
     */
    @PreAuthorize("@ss.hasPermi('manage:document:add')")
    @Log(title = "文档信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Document document)
    {
        return toAjax(documentService.insertDocument(document));
    }

    /**
     * 修改文档信息
     */
    @PreAuthorize("@ss.hasPermi('manage:document:edit')")
    @Log(title = "文档信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Document document)
    {
        return toAjax(documentService.updateDocument(document));
    }

    /**
     * 删除文档信息
     */
    @PreAuthorize("@ss.hasPermi('manage:document:remove')")
    @Log(title = "文档信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(documentService.deleteDocumentByIds(ids));
    }
}
