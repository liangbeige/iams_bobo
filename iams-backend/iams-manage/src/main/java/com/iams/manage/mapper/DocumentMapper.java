package com.iams.manage.mapper;

import java.util.List;
import com.iams.manage.domain.Document;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文档信息Mapper接口
 * 
 * @author zhjm
 * @date 2025-01-10
 */
@Mapper
public interface DocumentMapper 
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
     * @param ids 需要查询的数据主键集合
     * @return 文档信息集合
     */
    public List<Document> selectDocumentListByIds(Long[] ids);

    /**
     * 新增文档信息
     * 
     * @param document 文档信息
     * @return 结果
     */
    public int insertDocument(Document document);

    /**
     * 修改文档信息
     * 
     * @param document 文档信息
     * @return 结果
     */
    public int updateDocument(Document document);

    /**
     * 删除文档信息
     * 
     * @param id 文档信息主键
     * @return 结果
     */
    public int deleteDocumentById(Long id);

    /**
     * 批量删除文档信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteDocumentByIds(Long[] ids);

    /**
     * 查询文档最大序号
     *
     * @param id 文档信息主键
     * @return 文档最大序号
     */
    public Long selectMaxXuhaoByArchiveId(Long id);

    /**
     * 更新文档额外信息
     *
     * @param document 文档信息主键
     * @return 结果
     */
    public int updateDocExtraInfo(Document document);

    public List<Document> selectListByArchiveId(Long archiveId);

    public Document selectDocumentByArchiveIdAndName(Long archiveId, String name);

    /**
     * 批量更新文档门类
     *
     * @param docIds 文档ID列表
     * @param categoryCode 门类代码
     * @param categoryName 门类名称
     * @return 结果
     */
    int batchUpdateCategory(List<Long> docIds, String categoryCode, String categoryName);

    // 获取content为null的minioName列表
    List<Document> selectMinioNameList();

    // 获取已经做ocr操作的档案列表
    List<Document> selectOcredDocumentList(Document document);

    List<Document> selectNoOcrDocumentList(Document document);

    List<Document> selectByProjectId(Long projectId);

    int deleteDocumentByArchiveId(Long archiveId);
}
