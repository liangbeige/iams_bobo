package com.iams.manage.service.iot;// package com.ruoyi.system.service;

import com.iams.manage.domain.iot.IotAirConditioner;

import java.util.List;
// import com.ruoyi.system.domain.IotAirConditioner;

/**
 * 空调设备信息，存储空调的静态属性及实时状态Service接口
 *
 * @author liuziqi
 * @date 2025-02-26
 */
public interface IIotAirConditionerService
{
    /**
     * 查询空调设备信息，存储空调的静态属性及实时状态
     *
     * @param id 空调设备信息，存储空调的静态属性及实时状态主键
     * @return 空调设备信息，存储空调的静态属性及实时状态
     */
    IotAirConditioner selectIotAirConditionerById(String id);

    /**
     * 查询空调设备信息，存储空调的静态属性及实时状态列表
     *
     * @param iotAirConditioner 空调设备信息，存储空调的静态属性及实时状态
     * @return 空调设备信息，存储空调的静态属性及实时状态集合
     */
    List<IotAirConditioner> selectIotAirConditionerList(IotAirConditioner iotAirConditioner);

    /**
     * 新增空调设备信息，存储空调的静态属性及实时状态
     *
     * @param iotAirConditioner 空调设备信息，存储空调的静态属性及实时状态
     * @return 结果
     */
    int insertIotAirConditioner(IotAirConditioner iotAirConditioner);

    /**
     * 修改空调设备信息，存储空调的静态属性及实时状态
     *
     * @param iotAirConditioner 空调设备信息，存储空调的静态属性及实时状态
     * @return 结果
     */
    int updateIotAirConditioner(IotAirConditioner iotAirConditioner);

    /**
     * 批量删除空调设备信息，存储空调的静态属性及实时状态
     *
     * @param ids 需要删除的空调设备信息，存储空调的静态属性及实时状态主键集合
     * @return 结果
     */
    int deleteIotAirConditionerByIds(String[] ids);

    /**
     * 删除空调设备信息，存储空调的静态属性及实时状态信息
     *
     * @param id 空调设备信息，存储空调的静态属性及实时状态主键
     * @return 结果
     */
    int deleteIotAirConditionerById(String id);

    String getTopicById(String id);

    /**
     * 导入空调设备数据
     *
     * @param iotAirConditionerList 空调设备列表
     * @param updateSupport         是否更新支持，如果已存在，则进行更新数据
     * @return 导入结果
     */
    String importIotAirConditioner(List<IotAirConditioner> iotAirConditionerList, Boolean updateSupport);
}
