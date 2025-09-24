package com.iams.manage.service.impl;

import java.util.List;
import com.iams.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.iams.manage.mapper.DirectoryMapper;
import com.iams.manage.domain.Directory;
import com.iams.manage.service.IDirectoryService;

/**
 * 目录管理Service业务层处理
 * 
 * @author zhjm
 * @date 2025-01-09
 */
@Service
public class DirectoryServiceImpl implements IDirectoryService 
{
    @Autowired
    private DirectoryMapper directoryMapper;

    /**
     * 查询目录管理
     * 
     * @param id 目录管理主键
     * @return 目录管理
     */
    @Override
    public Directory selectDirectoryById(Long id)
    {
        return directoryMapper.selectDirectoryById(id);
    }

    /**
     * 查询目录管理列表
     * 
     * @param directory 目录管理
     * @return 目录管理
     */
    @Override
    public List<Directory> selectDirectoryList(Directory directory)
    {
        return directoryMapper.selectDirectoryList(directory);
    }

    /**
     * 新增目录管理
     * 
     * @param directory 目录管理
     * @return 结果
     */
    @Override
    public int insertDirectory(Directory directory)
    {
        directory.setCreateTime(DateUtils.getNowDate());
        return directoryMapper.insertDirectory(directory);
    }

    /**
     * 修改目录管理
     * 
     * @param directory 目录管理
     * @return 结果
     */
    @Override
    public int updateDirectory(Directory directory)
    {
        directory.setUpdateTime(DateUtils.getNowDate());
        return directoryMapper.updateDirectory(directory);
    }

    /**
     * 批量删除目录管理
     * 
     * @param ids 需要删除的目录管理主键
     * @return 结果
     */
    @Override
    public int deleteDirectoryByIds(Long[] ids)
    {
        return directoryMapper.deleteDirectoryByIds(ids);
    }

    /**
     * 删除目录管理信息
     * 
     * @param id 目录管理主键
     * @return 结果
     */
    @Override
    public int deleteDirectoryById(Long id)
    {
        return directoryMapper.deleteDirectoryById(id);
    }
}
