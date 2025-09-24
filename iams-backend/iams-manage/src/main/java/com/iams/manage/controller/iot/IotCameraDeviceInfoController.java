package com.iams.manage.controller.iot;

import com.iams.common.annotation.Log;
import com.iams.common.core.controller.BaseController;
import com.iams.common.core.domain.AjaxResult;
import com.iams.common.core.page.TableDataInfo;
import com.iams.common.enums.BusinessType;
import com.iams.common.utils.poi.ExcelUtil;
import com.iams.manage.domain.iot.IotCameraDeviceInfo;
import com.iams.manage.service.iot.IIotCameraDeviceInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

import static com.iams.common.core.domain.AjaxResult.success;

/**
 * 存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等Controller
 *
 * @author likang
 * @date 2025-02-25
 */
@RestController
@RequestMapping("/system/iot_camera_device_info")
public class IotCameraDeviceInfoController extends BaseController
{
    @Autowired
    private IIotCameraDeviceInfoService iotCameraDeviceInfoService;

    /**
     * 查询存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等列表
     */
    @GetMapping("/list")
    public TableDataInfo list(IotCameraDeviceInfo iotCameraDeviceInfo)
    {
        startPage();
        List<IotCameraDeviceInfo> list = iotCameraDeviceInfoService.selectIotCameraDeviceInfoList(iotCameraDeviceInfo);
        return getDataTable(list);
    }

    /**
     * 导出存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等列表
     */
    @Log(title = "存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, IotCameraDeviceInfo iotCameraDeviceInfo)
    {
        List<IotCameraDeviceInfo> list = iotCameraDeviceInfoService.selectIotCameraDeviceInfoList(iotCameraDeviceInfo);
        ExcelUtil<IotCameraDeviceInfo> util = new ExcelUtil<IotCameraDeviceInfo>(IotCameraDeviceInfo.class);
        util.exportExcel(response, list, "存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等数据");
    }

    /**
     * 获取存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等详细信息
     */
    @GetMapping(value = "/{deviceId}")
    public AjaxResult getInfo(@PathVariable("deviceId") Integer deviceId)
    {
        // deviceId 转为String
        String deviceIdStr = String.valueOf(deviceId);
        return success(iotCameraDeviceInfoService.selectIotCameraDeviceInfoByDeviceId(deviceIdStr));
    }

    /**
     * 新增存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等
     */
    @Log(title = "存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody IotCameraDeviceInfo iotCameraDeviceInfo)
    {
        return toAjax(iotCameraDeviceInfoService.insertIotCameraDeviceInfo(iotCameraDeviceInfo));
    }

    /**
     * 修改存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等
     */
    @Log(title = "存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody IotCameraDeviceInfo iotCameraDeviceInfo)
    {
        return toAjax(iotCameraDeviceInfoService.updateIotCameraDeviceInfo(iotCameraDeviceInfo));
    }

    /**
     * 删除存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等
     */
    @Log(title = "存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等", businessType = BusinessType.DELETE)
	@DeleteMapping("/{deviceIds}")
    public AjaxResult remove(@PathVariable String[] deviceIds)
    {
        return toAjax(iotCameraDeviceInfoService.deleteIotCameraDeviceInfoByDeviceIds(deviceIds));
    }
}
