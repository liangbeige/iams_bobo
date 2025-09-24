package com.iams.manage.service.iot.impl;

// import com.ruoyi.system.domain.IotTemperatureThreshold;
// import com.ruoyi.system.mapper.IotTemperatureThresholdMapper;
import com.iams.manage.domain.iot.IotTemperatureThreshold;
import com.iams.manage.mapper.iot.IotTemperatureThresholdMapper;
import com.iams.manage.service.iot.IIotTemperatureThresholdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 温度感知Service业务层处理
 *
 * @author pangzhongzheng
 * @date 2025-03-12
 */
@Service
public class IotTemperatureThresholdServiceImpl implements IIotTemperatureThresholdService
{
    @Autowired
    private IotTemperatureThresholdMapper iotTemperatureThresholdMapper;

    /**
     * 查询温度感知
     *
     * @param id 温度感知主键
     * @return 温度感知
     */
    @Override
    public IotTemperatureThreshold selectIotTemperatureThresholdById(Long id)
    {
        return iotTemperatureThresholdMapper.selectIotTemperatureThresholdById(id);
    }

    /**
     * 查询温度感知列表
     *
     * @param iotTemperatureThreshold 温度感知
     * @return 温度感知
     */
    @Override
    public List<IotTemperatureThreshold> selectIotTemperatureThresholdList(IotTemperatureThreshold iotTemperatureThreshold)
    {
        return iotTemperatureThresholdMapper.selectIotTemperatureThresholdList(iotTemperatureThreshold);
    }

    /**
     * 新增温度感知
     *
     * @param iotTemperatureThreshold 温度感知
     * @return 结果
     */
    @Override
    public int insertIotTemperatureThreshold(IotTemperatureThreshold iotTemperatureThreshold)
    {
        return iotTemperatureThresholdMapper.insertIotTemperatureThreshold(iotTemperatureThreshold);
    }

    /**
     * 修改温度感知
     *
     * @param iotTemperatureThreshold 温度感知
     * @return 结果
     */
    @Override
    public int updateIotTemperatureThreshold(IotTemperatureThreshold iotTemperatureThreshold)
    {
        return iotTemperatureThresholdMapper.updateIotTemperatureThreshold(iotTemperatureThreshold);
    }

    /**
     * 批量删除温度感知
     *
     * @param ids 需要删除的温度感知主键
     * @return 结果
     */
    @Override
    public int deleteIotTemperatureThresholdByIds(Long[] ids)
    {
        return iotTemperatureThresholdMapper.deleteIotTemperatureThresholdByIds(ids);
    }

    /**
     * 删除温度感知信息
     *
     * @param id 温度感知主键
     * @return 结果
     */
    @Override
    public int deleteIotTemperatureThresholdById(Long id)
    {
        return iotTemperatureThresholdMapper.deleteIotTemperatureThresholdById(id);
    }
}
