package com.iams.manage.service.iot;// package com.ruoyi.system.service;
//
import com.iams.manage.domain.iot.IotWarehouse;

import java.util.List;
// import com.ruoyi.system.domain.IotWarehouse;

/**
 * 仓库管理Service接口
 *
 * @author pangzhongzheng
 * @date 2025-02-27
 */
public interface IIotWarehouseService
{
    /**
     * 查询仓库管理
     *
     * @param warehouseId 仓库管理主键
     * @return 仓库管理
     */
    public IotWarehouse selectIotWarehouseByWarehouseId(Long warehouseId);

    /**
     * 查询仓库管理列表
     *
     * @param iotWarehouse 仓库管理
     * @return 仓库管理集合
     */
    public List<IotWarehouse> selectIotWarehouseList(IotWarehouse iotWarehouse);

    /**
     * 新增仓库管理
     *
     * @param iotWarehouse 仓库管理
     * @return 结果
     */
    public int insertIotWarehouse(IotWarehouse iotWarehouse);

    /**
     * 修改仓库管理
     *
     * @param iotWarehouse 仓库管理
     * @return 结果
     */
    public int updateIotWarehouse(IotWarehouse iotWarehouse);

    /**
     * 批量删除仓库管理
     *
     * @param warehouseIds 需要删除的仓库管理主键集合
     * @return 结果
     */
    public int deleteIotWarehouseByWarehouseIds(Long[] warehouseIds);

    /**
     * 删除仓库管理信息
     *
     * @param warehouseId 仓库管理主键
     * @return 结果
     */
    public int deleteIotWarehouseByWarehouseId(Long warehouseId);
}
