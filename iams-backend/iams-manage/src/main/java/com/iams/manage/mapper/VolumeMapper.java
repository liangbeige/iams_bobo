package com.iams.manage.mapper;

import java.util.List;
import com.iams.manage.domain.Volume;

/**
 * 组卷管理Mapper接口
 * 
 * @author zhld
 * @date 2025-06-06
 */
public interface VolumeMapper 
{
    /**
     * 查询组卷管理
     * 
     * @param id 组卷管理主键
     * @return 组卷管理
     */
    public Volume selectVolumeById(Long id);

    /**
     * 查询组卷管理列表
     * 
     * @param volume 组卷管理
     * @return 组卷管理集合
     */
    public List<Volume> selectVolumeList(Volume volume);

    /**
     * 新增组卷管理
     * 
     * @param volume 组卷管理
     * @return 结果
     */
    public int insertVolume(Volume volume);

    /**
     * 修改组卷管理
     * 
     * @param volume 组卷管理
     * @return 结果
     */
    public int updateVolume(Volume volume);

    /**
     * 删除组卷管理
     * 
     * @param id 组卷管理主键
     * @return 结果
     */
    public int deleteVolumeById(Long id);

    /**
     * 批量删除组卷管理
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteVolumeByIds(Long[] ids);
}
