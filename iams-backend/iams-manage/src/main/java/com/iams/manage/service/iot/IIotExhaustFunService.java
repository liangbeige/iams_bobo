package com.iams.manage.service.iot;// package com.ruoyi.system.service;

import com.iams.manage.domain.iot.IotExhaustFun;

import java.util.List;

// import com.ruoyi.system.domain.IotAirConditioner;
// import com.ruoyi.system.domain.IotExhaustFun;

/**
 * 排风扇设备信息Service接口
 *
 * @author liuziqi
 * @date 2025-02-28
 */
public interface IIotExhaustFunService
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
     * 批量删除排风扇设备信息
     *
     * @param deviceIds 需要删除的排风扇设备信息主键集合
     * @return 结果
     */
    int deleteIotExhaustFunByDeviceIds(String[] deviceIds);

    /**
     * 删除排风扇设备信息信息
     *
     * @param deviceId 排风扇设备信息主键
     * @return 结果
     */
    int deleteIotExhaustFunByDeviceId(String deviceId);

    String getTopicById(String id);

    /**
     * 导入抽风机设备数据
     *
     * @param ExhaustFunList 抽风机设备列表
     * @param updateSupport         是否更新支持，如果已存在，则进行更新数据
     * @return 导入结果
     */
    String importExhaustFun(List<IotExhaustFun> ExhaustFunList, Boolean updateSupport);
}
