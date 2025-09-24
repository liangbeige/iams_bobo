package com.iams.manage.service;

import java.util.List;
import com.iams.manage.domain.Directory;

/**
 * 目录管理Service接口
 * 
 * @author zhjm
 * @date 2025-01-09
 */
public interface IDirectoryService 
{
    /**
     * 查询目录管理
     * 
     * @param id 目录管理主键
     * @return 目录管理
     */
    public Directory selectDirectoryById(Long id);

    /**
     * 查询目录管理列表
     * 
     * @param directory 目录管理
     * @return 目录管理集合
     */
    public List<Directory> selectDirectoryList(Directory directory);

    /**
     * 新增目录管理
     * 
     * @param directory 目录管理
     * @return 结果
     */
    public int insertDirectory(Directory directory);

    /**
     * 修改目录管理
     * 
     * @param directory 目录管理
     * @return 结果
     */
    public int updateDirectory(Directory directory);

    /**
     * 批量删除目录管理
     * 
     * @param ids 需要删除的目录管理主键集合
     * @return 结果
     */
    public int deleteDirectoryByIds(Long[] ids);

    /**
     * 删除目录管理信息
     * 
     * @param id 目录管理主键
     * @return 结果
     */
    public int deleteDirectoryById(Long id);
}
