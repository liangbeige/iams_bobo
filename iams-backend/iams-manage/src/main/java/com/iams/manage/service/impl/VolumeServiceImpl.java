package com.iams.manage.service.impl;

import java.util.List;
import com.iams.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.iams.manage.mapper.VolumeMapper;
import com.iams.manage.domain.Volume;
import com.iams.manage.service.IVolumeService;

/**
 * 组卷管理Service业务层处理
 * 
 * @author zhld
 * @date 2025-06-06
 */
@Service
public class VolumeServiceImpl implements IVolumeService 
{
    @Autowired
    private VolumeMapper volumeMapper;

    /**
     * 查询组卷管理
     * 
     * @param id 组卷管理主键
     * @return 组卷管理
     */
    @Override
    public Volume selectVolumeById(Long id)
    {
        return volumeMapper.selectVolumeById(id);
    }

    /**
     * 查询组卷管理列表
     * 
     * @param volume 组卷管理
     * @return 组卷管理
     */
    @Override
    public List<Volume> selectVolumeList(Volume volume)
    {
        return volumeMapper.selectVolumeList(volume);
    }

    /**
     * 新增组卷管理
     * 
     * @param volume 组卷管理
     * @return 结果
     */
    @Override
    public int insertVolume(Volume volume)
    {
        volume.setCreateTime(DateUtils.getNowDate());
        return volumeMapper.insertVolume(volume);
    }

    /**
     * 修改组卷管理
     * 
     * @param volume 组卷管理
     * @return 结果
     */
    @Override
    public int updateVolume(Volume volume)
    {
        volume.setUpdateTime(DateUtils.getNowDate());
        return volumeMapper.updateVolume(volume);
    }

    /**
     * 批量删除组卷管理
     * 
     * @param ids 需要删除的组卷管理主键
     * @return 结果
     */
    @Override
    public int deleteVolumeByIds(Long[] ids)
    {
        return volumeMapper.deleteVolumeByIds(ids);
    }

    /**
     * 删除组卷管理信息
     * 
     * @param id 组卷管理主键
     * @return 结果
     */
    @Override
    public int deleteVolumeById(Long id)
    {
        return volumeMapper.deleteVolumeById(id);
    }
}
