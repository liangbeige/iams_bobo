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
import com.iams.manage.domain.Watermark;
import com.iams.manage.service.IWatermarkService;
import com.iams.common.utils.poi.ExcelUtil;
import com.iams.common.core.page.TableDataInfo;

/**
 * 水印管理Controller
 * 
 * @author zhjm
 * @date 2025-01-05
 */
@RestController
@RequestMapping("/manage/watermark")
public class WatermarkController extends BaseController
{
    @Autowired
    private IWatermarkService watermarkService;

    /**
     * 查询水印管理列表
     */
    @PreAuthorize("@ss.hasPermi('manage:watermark:list')")
    @GetMapping("/list")
    public TableDataInfo list(Watermark watermark)
    {
        startPage();
        List<Watermark> list = watermarkService.selectWatermarkList(watermark);
        return getDataTable(list);
    }

    /**
     * 导出水印管理列表
     */
    @PreAuthorize("@ss.hasPermi('manage:watermark:export')")
    @Log(title = "水印管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Watermark watermark)
    {
        List<Watermark> list = watermarkService.selectWatermarkList(watermark);
        ExcelUtil<Watermark> util = new ExcelUtil<Watermark>(Watermark.class);
        util.exportExcel(response, list, "水印管理数据");
    }

    /**
     * 获取水印管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('manage:watermark:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(watermarkService.selectWatermarkById(id));
    }

    /**
     * 新增水印管理
     */
    @PreAuthorize("@ss.hasPermi('manage:watermark:add')")
    @Log(title = "水印管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Watermark watermark)
    {
        return toAjax(watermarkService.insertWatermark(watermark));
    }

    /**
     * 修改水印管理
     */
    @PreAuthorize("@ss.hasPermi('manage:watermark:edit')")
    @Log(title = "水印管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Watermark watermark)
    {
        return toAjax(watermarkService.updateWatermark(watermark));
    }

    /**
     * 删除水印管理
     */
    @PreAuthorize("@ss.hasPermi('manage:watermark:remove')")
    @Log(title = "水印管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(watermarkService.deleteWatermarkByIds(ids));
    }

    /**
     * 设置默认水印
     */
    @PreAuthorize("@ss.hasPermi('manage:watermark:edit')")
    @Log(title = "水印管理", businessType = BusinessType.UPDATE)
    @PutMapping("/setDefault/{id}")
    public AjaxResult setDefault(@PathVariable Long id)
    {
        return toAjax(watermarkService.setDefault(id));
    }

    /**
     * 获取默认水印
     */
    @PreAuthorize("@ss.hasPermi('manage:watermark:query')")
    @GetMapping("/getDefault")
    public AjaxResult getDefault()
    {
        return success(watermarkService.selectDefaultWatermark());
    }
}
