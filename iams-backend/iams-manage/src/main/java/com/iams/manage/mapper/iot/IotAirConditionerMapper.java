package com.iams.manage.mapper.iot;

import com.iams.manage.domain.iot.IotAirConditioner;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 空调设备信息，存储空调的静态属性及实时状态Mapper接口
 *
 * @author liuziqi
 * @date 2025-02-26
 */
@Mapper
public interface IotAirConditionerMapper
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
     * 删除空调设备信息，存储空调的静态属性及实时状态
     *
     * @param id 空调设备信息，存储空调的静态属性及实时状态主键
     * @return 结果
     */
    int deleteIotAirConditionerById(String id);

    /**
     * 批量删除空调设备信息，存储空调的静态属性及实时状态
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteIotAirConditionerByIds(String[] ids);

    String getTopicById(String id);

    IotAirConditioner selectLastIotAirConditioner();
}
