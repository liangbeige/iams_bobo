package com.iams.manage.mapper;

import java.util.List;

import com.iams.manage.domain.ArchiveInventoryDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 档案盘点明细Mapper接口
 *
 * @author system
 * @date 2025-06-23
 */
@Mapper
public interface ArchiveInventoryDetailMapper {

    /**
     * 查询档案盘点明细列表
     *
     * @param archiveInventoryDetail 档案盘点明细
     * @return 档案盘点明细集合
     */
    public List<ArchiveInventoryDetail> selectArchiveInventoryDetailList(ArchiveInventoryDetail archiveInventoryDetail);

    /**
     * 根据ID查询档案盘点明细
     *
     * @param id 档案盘点明细主键
     * @return 档案盘点明细
     */
    public ArchiveInventoryDetail selectArchiveInventoryDetailById(Long id);

    /**
     * 根据盘点ID查询明细列表
     *
     * @param inventoryId 盘点ID
     * @return 档案盘点明细集合
     */
    public List<ArchiveInventoryDetail> selectDetailsByInventoryId(Long inventoryId);

    /**
     * 根据盘点ID和RFID查询明细
     *
     * @param inventoryId 盘点ID
     * @param rfid RFID标签
     * @return 档案盘点明细
     */
    public ArchiveInventoryDetail selectDetailByInventoryIdAndRfid(@Param("inventoryId") Long inventoryId, @Param("rfid") String rfid);

    /**
     * 批量插入盘点明细
     *
     * @param details 盘点明细列表
     * @return 结果
     */
    public int batchInsertDetails(List<ArchiveInventoryDetail> details);

    /**
     * 新增档案盘点明细
     *
     * @param archiveInventoryDetail 档案盘点明细
     * @return 结果
     */
    public int insertArchiveInventoryDetail(ArchiveInventoryDetail archiveInventoryDetail);

    /**
     * 修改档案盘点明细
     *
     * @param archiveInventoryDetail 档案盘点明细
     * @return 结果
     */
    public int updateArchiveInventoryDetail(ArchiveInventoryDetail archiveInventoryDetail);

    /**
     * 删除档案盘点明细
     *
     * @param id 档案盘点明细主键
     * @return 结果
     */
    public int deleteArchiveInventoryDetailById(Long id);

    /**
     * 批量删除档案盘点明细
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteArchiveInventoryDetailByIds(Long[] ids);

    /**
     * 根据盘点ID删除明细
     *
     * @param inventoryId 盘点ID
     * @return 结果
     */
    public int deleteDetailsByInventoryId(Long inventoryId);

    /**
     * 统计盘点ID下的明细总数量
     *
     * @param inventoryId 盘点ID
     * @return 明细数量
     */
    public int countDetailsByInventoryId(Long inventoryId);

    /**
     * 统计盘点ID下的手动明细数量
     *
     * @param inventoryId 盘点ID
     * @return 手动明细数量
     */
    public int countManualDetailsByInventoryId(Long inventoryId);

    /**
     * 统计盘点ID下的自动明细数量
     *
     * @param inventoryId 盘点ID
     * @return 自动明细数量
     */
    public int countAutoDetailsByInventoryId(Long inventoryId);

    /**
     * 统计盘点ID下的不同位置数量
     *
     * @param inventoryId 盘点ID
     * @return 位置数量
     */
    public int countLocationsByInventoryId(Long inventoryId);
}