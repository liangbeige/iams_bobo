package com.iams.manage.service.impl;

import java.util.List;
import com.iams.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.iams.manage.mapper.DocumentMapper;
import com.iams.manage.domain.Document;
import com.iams.manage.service.IDocumentService;

/**
 * 文档信息Service业务层处理
 * 
 * @author zhjm
 * @date 2025-01-10
 */
@Service
public class DocumentServiceImpl implements IDocumentService 
{
    @Autowired
    private DocumentMapper documentMapper;

    /**
     * 查询文档信息
     * 
     * @param id 文档信息主键
     * @return 文档信息
     */
    @Override
    public Document selectDocumentById(Long id)
    {
        return documentMapper.selectDocumentById(id);
    }

    /**
     * 查询文档信息列表
     * 
     * @param document 文档信息
     * @return 文档信息
     */
    @Override
    public List<Document> selectDocumentList(Document document)
    {
        return documentMapper.selectDocumentList(document);
    }

    /**
     * 新增文档信息
     * 
     * @param document 文档信息
     * @return 结果
     */
    @Override
    public int insertDocument(Document document)
    {
        document.setCreateTime(DateUtils.getNowDate());
        return documentMapper.insertDocument(document);
    }

    /**
     * 修改文档信息
     * 
     * @param document 文档信息
     * @return 结果
     */
    @Override
    public int updateDocument(Document document)
    {
        document.setUpdateTime(DateUtils.getNowDate());
        return documentMapper.updateDocument(document);
    }

    /**
     * 批量删除文档信息
     * 
     * @param ids 需要删除的文档信息主键
     * @return 结果
     */
    @Override
    public int deleteDocumentByIds(Long[] ids)
    {
        return documentMapper.deleteDocumentByIds(ids);
    }

    /**
     * 删除文档信息信息
     * 
     * @param id 文档信息主键
     * @return 结果
     */
    @Override
    public int deleteDocumentById(Long id)
    {
        return documentMapper.deleteDocumentById(id);
    }
}
