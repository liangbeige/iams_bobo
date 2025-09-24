package com.iams.manage.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iams.manage.domain.ArchiveCategory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 档案分类Mapper接口
 *
 * @author ruoyi
 */
public interface ArchiveCategoryMapper
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

    /**
     * 查询所有根节点
     *
     * @return 根节点集合
     */
    public List<ArchiveCategory> selectCategoryRoots();

    /**
     * 根据rootId查询分类树
     *
     * @param rootId 根节点ID
     * @return 分类集合
     */
    public List<ArchiveCategory> selectCategoryByRootId(Long rootId);

    /**
     * 统计根节点下的子分类数量
     *
     * @param rootId 根节点ID
     * @return 子分类数量
     */
    public int countChildrenByRootId(Long rootId);

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
     * 删除档案分类
     *
     * @param id 档案分类主键
     * @return 结果
     */
    public int deleteCategoryById(Long id);

    /**
     * 校验分类编码是否唯一
     *
     * @param code 分类编码
     * @param rootId 根节点ID
     * @return 分类信息
     */
    public ArchiveCategory checkCategoryCodeUnique(@Param("code") String code, @Param("rootId") Long rootId);

    /**
     * 是否存在子节点
     *
     * @param id 分类ID
     * @return 结果
     */
    public int hasChildById(Long id);

    /**
     * 查询ID是否存在于指定分类及其子分类中
     *
     * @param Long 分类ID列表
     * @return 结果
     */
    public int selectNormalChildrenCategoryById(Long id);

    /**
     * 根据ID查询所有子分类
     *
     * @param id 分类ID
     * @return 分类列表
     */
    public List<ArchiveCategory> selectChildrenCategoryById(Long id);

    /**
     * 修改子元素关系
     *
     * @param categories 子元素
     * @return 结果
     */
    public int updateCategoryChildren(@Param("categories") List<ArchiveCategory> categories);

    public Long selectRootIdByCode(String code);

    public String selectNameByCode(String code);

    public List<String> selectNameListByCategoryCodes(List<String> categoryCodes);


    /**
     * 搜索根分类（支持按code和name搜索任意层级节点，返回对应的根节点）
     *
     * @param archiveCategory 搜索条件
     * @return 根分类列表
     */
    List<ArchiveCategory> searchCategoryRoots(ArchiveCategory archiveCategory);

// 同时修改原有的selectCategoryRoots方法签名，添加参数：
    /**
     * 查询所有根节点
     *
     * @param archiveCategory 查询条件
     * @return 根节点集合
     */
    List<ArchiveCategory> selectCategoryRoots(ArchiveCategory archiveCategory);



    /**
     * 根据代码列表查询门类
     */
    List<ArchiveCategory> selectCategoriesByCodes(@Param("codes") List<String> codes);

    /**
     * 根据代码查询门类
     */
    ArchiveCategory selectCategoryByCode(@Param("code") String code);

    // 新增方法
    ArchiveCategory selectExactRoot(@Param("code") String code, @Param("name") String name);
}