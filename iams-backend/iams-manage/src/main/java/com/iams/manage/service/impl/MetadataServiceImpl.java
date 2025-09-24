package com.iams.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iams.common.constant.UserConstants;
import com.iams.common.utils.DateUtils;
import com.iams.common.utils.SecurityUtils;
import com.iams.common.utils.StringUtils;
import com.iams.manage.domain.Document;
import com.iams.manage.domain.Metadata;
import com.iams.manage.mapper.DocumentMapper;
import com.iams.manage.mapper.MetadataMapper;
import com.iams.manage.service.IMetadataService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 元数据Service业务层处理
 *
 * @author iams
 * @date 2024-01-01
 */
@Service
public class MetadataServiceImpl implements IMetadataService {

    @Autowired
    private MetadataMapper metadataMapper;

    @Autowired
    private DocumentMapper documentMapper;

    /**
     * 查询元数据
     *
     * @param id 元数据主键
     * @return 元数据
     */
    @Override
    public Metadata selectMetadataById(Integer id) {
        return metadataMapper.selectMetadataById(id);
    }

    @Override
    public List<Metadata> selectMetadataByIds(List<Integer> metadataIds) {
        return metadataMapper.selectMetadataByIds(metadataIds);
    }

    /**
     * 查询元数据列表
     *
     * @param metadata 元数据
     * @return 元数据
     */
    @Override
    public List<Metadata> selectMetadataList(Metadata metadata) {
        return metadataMapper.selectMetadataList(metadata);
    }

    /**
     * 查询所有元数据列表（不分页）
     *
     * @return 元数据集合
     */
//    @Override
//    public List<Metadata> selectMetadataListAll() {
//        return metadataMapper.selectMetadataList();
//    }

    /**
     * 新增元数据
     *
     * @param metadata 元数据
     * @return 结果
     */
    @Override
    public int insertMetadata(Metadata metadata) {
        return metadataMapper.insertMetadata(metadata);
    }

    /**
     * 修改元数据
     *
     * @param metadata 元数据
     * @return 结果
     */
    @Override
    public int updateMetadata(Metadata metadata) {
        return metadataMapper.updateMetadata(metadata);
    }

    /**
     * 批量删除元数据
     *
     * @param ids 需要删除的元数据主键
     * @return 结果
     */
    @Override
    public int deleteMetadataByIds(Integer[] ids) {
        int i = 0;
        for (Integer id : ids)
            i += metadataMapper.deleteMetadataById(id);
        return i;
    }

    /**
     * 删除元数据信息
     *
     * @param id 元数据主键
     * @return 结果
     */
    @Override
    public int deleteMetadataById(Integer id) {
        return metadataMapper.deleteMetadataById(id);
    }

    /**
     * 复制元数据
     *
     * @param id 元数据主键
     * @return 结果
     */
    @Override
    @Transactional
    public int copyMetadata(Integer id) {
        // 查询原元数据
        Metadata originalMetadata = metadataMapper.selectMetadataById(id);
        if (originalMetadata == null) {
            return 0;
        }

        // 创建新的元数据对象
        Metadata newMetadata = new Metadata();
        BeanUtils.copyProperties(originalMetadata, newMetadata);

        // 清空ID，设置为新增
        newMetadata.setId(null);

        // 修改名称以区分复制品----------不用区分
        String newName = originalMetadata.getName();
        String newCode = originalMetadata.getCode();

        // 确保编码唯一性
//        int copyCount = 1;
//        while (isCodeExists(newCode)) {
//            newCode = originalMetadata.getCode() + "_copy" + copyCount;
//            copyCount++;
//        }

        newMetadata.setName(newName);
        newMetadata.setCode(newCode);

        return metadataMapper.insertMetadata(newMetadata);
    }

    /**
     * 应用元数据到文档
     *
     * @param metadataIds 元数据ID字符串，多个用逗号分隔
     * @param documentIds 文档ID字符串，多个用逗号分隔
     * @return 结果
     */
    @Override
    @Transactional
    public int applyMetadataToDocuments(String metadataIds, String documentIds) {
        if (StringUtils.isEmpty(metadataIds) || StringUtils.isEmpty(documentIds)) {
            return 0;
        }

        try {
            // 这里需要调用文档服务来更新文档的metadata_ids字段
            // 假设有一个DocumentService来处理
            // return documentService.updateDocumentMetadataIds(documentIds, metadataIds);

            // 临时实现：直接使用SQL更新（实际项目中应该通过文档服务）
            String[] docIds = documentIds.split(",");
            int updateCount = 0;

            for (String docId : docIds) {
                if (StringUtils.isNotEmpty(docId.trim())) {
                    // 这里应该调用文档表的更新操作
                    Document document = documentMapper.selectDocumentById(Long.parseLong(docId));
                    document.setMetadataIds(metadataIds);
                    documentMapper.updateDocument(document);
                    updateCount++;
                }
            }

            return updateCount;
        } catch (Exception e) {
            throw new RuntimeException("应用元数据到文档失败：" + e.getMessage());
        }
    }

//    /**
//     * 校验元数据编码是否唯一
//     *
//     * @param metadata 元数据信息
//     * @return 结果
//     */
//    @Override
//    public String checkMetadataCodeUnique(Metadata metadata) {
//        Integer metadataId = StringUtils.isNull(metadata.getId()) ? -1 : metadata.getId();
//        LambdaQueryWrapper<Metadata> queryWrapper = Wrappers.lambdaQueryWrapper();
//        queryWrapper.eq(Metadata::getCode, metadata.getCode());
//        Metadata info = metadataMapper.selectOne(queryWrapper);
//        if (StringUtils.isNotNull(info) && info.getId().intValue() != metadataId.intValue()) {
//            return UserConstants.NOT_UNIQUE;
//        }
//        return UserConstants.UNIQUE;
//    }
//
//    /**
//     * 检查编码是否存在
//     *
//     * @param code 编码
//     * @return 是否存在
//     */
//    private boolean isCodeExists(String code) {
//        LambdaQueryWrapper<Metadata> queryWrapper = Wrappers.lambdaQueryWrapper();
//        queryWrapper.eq(Metadata::getCode, code);
//        return metadataMapper.selectCount(queryWrapper) > 0;
//    }
}
