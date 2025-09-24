package com.iams.manage.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.iams.manage.domain.Compartment;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.iams.common.annotation.Log;
import com.iams.common.core.controller.BaseController;
import com.iams.common.core.domain.AjaxResult;
import com.iams.common.enums.BusinessType;
import com.iams.manage.domain.Cabinet;
import com.iams.manage.service.ICabinetService;
import com.iams.common.utils.poi.ExcelUtil;
import com.iams.common.core.page.TableDataInfo;

/**
 * 档案柜管理Controller
 * 
 * @author zhjm
 * @date 2025-01-06
 */
@RestController
@RequestMapping("/manage/cabinet")
public class CabinetController extends BaseController
{
    @Autowired
    private ICabinetService cabinetService;

    /**
     * 查询档案柜管理列表
     */
    @PreAuthorize("@ss.hasPermi('manage:cabinet:list')")
    @GetMapping("/list")
    public TableDataInfo list(Cabinet cabinet)
    {
        startPage();
        List<Cabinet> list = cabinetService.selectCabinetList(cabinet);
        return getDataTable(list);
    }

    /**
     * 查询档案柜IDs列表
     */
    @PreAuthorize("@ss.hasPermi('manage:cabinet:query')")
    @GetMapping("/get-ids")
    public AjaxResult getCabinetIds(Cabinet cabinet)
    {
        List<Long> ids = cabinetService.selectCabinetIds(cabinet);
        return success(ids);
    }

    @PreAuthorize("@ss.hasPermi('manage:cabinet:query')")
    @GetMapping("/list-by-qunolist")
    public AjaxResult getCabinetByQuNoList(@RequestParam String quNoList)
    {
        String[] quNos = quNoList.split(",");
        System.out.println(Arrays.toString(quNos));
        List<Cabinet> cabinetList;
        try {
            cabinetList = cabinetService.getCabinetByQuNoList(quNos);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return AjaxResult.error("未找到");
        }
        return success(cabinetList);
    }


    /**
     * 导出档案柜管理列表
     */
    @PreAuthorize("@ss.hasPermi('manage:cabinet:export')")
    @Log(title = "档案柜管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Cabinet cabinet)
    {
        List<Cabinet> list = cabinetService.selectCabinetList(cabinet);
        ExcelUtil<Cabinet> util = new ExcelUtil<Cabinet>(Cabinet.class);
        util.exportExcel(response, list, "档案柜管理数据");
    }

    /**
     * 获取档案柜管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('manage:cabinet:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(cabinetService.selectCabinetById(id));
    }

    /**
     * 新增档案柜管理
     */
    @PreAuthorize("@ss.hasPermi('manage:cabinet:add')")
    @Log(title = "档案柜管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Cabinet cabinet)
    {
        return toAjax(cabinetService.insertCabinet(cabinet));
    }

    /**
     * 修改档案柜管理
     */
    @PreAuthorize("@ss.hasPermi('manage:cabinet:edit')")
    @Log(title = "档案柜管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Cabinet cabinet)
    {
        return toAjax(cabinetService.updateCabinet(cabinet));
    }

    /**
     * 删除档案柜管理
     */
    @PreAuthorize("@ss.hasPermi('manage:cabinet:remove')")
    @Log(title = "档案柜管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(cabinetService.deleteCabinetByIds(ids));
    }

    /**
     * 更新档案柜尺寸
     */
    @PreAuthorize("@ss.hasPermi('manage:cabinet:update')")
    @Log(title = "档案柜管理", businessType = BusinessType.UPDATE)
    @PutMapping("/size")
    public AjaxResult updateSize(@RequestParam String shitiLocation, @RequestParam String option)
    {
        return toAjax(cabinetService.updateSize(shitiLocation, option));
    }

    /**
     * 同步位置统计数据
     * @param locationData 位置统计数据，格式：{"位置": 数量}
     * @return 同步结果
     */
    @PostMapping("/synchronize")
    public ResponseEntity<?> synchronizeLocationStats(@RequestBody Map<String, Integer> locationData) {
        try {
            // 验证数据
            if (locationData == null || locationData.isEmpty()) {
                return ResponseEntity.badRequest().body("位置数据不能为空");
            }

            // 调用服务层处理数据
            boolean result = cabinetService.synchronizeLocationStats(locationData);
            if (result) {
                return ResponseEntity.ok().body(Map.of(
                        "success", true,
                        "message", "位置统计数据同步成功",
                        "syncedCount", locationData.size()
                ));
            } else {
                return ResponseEntity.internalServerError().body("同步失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("同步过程中发生错误: " + e.getMessage());
        }
    }
}
