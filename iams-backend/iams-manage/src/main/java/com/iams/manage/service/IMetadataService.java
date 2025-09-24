package com.iams.manage.service;

import com.iams.manage.domain.Metadata;

import java.util.List;

/**
 * 元数据Service接口
 *
 * @author iams
 * @date 2024-01-01
 */
public interface IMetadataService {

    /**
     * 查询元数据
     *
     * @param id 元数据主键
     * @return 元数据
     */
    public Metadata selectMetadataById(Integer id);

    /**
     * 查询元数据列表
     *
     * @param metadata 元数据
     * @return 元数据集合
     */
    public List<Metadata> selectMetadataList(Metadata metadata);

    /**
     * 查询所有元数据列表（不分页）
     *
     * @return 元数据集合
     */
//    public List<Metadata> selectMetadataListAll();

    /**
     * 新增元数据
     *
     * @param metadata 元数据
     * @return 结果
     */
    public int insertMetadata(Metadata metadata);

    /**
     * 修改元数据
     *
     * @param metadata 元数据
     * @return 结果
     */
    public int updateMetadata(Metadata metadata);

    /**
     * 批量删除元数据
     *
     * @param ids 需要删除的元数据主键集合
     * @return 结果
     */
    public int deleteMetadataByIds(Integer[] ids);

    /**
     * 删除元数据信息
     *
     * @param id 元数据主键
     * @return 结果
     */
    public int deleteMetadataById(Integer id);

    /**
     * 复制元数据
     *
     * @param id 元数据主键
     * @return 结果
     */
    public int copyMetadata(Integer id);

    /**
     * 应用元数据到文档
     *
     * @param metadataIds 元数据ID字符串，多个用逗号分隔
     * @param documentIds 文档ID字符串，多个用逗号分隔
     * @return 结果
     */
    public int applyMetadataToDocuments(String metadataIds, String documentIds);

    List<Metadata> selectMetadataByIds(List<Integer> metadataIds);

    /**
     * 校验元数据编码是否唯一
     *
     * @param metadata 元数据信息
     * @return 结果
     */
//    public String checkMetadataCodeUnique(Metadata metadata);
}