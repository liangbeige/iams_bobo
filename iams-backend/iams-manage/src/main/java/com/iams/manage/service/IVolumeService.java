package com.iams.manage.service;

import java.util.List;
import com.iams.manage.domain.Volume;

/**
 * 组卷管理Service接口
 * 
 * @author zhld
 * @date 2025-06-06
 */
public interface IVolumeService 
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
     * 批量删除组卷管理
     * 
     * @param ids 需要删除的组卷管理主键集合
     * @return 结果
     */
    public int deleteVolumeByIds(Long[] ids);

    /**
     * 删除组卷管理信息
     * 
     * @param id 组卷管理主键
     * @return 结果
     */
    public int deleteVolumeById(Long id);
}
