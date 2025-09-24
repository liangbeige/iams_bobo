package com.iams.manage.mapper;

import java.util.List;
import com.iams.manage.domain.Category;

/**
 * 档案门类管理Mapper接口
 * 
 * @author zhjm
 * @date 2025-01-05
 */
public interface CategoryMapper 
{
    /**
     * 查询档案门类管理
     * 
     * @param id 档案门类管理主键
     * @return 档案门类管理
     */
    public Category selectCategoryById(Long id);

    /**
     * 查询档案门类管理列表
     * 
     * @param category 档案门类管理
     * @return 档案门类管理集合
     */
    public List<Category> selectCategoryList(Category category);

    /**
     * 新增档案门类管理
     * 
     * @param category 档案门类管理
     * @return 结果
     */
    public int insertCategory(Category category);

    /**
     * 修改档案门类管理
     * 
     * @param category 档案门类管理
     * @return 结果
     */
    public int updateCategory(Category category);

    /**
     * 删除档案门类管理
     * 
     * @param id 档案门类管理主键
     * @return 结果
     */
    public int deleteCategoryById(Long id);

    /**
     * 批量删除档案门类管理
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCategoryByIds(Long[] ids);

    List<Category> searchRootCategories(String name, String code);
}
