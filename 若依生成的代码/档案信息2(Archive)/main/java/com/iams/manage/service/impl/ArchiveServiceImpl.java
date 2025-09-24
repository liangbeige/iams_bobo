package com.iams.manage.service.impl;

import java.util.List;
import com.iams.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.iams.manage.mapper.ArchiveMapper;
import com.iams.manage.domain.Archive;
import com.iams.manage.service.IArchiveService;

/**
 * 档案列表Service业务层处理
 * 
 * @author zhjm
 * @date 2025-01-10
 */
@Service
public class ArchiveServiceImpl implements IArchiveService 
{
    @Autowired
    private ArchiveMapper archiveMapper;

    /**
     * 查询档案列表
     * 
     * @param id 档案列表主键
     * @return 档案列表
     */
    @Override
    public Archive selectArchiveById(Long id)
    {
        return archiveMapper.selectArchiveById(id);
    }

    /**
     * 查询档案列表列表
     * 
     * @param archive 档案列表
     * @return 档案列表
     */
    @Override
    public List<Archive> selectArchiveList(Archive archive)
    {
        return archiveMapper.selectArchiveList(archive);
    }

    /**
     * 新增档案列表
     * 
     * @param archive 档案列表
     * @return 结果
     */
    @Override
    public int insertArchive(Archive archive)
    {
        archive.setCreateTime(DateUtils.getNowDate());
        return archiveMapper.insertArchive(archive);
    }

    /**
     * 修改档案列表
     * 
     * @param archive 档案列表
     * @return 结果
     */
    @Override
    public int updateArchive(Archive archive)
    {
        archive.setUpdateTime(DateUtils.getNowDate());
        return archiveMapper.updateArchive(archive);
    }

    /**
     * 批量删除档案列表
     * 
     * @param ids 需要删除的档案列表主键
     * @return 结果
     */
    @Override
    public int deleteArchiveByIds(Long[] ids)
    {
        return archiveMapper.deleteArchiveByIds(ids);
    }

    /**
     * 删除档案列表信息
     * 
     * @param id 档案列表主键
     * @return 结果
     */
    @Override
    public int deleteArchiveById(Long id)
    {
        return archiveMapper.deleteArchiveById(id);
    }
}
