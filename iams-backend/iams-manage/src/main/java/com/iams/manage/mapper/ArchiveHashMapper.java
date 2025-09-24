package com.iams.manage.mapper;

import com.iams.manage.domain.ArchiveHash;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArchiveHashMapper {
    /**
     * 根据档案ID获取哈希记录
     */
    ArchiveHash selectByArchiveId(@Param("archiveId") Long archiveId);

    /**
     * 保存或更新哈希记录
     */
    void saveOrUpdateHash(ArchiveHash archiveHash);

    int insertArchiveHash(ArchiveHash archiveHash);

    int updateArchiveHash(ArchiveHash archiveHash);

}
