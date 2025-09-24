package com.iams.manage.service.iot.impl;

// import com.ruoyi.system.domain.IotCameraDeviceInfo;
// import com.ruoyi.system.mapper.IotCameraDeviceInfoMapper;
// import com.ruoyi.system.service.IIotCameraDeviceInfoService;
import com.iams.manage.domain.iot.IotCameraDeviceInfo;
import com.iams.manage.mapper.iot.IotCameraDeviceInfoMapper;
import com.iams.manage.service.iot.IIotCameraDeviceInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等Service业务层处理
 *
 * @author likang
 * @date 2025-02-25
 */
@Service
public class IotCameraDeviceInfoServiceImpl implements IIotCameraDeviceInfoService
{
    @Autowired
    private IotCameraDeviceInfoMapper iotCameraDeviceInfoMapper;

    /**
     * 查询存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等
     *
     * @param deviceId 存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等主键
     * @return 存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等
     */
    @Override
    public IotCameraDeviceInfo selectIotCameraDeviceInfoByDeviceId(String deviceId)
    {
        return iotCameraDeviceInfoMapper.selectIotCameraDeviceInfoByDeviceId(deviceId);
    }

    /**
     * 查询存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等列表
     *
     * @param iotCameraDeviceInfo 存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等
     * @return 存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等
     */
    @Override
    public List<IotCameraDeviceInfo> selectIotCameraDeviceInfoList(IotCameraDeviceInfo iotCameraDeviceInfo)
    {
        return iotCameraDeviceInfoMapper.selectIotCameraDeviceInfoList(iotCameraDeviceInfo);
    }

    /**
     * 新增存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等
     *
     * @param iotCameraDeviceInfo 存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等
     * @return 结果
     */
    @Override
    public int insertIotCameraDeviceInfo(IotCameraDeviceInfo iotCameraDeviceInfo)
    {
        return iotCameraDeviceInfoMapper.insertIotCameraDeviceInfo(iotCameraDeviceInfo);
    }

    /**
     * 修改存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等
     *
     * @param iotCameraDeviceInfo 存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等
     * @return 结果
     */
    @Override
    public int updateIotCameraDeviceInfo(IotCameraDeviceInfo iotCameraDeviceInfo)
    {
        return iotCameraDeviceInfoMapper.updateIotCameraDeviceInfo(iotCameraDeviceInfo);
    }

    /**
     * 批量删除存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等
     *
     * @param deviceIds 需要删除的存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等主键
     * @return 结果
     */
    @Override
    public int deleteIotCameraDeviceInfoByDeviceIds(String[] deviceIds)
    {
        return iotCameraDeviceInfoMapper.deleteIotCameraDeviceInfoByDeviceIds(deviceIds);
    }

    /**
     * 删除存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等信息
     *
     * @param deviceId 存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等主键
     * @return 结果
     */
    @Override
    public int deleteIotCameraDeviceInfoByDeviceId(String deviceId)
    {
        return iotCameraDeviceInfoMapper.deleteIotCameraDeviceInfoByDeviceId(deviceId);
    }
}
