package com.iams.manage.mapper;

import java.util.List;
import com.iams.manage.domain.Document;

/**
 * 文档信息Mapper接口
 * 
 * @author zhjm
 * @date 2025-01-10
 */
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
}
