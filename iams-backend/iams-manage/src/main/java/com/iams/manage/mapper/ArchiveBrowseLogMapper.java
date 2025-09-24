package com.iams.manage.mapper;

import com.iams.manage.domain.ArchiveBrowseLog;
import java.util.List;

public interface ArchiveBrowseLogMapper {
    /**
     * 新增档案浏览日志
     */
    int insertArchiveBrowseLog(ArchiveBrowseLog browseLog);

    /**
     * 更新档案浏览日志
     */
    int updateArchiveBrowseLog(ArchiveBrowseLog browseLog);

    /**
     * 根据ID查询档案浏览日志
     */
    ArchiveBrowseLog selectArchiveBrowseLogById(Long id);

    /**
     * 根据文档ID查询浏览日志列表
     */
    List<ArchiveBrowseLog> selectBrowseLogByDocumentId(Long documentId);

    /**
     * 根据档案ID查询浏览日志列表
     */
    List<ArchiveBrowseLog> selectBrowseLogByArchiveId(Long archiveId);

    /**
     * 根据查看人ID查询浏览日志列表
     */
    List<ArchiveBrowseLog> selectBrowseLogByViewerId(Long viewerId);

    /**
     * 删除档案浏览日志
     */
    int deleteArchiveBrowseLogById(Long id);

    /**
     * 批量删除档案浏览日志
     */
    int deleteArchiveBrowseLogByIds(Long[] ids);
}
