package com.iams.manage.controller.iot;

import com.iams.common.annotation.Log;
import com.iams.common.core.controller.BaseController;
import com.iams.common.core.domain.AjaxResult;
import com.iams.common.core.page.TableDataInfo;
import com.iams.common.enums.BusinessType;
import com.iams.common.utils.poi.ExcelUtil;
import com.iams.manage.domain.iot.IotTemperatureThreshold;
import com.iams.manage.service.iot.IIotTemperatureThresholdService;
import com.iams.manage.service.iot.IIotWarehouseService;
// import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 温度感知Controller
 *
 * @author pangzhongzheng
 * @date 2025-03-12
 */
@RestController
@EnableScheduling // 启用定时任务支持
@RequestMapping("/system/threshold")
public class IotTemperatureThresholdController extends BaseController
{
    @Autowired
    private IIotTemperatureThresholdService iotTemperatureThresholdService;

    @Autowired
    private IIotWarehouseService iotWarehouseService;

    // @Autowired
    // private MqttService mqttService;


    /**
     * 查询温度感知列表
     */
    @GetMapping("/list")
    public TableDataInfo list(IotTemperatureThreshold iotTemperatureThreshold)
    {
        startPage();
        List<IotTemperatureThreshold> list = iotTemperatureThresholdService.selectIotTemperatureThresholdList(iotTemperatureThreshold);
        return getDataTable(list);
    }

    /**
     * 导出温度感知列表
     */
    @Log(title = "温度感知", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, IotTemperatureThreshold iotTemperatureThreshold)
    {
        List<IotTemperatureThreshold> list = iotTemperatureThresholdService.selectIotTemperatureThresholdList(iotTemperatureThreshold);
        ExcelUtil<IotTemperatureThreshold> util = new ExcelUtil<IotTemperatureThreshold>(IotTemperatureThreshold.class);
        util.exportExcel(response, list, "温度感知数据");
    }

    /**
     * 获取温度感知详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(iotTemperatureThresholdService.selectIotTemperatureThresholdById(id));
    }

    /**
     * 新增温度感知
     */
    @Log(title = "温度感知", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody IotTemperatureThreshold iotTemperatureThreshold)
    {
        return toAjax(iotTemperatureThresholdService.insertIotTemperatureThreshold(iotTemperatureThreshold));
    }

    /**
     * 修改温度感知
     */
    @Log(title = "温度感知", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody IotTemperatureThreshold iotTemperatureThreshold)
    {
        return toAjax(iotTemperatureThresholdService.updateIotTemperatureThreshold(iotTemperatureThreshold));
    }

    /**
     * 删除温度感知
     */
    @Log(title = "温度感知", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(iotTemperatureThresholdService.deleteIotTemperatureThresholdByIds(ids));
    }


    /**
    * 实时检测温度情况，向空调发送开关请求
     **/
        // @Scheduled(fixedRate = 5000) // 每 5 秒执行一次
//         public void scheduledTask() throws MqttException {
//             // 查询特定仓库区域的温度数据
//             IotWarehouse query = new IotWarehouse();
//             query.setLocation("区域1"); // 设置需要查询的区域
//             query.setWarehouseName("仓库A1"); // 设置需要查询的仓库名称
//
//             // 查询数据
//             List<IotWarehouse> warehouseList = iotWarehouseService.selectIotWarehouseList(query);
//
//             if (warehouseList != null && !warehouseList.isEmpty()) {
//                 // 使用 Stream API 找到最新的数据
//                 IotWarehouse latestWarehouse = warehouseList.stream()
//                         .max(Comparator.comparing(IotWarehouse::getLastUpdated))
//                         .orElse(null);
//
//                 if (latestWarehouse != null) {
//                     // 获取最新数据的 lastUpdated 时间
//                     Date lastUpdatedDate = latestWarehouse.getLastUpdated();
//
//                     // 将 Date 转换为 LocalDateTime
//                     LocalDateTime lastUpdated = lastUpdatedDate.toInstant()
//                             .atZone(ZoneId.systemDefault())
//                             .toLocalDateTime();
// //                    System.out.println(lastUpdated);
//
//                     // 获取当前时间
//                     LocalDateTime now = LocalDateTime.now();
//
//                     // 计算当前时间前后 1 分钟的时间范围
//                     LocalDateTime fiveMinutesAgo = now.minusMinutes(1);
//                     LocalDateTime fiveMinutesLater = now.plusMinutes(1);
//
//                     // 检查 lastUpdated 是否在当前时间前后 1 分钟内
//                     if (lastUpdated.isAfter(fiveMinutesAgo) && lastUpdated.isBefore(fiveMinutesLater)) {
//                         // lastUpdated 在允许的时间范围内，继续执行后续逻辑
//                         BigDecimal temperature = latestWarehouse.getTemperature();
//
//                         // 查询特定仓库区域的温度阈值
//                         IotTemperatureThreshold query2 = new IotTemperatureThreshold();
//                         query2.setLocation("区域1"); // 设置需要查询的区域
//                         query2.setWarehouseName("仓库A1"); // 设置需要查询的仓库名称
//
//                         // 查询数据
//                         List<IotTemperatureThreshold> temperatureThresholdList = iotTemperatureThresholdService.selectIotTemperatureThresholdList(query2);
//
//                         for (IotTemperatureThreshold threshold : temperatureThresholdList) {
//                             if (threshold.getWarehouseName().equals(latestWarehouse.getWarehouseName()) && threshold.getLocation().equals(latestWarehouse.getLocation())) {
//                                 // 阈值比较,提交空调变动
//                                 if (threshold.getThresholdMin() >= temperature.intValue()) {
//                                     JSONObject jsonObject = new JSONObject();
//                                     String msg = "空调关闭";
//                                     jsonObject.put("msg", msg);
//                                     String topic = "ck/test";
//                                     mqttService.publishByJSON(topic, jsonObject);
//                                 } else if (threshold.getThresholdMax() <= temperature.intValue()) {
//                                     JSONObject jsonObject = new JSONObject();
//                                     String msg = "空调开启";
//                                     jsonObject.put("msg", msg);
//                                     String topic = "ck/test";
//                                     mqttService.publishByJSON(topic, jsonObject);
//                                 }
// //                                System.out.println("66666");
//                             }
//                         }
//                     }
//
//                 }
//             }
//         }
}

