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
import com.iams.manage.domain.Volume;
import com.iams.manage.service.IVolumeService;
import com.iams.common.utils.poi.ExcelUtil;
import com.iams.common.core.page.TableDataInfo;

/**
 * 组卷管理Controller
 * 
 * @author zhld
 * @date 2025-06-06
 */
@RestController
@RequestMapping("/manage/volume")
public class VolumeController extends BaseController
{
    @Autowired
    private IVolumeService volumeService;

    /**
     * 查询组卷管理列表
     */
    @PreAuthorize("@ss.hasPermi('manage:volume:list')")
    @GetMapping("/list")
    public TableDataInfo list(Volume volume)
    {
        startPage();
        List<Volume> list = volumeService.selectVolumeList(volume);
        return getDataTable(list);
    }

    /**
     * 导出组卷管理列表
     */
    @PreAuthorize("@ss.hasPermi('manage:volume:export')")
    @Log(title = "组卷管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Volume volume)
    {
        List<Volume> list = volumeService.selectVolumeList(volume);
        ExcelUtil<Volume> util = new ExcelUtil<Volume>(Volume.class);
        util.exportExcel(response, list, "组卷管理数据");
    }

    /**
     * 获取组卷管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('manage:volume:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(volumeService.selectVolumeById(id));
    }

    /**
     * 新增组卷管理
     */
    @PreAuthorize("@ss.hasPermi('manage:volume:add')")
    @Log(title = "组卷管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Volume volume)
    {
        return toAjax(volumeService.insertVolume(volume));
    }

    /**
     * 修改组卷管理
     */
    @PreAuthorize("@ss.hasPermi('manage:volume:edit')")
    @Log(title = "组卷管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Volume volume)
    {
        return toAjax(volumeService.updateVolume(volume));
    }

    /**
     * 删除组卷管理
     */
    @PreAuthorize("@ss.hasPermi('manage:volume:remove')")
    @Log(title = "组卷管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(volumeService.deleteVolumeByIds(ids));
    }
}
