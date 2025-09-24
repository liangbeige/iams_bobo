package com.iams.manage.mapper.iot;



import com.iams.manage.domain.iot.IotCameraDeviceInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等Mapper接口
 *
 * @author likang
 * @date 2025-02-25
 */
@Mapper
public interface IotCameraDeviceInfoMapper
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
     * 删除存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等
     *
     * @param deviceId 存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等主键
     * @return 结果
     */
    public int deleteIotCameraDeviceInfoByDeviceId(String deviceId);

    /**
     * 批量删除存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等
     *
     * @param deviceIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteIotCameraDeviceInfoByDeviceIds(String[] deviceIds);
}
