package com.iams.manage.service;

import java.util.List;

import com.iams.manage.domain.BatchUpdateCategoryDto;
import com.iams.manage.domain.Document;
import com.iams.manage.domain.ExtraInfo;

/**
 * 文档信息Service接口
 * 
 * @author zhjm
 * @date 2025-01-10
 */
public interface IDocumentService 
{
    /**
     * 查询文档信息
     * 
     * @param id 文档信息主键
     * @return 文档信息
     */
    public Document selectDocumentById(Long id);

    /**
     * 查询文档信息列表
     * 
     * @param document 文档信息
     * @return 文档信息集合
     */
    public List<Document> selectDocumentList(Document document);

    /**
     * 查询文档信息列表
     *
     * @param ids 需要查询的文档信息主键
     * @return 文档信息集合
     */
    public List<Document> selectDocumentListByIds(Long[] ids);


    /**
     * 新增文档信息
     * 
     * @param document 文档信息
     * @return 结果
     */
    public Long insertDocument(Document document);

    /**
     * 修改文档信息
     * 
     * @param document 文档信息
     * @return 结果
     */
    public int updateDocument(Document document);

    /**
     * 批量删除文档信息
     * 
     * @param ids 需要删除的文档信息主键集合
     * @return 结果
     */
    public int deleteDocumentByIds(Long[] ids);

    /**
     * 删除文档信息信息
     * 
     * @param id 文档信息主键
     * @return 结果
     */
    public int deleteDocumentById(Long id);

    /**
     * 查询文档序号最大值
     *
     * @param id 需要查询的文档信息主键
     * @return 最大的序号
     */
    public Long selectMaxXuhaoByArchiveId(Long id);

    /**
     * 修改文档额外信息
     *
     * @param extraInfo 文档信息主键
     * @return 结果
     */
    public int updateDocExtraInfo(ExtraInfo extraInfo);

    public List<Document> selectListByArchiveId(Long archiveId);

    public Document selectDocumentByArchiveIdAndName(Long archiveId, String name);

    /**
     * 批量更新文档门类
     *
     * @param dto 批量更新参数
     * @return 结果
     */
    public int batchUpdateDocumentCategory(BatchUpdateCategoryDto dto);

    // 获取content为null的minioName列表
    public List<Document> selectMinioNameList();

    public List<Document> selectOcredDocumentList(Document document);

    public List<Document> selectNoOcrDocumentList(Document document);

    public List<Document> selectByProjectId(Long projectId);
}
