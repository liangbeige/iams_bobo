package com.iams.manage.controller.iot;

import com.iams.common.annotation.Log;
import com.iams.common.core.controller.BaseController;
import com.iams.common.core.domain.AjaxResult;
import com.iams.common.core.page.TableDataInfo;
import com.iams.common.enums.BusinessType;
import com.iams.common.utils.poi.ExcelUtil;
import com.iams.manage.domain.iot.GdlIpInfo;

import com.iams.manage.service.iot.IGdlIpInfoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.List;

/**
 * 固定列IP信息管理Controller
 *
 * @author likang
 * @date 2025-04-18
 */
@RestController
@RequestMapping("/system/info")
public class GdlIpInfoController extends BaseController
{
    @Autowired
    private IGdlIpInfoService gdlIpInfoService;

    /**
     * 查询固定列IP信息管理列表
     */
    @GetMapping("/list")
    public TableDataInfo list(GdlIpInfo gdlIpInfo)
    {
        startPage();
        List<GdlIpInfo> list = gdlIpInfoService.selectGdlIpInfoList(gdlIpInfo);
        return getDataTable(list);
    }

    /**
     * 导出固定列IP信息管理列表
     */
    @Log(title = "固定列IP信息管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, GdlIpInfo gdlIpInfo)
    {
        List<GdlIpInfo> list = gdlIpInfoService.selectGdlIpInfoList(gdlIpInfo);
        ExcelUtil<GdlIpInfo> util = new ExcelUtil<GdlIpInfo>(GdlIpInfo.class);
        util.exportExcel(response, list, "固定列IP信息管理数据");
    }

    /**
     * 获取固定列IP信息管理详细信息
     */
    @GetMapping(value = "/{gdlNo}")
    public AjaxResult getInfo(@PathVariable("gdlNo") String gdlNo)
    {
        return success(gdlIpInfoService.selectGdlIpInfoByGdlNo(gdlNo));
    }

    /**
     * 新增固定列IP信息管理
     */
    @Log(title = "固定列IP信息管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GdlIpInfo gdlIpInfo)
    {
        return toAjax(gdlIpInfoService.insertGdlIpInfo(gdlIpInfo));
    }

    /**
     * 修改固定列IP信息管理
     */
    @Log(title = "固定列IP信息管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GdlIpInfo gdlIpInfo)
    {
        return toAjax(gdlIpInfoService.updateGdlIpInfo(gdlIpInfo));
    }

    /**
     * 删除固定列IP信息管理
     */
    @Log(title = "固定列IP信息管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{gdlNos}")
    public AjaxResult remove(@PathVariable String[] gdlNos)
    {
        return toAjax(gdlIpInfoService.deleteGdlIpInfoByGdlNos(gdlNos));
    }
}
