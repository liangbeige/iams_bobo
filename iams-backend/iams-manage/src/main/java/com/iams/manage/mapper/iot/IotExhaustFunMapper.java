package com.iams.manage.mapper.iot;

import com.iams.manage.domain.iot.IotExhaustFun;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 排风扇设备信息Mapper接口
 *
 * @author liuziqi
 * @date 2025-02-28
 */
@Mapper
public interface IotExhaustFunMapper
{
    /**
     * 查询排风扇设备信息
     *
     * @param deviceId 排风扇设备信息主键
     * @return 排风扇设备信息
     */
    IotExhaustFun selectIotExhaustFunByDeviceId(String deviceId);

    /**
     * 查询排风扇设备信息列表
     *
     * @param iotExhaustFun 排风扇设备信息
     * @return 排风扇设备信息集合
     */
    List<IotExhaustFun> selectIotExhaustFunList(IotExhaustFun iotExhaustFun);

    /**
     * 新增排风扇设备信息
     *
     * @param iotExhaustFun 排风扇设备信息
     * @return 结果
     */
    int insertIotExhaustFun(IotExhaustFun iotExhaustFun);

    /**
     * 修改排风扇设备信息
     *
     * @param iotExhaustFun 排风扇设备信息
     * @return 结果
     */
    int updateIotExhaustFun(IotExhaustFun iotExhaustFun);

    /**
     * 删除排风扇设备信息
     *
     * @param deviceId 排风扇设备信息主键
     * @return 结果
     */
    int deleteIotExhaustFunByDeviceId(String deviceId);

    /**
     * 批量删除排风扇设备信息
     *
     * @param deviceIds 需要删除的数据主键集合
     * @return 结果
     */
    int deleteIotExhaustFunByDeviceIds(String[] deviceIds);

    String getTopicById(String id);

    IotExhaustFun selectLastIotExhaustFun();
}
