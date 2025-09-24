package com.iams.manage.service.iot;// package com.ruoyi.system.service;

import com.iams.manage.domain.iot.IotCameraDeviceInfo;

import java.util.List;
// import com.ruoyi.system.domain.IotCameraDeviceInfo;

/**
 * 存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等Service接口
 *
 * @author likang
 * @date 2025-02-25
 */
public interface IIotCameraDeviceInfoService
{
    /**
     * 查询存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等
     *
     * @param deviceId 存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等主键
     * @return 存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等
     */
    public IotCameraDeviceInfo selectIotCameraDeviceInfoByDeviceId(String deviceId);

    /**
     * 查询存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等列表
     *
     * @param iotCameraDeviceInfo 存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等
     * @return 存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等集合
     */
    public List<IotCameraDeviceInfo> selectIotCameraDeviceInfoList(IotCameraDeviceInfo iotCameraDeviceInfo);

    /**
     * 新增存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等
     *
     * @param iotCameraDeviceInfo 存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等
     * @return 结果
     */
    public int insertIotCameraDeviceInfo(IotCameraDeviceInfo iotCameraDeviceInfo);

    /**
     * 修改存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等
     *
     * @param iotCameraDeviceInfo 存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等
     * @return 结果
     */
    public int updateIotCameraDeviceInfo(IotCameraDeviceInfo iotCameraDeviceInfo);

    /**
     * 批量删除存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等
     *
     * @param deviceIds 需要删除的存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等主键集合
     * @return 结果
     */
    public int deleteIotCameraDeviceInfoByDeviceIds(String[] deviceIds);

    /**
     * 删除存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等信息
     *
     * @param deviceId 存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等主键
     * @return 结果
     */
    public int deleteIotCameraDeviceInfoByDeviceId(String deviceId);
}
