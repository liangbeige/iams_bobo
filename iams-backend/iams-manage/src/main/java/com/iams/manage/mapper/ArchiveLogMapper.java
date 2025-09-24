package com.iams.manage.mapper;

import java.util.List;
import com.iams.manage.domain.ArchiveLog;

/**
 * 档案日志Mapper接口
 * 
 * @author LiuTao
 * @date 2025-04-04
 */
public interface ArchiveLogMapper 
{
    /**
     * 查询档案日志
     * 
     * @param id 档案日志主键
     * @return 档案日志
     */
    public ArchiveLog selectArchiveLogById(Long id);

    /**
     * 查询档案日志列表
     * 
     * @param archiveLog 档案日志
     * @return 档案日志集合
     */
    public List<ArchiveLog> selectArchiveLogList(ArchiveLog archiveLog);

    /**
     * 新增档案日志
     * 
     * @param archiveLog 档案日志
     * @return 结果
     */
    public int insertArchiveLog(ArchiveLog archiveLog);

    /**
     * 修改档案日志
     * 
     * @param archiveLog 档案日志
     * @return 结果
     */
    public int updateArchiveLog(ArchiveLog archiveLog);

    /**
     * 删除档案日志
     * 
     * @param id 档案日志主键
     * @return 结果
     */
    public int deleteArchiveLogById(Long id);

    /**
     * 批量删除档案日志
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteArchiveLogByIds(Long[] ids);

    public List<ArchiveLog> selectOpLogs(Long archiveId);

    public List<ArchiveLog> selectBorrowLogs(Long archiveId);
}
