package com.iams.manage.mapper;

import java.util.List;

import com.iams.manage.domain.ArchiveInventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 档案盘点Mapper接口
 *
 * @author system
 * @date 2025-06-23
 */
@Mapper
public interface ArchiveInventoryMapper {

    /**
     * 查询档案盘点列表
     *
     * @param archiveInventory 档案盘点
     * @return 档案盘点集合
     */
    public List<ArchiveInventory> selectArchiveInventoryList(ArchiveInventory archiveInventory);

    /**
     * 根据ID查询档案盘点
     *
     * @param id 档案盘点主键
     * @return 档案盘点
     */
    public ArchiveInventory selectArchiveInventoryById(Long id);

    /**
     * 新增档案盘点
     *
     * @param archiveInventory 档案盘点
     * @return 结果
     */
    public int insertArchiveInventory(ArchiveInventory archiveInventory);

    /**
     * 修改档案盘点
     *
     * @param archiveInventory 档案盘点
     * @return 结果
     */
    public int updateArchiveInventory(ArchiveInventory archiveInventory);

    /**
     * 删除档案盘点
     *
     * @param id 档案盘点主键
     * @return 结果
     */
    public int deleteArchiveInventoryById(Long id);

    /**
     * 批量删除档案盘点
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteArchiveInventoryByIds(Long[] ids);
}