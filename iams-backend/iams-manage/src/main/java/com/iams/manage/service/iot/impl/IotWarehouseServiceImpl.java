package com.iams.manage.service.iot.impl;

// import com.ruoyi.system.domain.IotWarehouse;
// import com.ruoyi.system.mapper.IotWarehouseMapper;
import com.iams.manage.domain.iot.IotWarehouse;
import com.iams.manage.mapper.iot.IotWarehouseMapper;
// import com.ruoyi.system.service.IIotWarehouseService;
import com.iams.manage.service.iot.IIotWarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 仓库管理Service业务层处理
 *
 * @author pangzhongzheng
 * @date 2025-02-27
 */
@Service
public class IotWarehouseServiceImpl implements IIotWarehouseService
{
    @Autowired
    private IotWarehouseMapper iotWarehouseMapper;

    /**
     * 查询仓库管理
     *
     * @param warehouseId 仓库管理主键
     * @return 仓库管理
     */
    @Override
    public IotWarehouse selectIotWarehouseByWarehouseId(Long warehouseId)
    {
        return iotWarehouseMapper.selectIotWarehouseByWarehouseId(warehouseId);
    }

    /**
     * 查询仓库管理列表
     *
     * @param iotWarehouse 仓库管理
     * @return 仓库管理
     */
    @Override
    public List<IotWarehouse> selectIotWarehouseList(IotWarehouse iotWarehouse)
    {
        return iotWarehouseMapper.selectIotWarehouseList(iotWarehouse);
    }

    /**
     * 新增仓库管理
     *
     * @param iotWarehouse 仓库管理
     * @return 结果
     */
    @Override
    public int insertIotWarehouse(IotWarehouse iotWarehouse)
    {
        return iotWarehouseMapper.insertIotWarehouse(iotWarehouse);
    }

    /**
     * 修改仓库管理
     *
     * @param iotWarehouse 仓库管理
     * @return 结果
     */
    @Override
    public int updateIotWarehouse(IotWarehouse iotWarehouse)
    {
        return iotWarehouseMapper.updateIotWarehouse(iotWarehouse);
    }

    /**
     * 批量删除仓库管理
     *
     * @param warehouseIds 需要删除的仓库管理主键
     * @return 结果
     */
    @Override
    public int deleteIotWarehouseByWarehouseIds(Long[] warehouseIds)
    {
        return iotWarehouseMapper.deleteIotWarehouseByWarehouseIds(warehouseIds);
    }

    /**
     * 删除仓库管理信息
     *
     * @param warehouseId 仓库管理主键
     * @return 结果
     */
    @Override
    public int deleteIotWarehouseByWarehouseId(Long warehouseId)
    {
        return iotWarehouseMapper.deleteIotWarehouseByWarehouseId(warehouseId);
    }
}
