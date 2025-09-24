package com.iams.manage.mapper;

import java.util.List;
import com.iams.manage.domain.Archive;

/**
 * 档案列表Mapper接口
 * 
 * @author zhjm
 * @date 2025-01-10
 */
public interface ArchiveMapper 
{
    /**
     * 查询档案列表
     * 
     * @param id 档案列表主键
     * @return 档案列表
     */
    public Archive selectArchiveById(Long id);

    /**
     * 查询档案列表列表
     * 
     * @param archive 档案列表
     * @return 档案列表集合
     */
    public List<Archive> selectArchiveList(Archive archive);

    /**
     * 新增档案列表
     * 
     * @param archive 档案列表
     * @return 结果
     */
    public int insertArchive(Archive archive);

    /**
     * 修改档案列表
     * 
     * @param archive 档案列表
     * @return 结果
     */
    public int updateArchive(Archive archive);

    /**
     * 删除档案列表
     * 
     * @param id 档案列表主键
     * @return 结果
     */
    public int deleteArchiveById(Long id);

    /**
     * 批量删除档案列表
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteArchiveByIds(Long[] ids);
}
