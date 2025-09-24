package com.iams.manage.controller.iot;

import com.iams.common.annotation.Log;
import com.iams.common.core.controller.BaseController;
import com.iams.common.core.domain.AjaxResult;
import com.iams.common.core.page.TableDataInfo;
import com.iams.common.enums.BusinessType;
import com.iams.common.utils.poi.ExcelUtil;
import com.iams.manage.domain.iot.IotWarehouse;
import com.iams.manage.service.iot.IIotWarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 仓库管理Controller
 *
 * @author pangzhongzheng
 * @date 2025-02-27
 */
@RestController
@RequestMapping("/system/warehouse")
public class IotWarehouseController extends BaseController
{
    @Autowired
    private IIotWarehouseService iotWarehouseService;

    /**
     * 查询仓库管理列表
     */
    @GetMapping("/list")
    public TableDataInfo list(IotWarehouse iotWarehouse)
    {
        startPage();
        List<IotWarehouse> list = iotWarehouseService.selectIotWarehouseList(iotWarehouse);
        return getDataTable(list);
    }

    /**
     * 导出仓库管理列表
     */
    @Log(title = "仓库管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, IotWarehouse iotWarehouse)
    {
        List<IotWarehouse> list = iotWarehouseService.selectIotWarehouseList(iotWarehouse);
        ExcelUtil<IotWarehouse> util = new ExcelUtil<IotWarehouse>(IotWarehouse.class);
        util.exportExcel(response, list, "仓库管理数据");
    }

    /**
     * 获取仓库管理详细信息
     */
    @GetMapping(value = "/{warehouseId}")
    public AjaxResult getInfo(@PathVariable("warehouseId") Long warehouseId)
    {
        return success(iotWarehouseService.selectIotWarehouseByWarehouseId(warehouseId));
    }

    /**
     * 新增仓库管理
     */
    @Log(title = "仓库管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody IotWarehouse iotWarehouse)
    {
        return toAjax(iotWarehouseService.insertIotWarehouse(iotWarehouse));
    }

    /**
     * 修改仓库管理
     */
    @Log(title = "仓库管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody IotWarehouse iotWarehouse)
    {
        return toAjax(iotWarehouseService.updateIotWarehouse(iotWarehouse));
    }

    /**
     * 删除仓库管理
     */
    @Log(title = "仓库管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{warehouseIds}")
    public AjaxResult remove(@PathVariable Long[] warehouseIds)
    {
        return toAjax(iotWarehouseService.deleteIotWarehouseByWarehouseIds(warehouseIds));
    }
}
