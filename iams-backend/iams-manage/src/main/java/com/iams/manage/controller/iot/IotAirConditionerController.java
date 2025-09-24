package com.iams.manage.controller.iot;

import com.iams.common.annotation.Log;
import com.iams.common.core.controller.BaseController;
import com.iams.common.core.domain.AjaxResult;
import com.iams.common.core.page.TableDataInfo;
import com.iams.common.enums.BusinessType;
import com.iams.common.utils.poi.ExcelUtil;
import com.iams.manage.domain.iot.IotAirConditioner;
import com.iams.manage.service.iot.IIotAirConditionerService;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 空调设备信息，存储空调的静态属性及实时状态Controller
 *
 * @author liuziqi
 * @date 2025-02-26
 */
@RestController
@RequestMapping("/system/iot_air_conditioner")
public class IotAirConditionerController extends BaseController
{
    @Autowired
    private IIotAirConditionerService iotAirConditionerService;


    /**
     * 查询空调设备信息，存储空调的静态属性及实时状态列表
     */
    @GetMapping("/list")
    public TableDataInfo list(IotAirConditioner iotAirConditioner)
    {
        startPage();
        List<IotAirConditioner> list = iotAirConditionerService.selectIotAirConditionerList(iotAirConditioner);
        return getDataTable(list);
    }

    /**
     * 导出空调设备信息，存储空调的静态属性及实时状态列表
     */
    @Log(title = "空调设备信息，存储空调的静态属性及实时状态", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, IotAirConditioner iotAirConditioner)
    {
        List<IotAirConditioner> list = iotAirConditionerService.selectIotAirConditionerList(iotAirConditioner);
        ExcelUtil<IotAirConditioner> util = new ExcelUtil<IotAirConditioner>(IotAirConditioner.class);
        util.exportExcel(response, list, "空调设备信息，存储空调的静态属性及实时状态数据");
    }

    /**
     * 获取空调设备信息，存储空调的静态属性及实时状态详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(iotAirConditionerService.selectIotAirConditionerById(id));
    }

    /**
     * 新增空调设备信息，存储空调的静态属性及实时状态
     */
    @Log(title = "空调设备信息，存储空调的静态属性及实时状态", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody IotAirConditioner iotAirConditioner)
    {
        return toAjax(iotAirConditionerService.insertIotAirConditioner(iotAirConditioner));
    }

    /**
     * 修改空调设备信息，存储空调的静态属性及实时状态
     */
    @Log(title = "空调设备信息，存储空调的静态属性及实时状态", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody IotAirConditioner iotAirConditioner)
    {
        return toAjax(iotAirConditionerService.updateIotAirConditioner(iotAirConditioner));
    }

    /**
     * 删除空调设备信息，存储空调的静态属性及实时状态
     */
    @Log(title = "空调设备信息，存储空调的静态属性及实时状态", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(iotAirConditionerService.deleteIotAirConditionerByIds(ids));
    }

    // @PostMapping("/PowerOnOff")
    // public String PowerOnOff(@RequestBody Map<String, Object> remote){
    //     try {
    //         JSONObject jsonObject = new JSONObject();
    //         String msg = (Boolean) remote.get("isPowerOn") ? "空调开启成功" : "空调关闭成功";
    //         jsonObject.put("msg", msg);
    //         jsonObject.put("ID", remote.get("id"));
    //         String topic = iotAirConditionerService.getTopicById((String) remote.get("id"));
    //         mqttService.publishByJSON(topic, jsonObject);
    //     } catch (MqttException e) {
    //         e.printStackTrace();
    //     }
    //     return (String)remote.get("id");
    // }
    //
    // @PostMapping("/changeAirTemp")
    // public String changeAirTemp(@RequestBody Map<String, Object> remote){
    //     try {
    //         JSONObject jsonObject = new JSONObject();
    //         jsonObject.put("msg", "当前空调温度为： " + remote.get("currentTemp") + " °C");
    //         jsonObject.put("ID", remote.get("id"));
    //         String topic = iotAirConditionerService.getTopicById((String) remote.get("id"));
    //         mqttService.publishByJSON(topic, jsonObject);
    //     } catch (MqttException e) {
    //         e.printStackTrace();
    //     }
    //     return remote.get("currentTemp").toString();
    // }
    //
    // @PostMapping("/updateMode")
    // public String updateMode(@RequestBody Map<String, Object> remote){
    //     try {
    //         JSONObject jsonObject = new JSONObject();
    //         jsonObject.put("msg", "当前空调模式为： " + remote.get("modeName"));
    //         jsonObject.put("ID", remote.get("id"));
    //         String topic = iotAirConditionerService.getTopicById((String) remote.get("id"));
    //         mqttService.publishByJSON(topic, jsonObject);
    //     } catch (MqttException e) {
    //         e.printStackTrace();
    //     }
    //     return remote.get("modeName").toString();
    // }
    //
    // @PostMapping("/updateFanSpeed")
    // public String updateFanSpeed(@RequestBody Map<String, Object> remote){
    //     try {
    //         JSONObject jsonObject = new JSONObject();
    //         jsonObject.put("msg", "当前空调风速为： " + remote.get("fanSpeedName"));
    //         jsonObject.put("ID", remote.get("id"));
    //         String topic = iotAirConditionerService.getTopicById((String) remote.get("id"));
    //         mqttService.publishByJSON(topic, jsonObject);
    //     } catch (MqttException e) {
    //         e.printStackTrace();
    //     }
    //     return remote.get("fanSpeedName").toString();
    // }

    /**
     * 导入空调设备数据
     *
     * @param file           上传的文件
     * @param updateSupport 是否更新支持，如果已存在，则进行更新数据
     * @return 导入结果
     */
    @Log(title = "空调信息管理", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception {
        ExcelUtil<IotAirConditioner> importUtil = new ExcelUtil<IotAirConditioner>(IotAirConditioner.class);
        List<IotAirConditioner> AirConditionerList = importUtil.importExcel(file.getInputStream());
        String message = iotAirConditionerService.importIotAirConditioner(AirConditionerList, updateSupport);
        return success(message);
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<IotAirConditioner> util = new ExcelUtil<IotAirConditioner>(IotAirConditioner.class);
        util.importTemplateExcel(response, "空调数据");
    }
}
