package com.iams.manage.controller;

import java.time.LocalDate;
import java.util.List;

import com.iams.manage.domain.StatisticsVO.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.iams.common.annotation.Log;
import com.iams.common.core.controller.BaseController;
import com.iams.common.core.domain.AjaxResult;
import com.iams.common.enums.BusinessType;
import com.iams.manage.domain.Statistics;
import com.iams.manage.service.IStatisticsService;
import com.iams.common.utils.poi.ExcelUtil;
import com.iams.common.core.page.TableDataInfo;

/**
 * 统计数据Controller
 * 
 * @author LiuTao
 * @date 2025-04-04
 */
@RestController
@RequestMapping("/manage/statistics")
public class StatisticsController extends BaseController
{
    @Autowired
    private IStatisticsService statisticsService;

    /**
     * 查询统计数据列表
     */
    //@PreAuthorize("@ss.hasPermi('manage:statistics:list')")
    @GetMapping("/list")
    public TableDataInfo list(Statistics statistics)
    {
        startPage();
        List<Statistics> list = statisticsService.selectStatisticsList(statistics);
        return getDataTable(list);
    }

//    @GetMapping("/latest")
//    public AjaxResult getLatest()
//    {
//        return success(statisticsService.selectLatestStatistics());
//    }

    /**
     * 导出统计数据列表
     */
    //@PreAuthorize("@ss.hasPermi('manage:statistics:export')")
    @Log(title = "统计数据", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Statistics statistics)
    {
        List<Statistics> list = statisticsService.selectStatisticsList(statistics);
        ExcelUtil<Statistics> util = new ExcelUtil<Statistics>(Statistics.class);
        util.exportExcel(response, list, "统计数据数据");
    }

    /**
     * 获取统计数据详细信息
     */
    //@PreAuthorize("@ss.hasPermi('manage:statistics:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(statisticsService.selectStatisticsById(id));
    }

    /**
     * 新增统计数据
     */
    //@PreAuthorize("@ss.hasPermi('manage:statistics:add')")
    @Log(title = "统计数据", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Statistics statistics)
    {
        return toAjax(statisticsService.insertStatistics(statistics));
    }

    /**
     * 修改统计数据
     */
    //@PreAuthorize("@ss.hasPermi('manage:statistics:edit')")
    @Log(title = "统计数据", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Statistics statistics)
    {
        return toAjax(statisticsService.updateStatistics(statistics));
    }

    /**
     * 删除统计数据
     */
    //@PreAuthorize("@ss.hasPermi('manage:statistics:remove')")
    @Log(title = "统计数据", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(statisticsService.deleteStatisticsByIds(ids));
    }

    /**
     * 获取最新统计数据
     */
    @GetMapping("/latest")
    public ResponseEntity<StatisticsVO> getLatestStatistics() {
        StatisticsVO statistics = statisticsService.getLatestStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * 获取日统计数据
     */
    @GetMapping("/daily")
    public ResponseEntity<DailyStatisticsVO> getDailyStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        DailyStatisticsVO statistics = statisticsService.getDailyStatistics(date);
        return ResponseEntity.ok(statistics);
    }

    /**
     * 获取月统计数据
     */
    @GetMapping("/monthly")
    public ResponseEntity<MonthlyStatisticsVO> getMonthlyStatistics(
            @RequestParam int year,
            @RequestParam int month) {
        MonthlyStatisticsVO statistics = statisticsService.getMonthlyStatistics(year, month);
        return ResponseEntity.ok(statistics);
    }

    /**
     * 获取最近几天的借出数量数据
     */
    @GetMapping("/recent-borrow")
    public ResponseEntity<List<StatisticsVO>> getRecentBorrowData(
            @RequestParam(defaultValue = "7") int days) {
        List<StatisticsVO> statistics = statisticsService.getRecentBorrowData(days);
        return ResponseEntity.ok(statistics);
    }
}
