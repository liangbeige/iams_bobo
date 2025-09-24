package com.iams.manage.service.iot;// package com.ruoyi.system.service;
//
// import java.util.List;
// import com.ruoyi.system.domain.IotTemperatureThreshold;

import com.iams.manage.domain.iot.IotTemperatureThreshold;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 温度感知Service接口
 *
 * @author pangzhongzheng
 * @date 2025-03-12
 */
@Service
public interface IIotTemperatureThresholdService
{
    /**
     * 查询温度感知
     *
     * @param id 温度感知主键
     * @return 温度感知
     */
    public IotTemperatureThreshold selectIotTemperatureThresholdById(Long id);

    /**
     * 查询温度感知列表
     *
     * @param iotTemperatureThreshold 温度感知
     * @return 温度感知集合
     */
    public List<IotTemperatureThreshold> selectIotTemperatureThresholdList(IotTemperatureThreshold iotTemperatureThreshold);

    /**
     * 新增温度感知
     *
     * @param iotTemperatureThreshold 温度感知
     * @return 结果
     */
    public int insertIotTemperatureThreshold(IotTemperatureThreshold iotTemperatureThreshold);

    /**
     * 修改温度感知
     *
     * @param iotTemperatureThreshold 温度感知
     * @return 结果
     */
    public int updateIotTemperatureThreshold(IotTemperatureThreshold iotTemperatureThreshold);

    /**
     * 批量删除温度感知
     *
     * @param ids 需要删除的温度感知主键集合
     * @return 结果
     */
    public int deleteIotTemperatureThresholdByIds(Long[] ids);

    /**
     * 删除温度感知信息
     *
     * @param id 温度感知主键
     * @return 结果
     */
    public int deleteIotTemperatureThresholdById(Long id);
}
