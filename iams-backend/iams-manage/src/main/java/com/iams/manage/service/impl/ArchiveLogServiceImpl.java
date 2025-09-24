package com.iams.manage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.iams.manage.mapper.ArchiveLogMapper;
import com.iams.manage.domain.ArchiveLog;
import com.iams.manage.service.IArchiveLogService;

/**
 * 档案日志Service业务层处理
 * 
 * @author LiuTao
 * @date 2025-04-04
 */
@Service
public class ArchiveLogServiceImpl implements IArchiveLogService 
{
    @Autowired
    private ArchiveLogMapper archiveLogMapper;

    /**
     * 查询档案日志
     * 
     * @param id 档案日志主键
     * @return 档案日志
     */
    @Override
    public ArchiveLog selectArchiveLogById(Long id)
    {
        return archiveLogMapper.selectArchiveLogById(id);
    }

    /**
     * 查询档案日志列表
     * 
     * @param archiveLog 档案日志
     * @return 档案日志
     */
    @Override
    public List<ArchiveLog> selectArchiveLogList(ArchiveLog archiveLog)
    {
        return archiveLogMapper.selectArchiveLogList(archiveLog);
    }

    /**
     * 新增档案日志
     * 
     * @param archiveLog 档案日志
     * @return 结果
     */
    @Override
    public int insertArchiveLog(ArchiveLog archiveLog)
    {
        return archiveLogMapper.insertArchiveLog(archiveLog);
    }

    /**
     * 修改档案日志
     * 
     * @param archiveLog 档案日志
     * @return 结果
     */
    @Override
    public int updateArchiveLog(ArchiveLog archiveLog)
    {
        return archiveLogMapper.updateArchiveLog(archiveLog);
    }

    /**
     * 批量删除档案日志
     * 
     * @param ids 需要删除的档案日志主键
     * @return 结果
     */
    @Override
    public int deleteArchiveLogByIds(Long[] ids)
    {
        return archiveLogMapper.deleteArchiveLogByIds(ids);
    }

    /**
     * 删除档案日志信息
     * 
     * @param id 档案日志主键
     * @return 结果
     */
    @Override
    public int deleteArchiveLogById(Long id)
    {
        return archiveLogMapper.deleteArchiveLogById(id);
    }

    public List<ArchiveLog> selectOpLogs(Long archiveId)
    {
        return archiveLogMapper.selectOpLogs(archiveId);
    }

    public List<ArchiveLog> selectBorrowLogs(Long archiveId)
    {
        return archiveLogMapper.selectBorrowLogs(archiveId);
    }
}
