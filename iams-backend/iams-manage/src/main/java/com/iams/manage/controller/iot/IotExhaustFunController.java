package com.iams.manage.controller.iot;

import com.iams.common.annotation.Log;
import com.iams.common.core.controller.BaseController;
import com.iams.common.core.domain.AjaxResult;
import com.iams.common.core.page.TableDataInfo;
import com.iams.common.enums.BusinessType;
import com.iams.common.utils.poi.ExcelUtil;
import com.iams.manage.domain.iot.IotExhaustFun;
import com.iams.manage.service.iot.IIotExhaustFunService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 排风扇设备信息Controller
 *
 * @author liuziqi
 * @date 2025-02-28
 */
@RestController
@RequestMapping("/system/iot_exhaust_fun")
public class IotExhaustFunController extends BaseController
{
    @Autowired
    private IIotExhaustFunService iotExhaustFunService;


    /**
     * 查询排风扇设备信息列表
     */
    @PreAuthorize("@ss.hasPermi('system:iot_exhaust_fun:list')")
    @GetMapping("/list")
    public TableDataInfo list(IotExhaustFun iotExhaustFun)
    {
        startPage();
        List<IotExhaustFun> list = iotExhaustFunService.selectIotExhaustFunList(iotExhaustFun);
        return getDataTable(list);
    }

    /**
     * 导出排风扇设备信息列表
     */
    @PreAuthorize("@ss.hasPermi('system:iot_exhaust_fun:export')")
    @Log(title = "排风扇设备信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, IotExhaustFun iotExhaustFun)
    {
        List<IotExhaustFun> list = iotExhaustFunService.selectIotExhaustFunList(iotExhaustFun);
        ExcelUtil<IotExhaustFun> util = new ExcelUtil<IotExhaustFun>(IotExhaustFun.class);
        util.exportExcel(response, list, "排风扇设备信息数据");
    }

    /**
     * 获取排风扇设备信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:iot_exhaust_fun:query')")
    @GetMapping(value = "/{deviceId}")
    public AjaxResult getInfo(@PathVariable("deviceId") String deviceId)
    {
        return success(iotExhaustFunService.selectIotExhaustFunByDeviceId(deviceId));
    }

    /**
     * 新增排风扇设备信息
     */
    @PreAuthorize("@ss.hasPermi('system:iot_exhaust_fun:add')")
    @Log(title = "排风扇设备信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody IotExhaustFun iotExhaustFun)
    {
        return toAjax(iotExhaustFunService.insertIotExhaustFun(iotExhaustFun));
    }

    /**
     * 修改排风扇设备信息
     */
    @PreAuthorize("@ss.hasPermi('system:iot_exhaust_fun:edit')")
    @Log(title = "排风扇设备信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody IotExhaustFun iotExhaustFun)
    {
        return toAjax(iotExhaustFunService.updateIotExhaustFun(iotExhaustFun));
    }

    /**
     * 删除排风扇设备信息
     */
    @PreAuthorize("@ss.hasPermi('system:iot_exhaust_fun:remove')")
    @Log(title = "排风扇设备信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{deviceIds}")
    public AjaxResult remove(@PathVariable String[] deviceIds)
    {
        return toAjax(iotExhaustFunService.deleteIotExhaustFunByDeviceIds(deviceIds));
    }

    // @PostMapping("/FunPowerOnOff")
    // public String FunPowerOnOff(@RequestBody Map<String, Object> remote){
    //     try {
    //         JSONObject jsonObject = new JSONObject();
    //         jsonObject.put("msg", (Boolean) remote.get("isPowerOn") ? "抽风机开启成功" : "抽风机关闭成功");
    //         jsonObject.put("ID", remote.get("deviceId"));
    //         String topic = iotExhaustFunService.getTopicById((String) remote.get("deviceId"));
    //         mqttService.publishByJSON(topic, jsonObject);
    //     } catch (MqttException e) {
    //         e.printStackTrace();
    //     }
    //     return (String) remote.get("deviceId");
    // }
    //
    // @PostMapping("/updateFanSpeed")
    // public String updateFanSpeed(@RequestBody Map<String, Object> remote){
    //     try {
    //         JSONObject jsonObject = new JSONObject();
    //         jsonObject.put("msg", "当前抽风机风速为： " + remote.get("fanSpeedName"));
    //         jsonObject.put("ID", remote.get("deviceId"));
    //         String topic = iotExhaustFunService.getTopicById((String) remote.get("deviceId"));
    //         mqttService.publishByJSON(topic, jsonObject);
    //     } catch (MqttException e) {
    //         e.printStackTrace();
    //     }
    //     return (String) remote.get("fanSpeedName");
    // }

    /**
     * 导入抽风机设备数据
     *
     * @param file           上传的文件
     * @param updateSupport 是否更新支持，如果已存在，则进行更新数据
     * @return 导入结果
     */
    @Log(title = "抽风机信息管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('system:iot_exhaust_fun:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception {
        ExcelUtil<IotExhaustFun> importUtil = new ExcelUtil<IotExhaustFun>(IotExhaustFun.class);
        List<IotExhaustFun> ExhaustFunList = importUtil.importExcel(file.getInputStream());
        String message = iotExhaustFunService.importExhaustFun(ExhaustFunList, updateSupport);
        return success(message);
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<IotExhaustFun> util = new ExcelUtil<IotExhaustFun>(IotExhaustFun.class);
        util.importTemplateExcel(response, "空调数据");
    }
}
