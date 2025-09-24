package com.iams.manage.service.impl;

import com.iams.manage.domain.Archive;
import com.iams.manage.domain.ArchiveBrowseLog;
import com.iams.manage.mapper.ArchiveBrowseLogMapper;
import com.iams.manage.mapper.ArchiveMapper;
import com.iams.manage.service.IArchiveBrowseLogService;
import com.iams.manage.service.IArchiveLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static com.iams.framework.datasource.DynamicDataSourceContextHolder.log;

@Service
public class ArchiveBrowseLogServiceImpl implements IArchiveBrowseLogService {
    @Autowired
    private ArchiveBrowseLogMapper archiveBrowseLogMapper;
    @Autowired
    private ArchiveMapper archiveMapper; // 需要注入 ArchiveMapper

    @Override
    @Transactional
    public int insertArchiveBrowseLog(ArchiveBrowseLog browseLog) {
        // 如果 archive_id 不为空，查询档案信息
        if (browseLog.getArchiveId() != null) {
            try {
                // 查询档案信息
                Archive archive = archiveMapper.selectArchiveById(browseLog.getArchiveId());
                if (archive != null) {
                    // 设置档案档号和名称
                    browseLog.setArchiveDanghao(archive.getDanghao());
                    browseLog.setArchiveName(archive.getName());
                }
            } catch (Exception e) {
                // 记录错误但不中断流程
                log.error("查询档案信息失败: {}", e.getMessage());
            }
        }

        // 插入浏览日志
        return archiveBrowseLogMapper.insertArchiveBrowseLog(browseLog);
    }

    @Override
    public int updateArchiveBrowseLog(ArchiveBrowseLog browseLog) {
        return archiveBrowseLogMapper.updateArchiveBrowseLog(browseLog);
    }

    @Override
    public ArchiveBrowseLog selectArchiveBrowseLogById(Long id) {
        return archiveBrowseLogMapper.selectArchiveBrowseLogById(id);
    }

    @Override
    public List<ArchiveBrowseLog> selectBrowseLogByDocumentId(Long documentId) {
        return archiveBrowseLogMapper.selectBrowseLogByDocumentId(documentId);
    }

    @Override
    public List<ArchiveBrowseLog> selectBrowseLogByArchiveId(Long archiveId) {
        return archiveBrowseLogMapper.selectBrowseLogByArchiveId(archiveId);
    }

    @Override
    public List<ArchiveBrowseLog> selectBrowseLogByViewerId(Long viewerId) {
        return archiveBrowseLogMapper.selectBrowseLogByViewerId(viewerId);
    }

    @Override
    public int deleteArchiveBrowseLogById(Long id) {
        return archiveBrowseLogMapper.deleteArchiveBrowseLogById(id);
    }

    @Override
    public int deleteArchiveBrowseLogByIds(Long[] ids) {
        return archiveBrowseLogMapper.deleteArchiveBrowseLogByIds(ids);
    }
}
