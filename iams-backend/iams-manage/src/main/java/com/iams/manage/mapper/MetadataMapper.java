package com.iams.manage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iams.manage.domain.Metadata;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 元数据Mapper接口
 *
 * @author iams
 * @date 2024-01-01
 */
@Mapper
public interface MetadataMapper extends BaseMapper<Metadata> {

    /**
     * 查询元数据列表
     *
     * @param metadata 元数据
     * @return 元数据集合
     */
    public List<Metadata> selectMetadataList(Metadata metadata);

    /**
     * 批量更新文档的元数据ID
     *
     * @param documentIds 文档ID列表
     * @param metadataIds 元数据ID字符串
     * @return 更新数量
     */
    @Update("<script>" +
            "UPDATE tb_document SET metadata_ids = #{metadataIds} " +
            "WHERE id IN " +
            "<foreach collection='documentIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int updateDocumentMetadataIds(@Param("documentIds") List<String> documentIds,
                                  @Param("metadataIds") String metadataIds);

    /**
     * 根据元数据编码查询
     *
     * @param code 元数据编码
     * @return 元数据
     */
    Metadata selectMetadataByCode(@Param("code") String code);

    /**
     * 批量查询元数据
     *
     * @param ids 元数据ID列表
     * @return 元数据列表
     */
    List<Metadata> selectMetadataByIds(@Param("ids") List<Integer> ids);

    Metadata selectMetadataById(Integer id);

    int insertMetadata(Metadata metadata);

    int updateMetadata(Metadata metadata);

    int deleteMetadataBatchIds(List<Integer> list);

    int deleteMetadataById(Integer id);
}
