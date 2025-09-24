package com.iams.manage.service;

import com.iams.common.core.domain.TreeSelect;
import com.iams.manage.domain.ArchiveCategory;

import java.util.List;
import java.util.Map;

/**
 * 档案分类Service接口
 *
 * @author ruoyi
 */
public interface IArchiveCategoryService
{
    /**
     * 查询档案分类
     *
     * @param id 档案分类主键
     * @return 档案分类
     */
    public ArchiveCategory selectCategoryById(Long id);

    /**
     * 查询档案分类列表
     *
     * @param archiveCategory 档案分类
     * @return 档案分类集合
     */
    public List<ArchiveCategory> selectCategoryList(ArchiveCategory archiveCategory);

//    /**
//     * 查询所有根节点
//     *
//     * @return 根节点集合
//     */
//    public List<ArchiveCategory> selectCategoryRoots();

    /**
     * 根据rootId查询整个分类树
     *
     * @param rootId 根节点ID
     * @return 分类树
     */
    public ArchiveCategory selectCategoryTreeByRootId(Long rootId);

    /**
     * 新增档案分类
     *
     * @param archiveCategory 档案分类
     * @return 结果
     */
    public int insertCategory(ArchiveCategory archiveCategory);

    /**
     * 修改档案分类
     *
     * @param archiveCategory 档案分类
     * @return 结果
     */
    public int updateCategory(ArchiveCategory archiveCategory);

    /**
     * 删除档案分类信息
     *
     * @param id 档案分类主键
     * @return 结果
     */
    public int deleteCategory(Long id);

    /**
     * 校验分类编码是否唯一
     *
     * @param archiveCategory 档案分类
     * @return 结果
     */
    public boolean checkCategoryCodeUnique(ArchiveCategory archiveCategory);

    /**
     * 是否存在子节点
     *
     * @param id 分类ID
     * @return 结果
     */
    public boolean hasChildById(Long id);

    /**
     * 构建前端所需要下拉树结构
     *
     * @param categories 分类列表
     * @return 下拉树结构列表
     */
    public List<TreeSelect> buildCategoryTreeSelect(List<ArchiveCategory> categories);

    /**
     * 构建前端所需要树结构
     *
     * @param categories 分类列表
     * @return 树结构列表
     */
    public List<ArchiveCategory> buildCategoryTree(List<ArchiveCategory> categories);


    // 新增方法：根据 code 和 name 获取根节点ID
    Long getCategoryRootIdByCodeAndName(String code, String name);

    // 新增方法：根据唯一标识符获取门类树
//    ArchiveCategory getCategoryTreeByUniqueKey(String uniqueKey);

    /**
     * 批量保存档案分类树（新建模式）
     *
     * @param categories 分类列表
     * @return 结果
     */
    int saveCategoryTree(List<ArchiveCategory> categories);

    /**
     * 更新档案分类树（编辑模式）
     *
     * @param rootId 根节点ID
     * @param categories 分类列表
     * @return 结果
     */
    int updateCategoryTree(Long rootId, List<ArchiveCategory> categories);

    public Long getCategoryRootIdByCode(String code);
    public String getCategoryNameByCode(String code);

    public List<Map<String, String>> getCategoryNameListByArchiveId(Long archiveId);


//    /**
//     * 查询所有根节点（支持搜索）
//     *
//     * @param archiveCategory 搜索条件
//     * @return 根节点集合
//     */
//    List<ArchiveCategory> selectCategoryRoots(ArchiveCategory archiveCategory);

// 如果需要保持向后兼容，可以添加重载方法：
    /**
     * 查询所有根节点（无搜索条件）
     *
     * @return 根节点集合
     */
    List<ArchiveCategory> selectCategoryRoots(ArchiveCategory archiveCategory);


    /**
     * 根据代码列表查询门类
     */
    List<ArchiveCategory> selectCategoriesByCodes(List<String> codes);

    /**
     * 根据代码查询门类
     */
    ArchiveCategory selectCategoryByCode(String code);

    /**
     * 根据代码查询门类-携带根节点
     */
    public ArchiveCategory selectCategoryByCodeWithRoot(String code, String rootName, String categoryId);

    /**
     * 复制树
     */
    public Long copyCategoryTree(Long sourceRootId);
}
