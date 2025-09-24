package com.iams.manage.service.impl;

import java.util.List;
import com.iams.common.utils.DateUtils;
import com.iams.common.utils.SecurityUtils;
import org.apache.catalina.security.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.iams.manage.mapper.CategoryMapper;
import com.iams.manage.domain.Category;
import com.iams.manage.service.ICategoryService;

/**
 * 档案门类管理Service业务层处理
 * 
 * @author zhjm
 * @date 2025-01-05
 */
@Service
public class CategoryServiceImpl implements ICategoryService 
{
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 查询档案门类管理
     * 
     * @param id 档案门类管理主键
     * @return 档案门类管理
     */
    @Override
    public Category selectCategoryById(Long id)
    {
        return categoryMapper.selectCategoryById(id);
    }

    /**
     * 查询档案门类管理列表
     * 
     * @param category 档案门类管理
     * @return 档案门类管理
     */
    @Override
    public List<Category> selectCategoryList(Category category)
    {
        return categoryMapper.selectCategoryList(category);
    }

    /**
     * 新增档案门类管理
     * 
     * @param category 档案门类管理
     * @return 结果
     */
    @Override
    public int insertCategory(Category category)
    {
        category.setCreateTime(DateUtils.getNowDate());
        category.setCreateBy(SecurityUtils.getUsername());
        return categoryMapper.insertCategory(category);
    }

    /**
     * 修改档案门类管理
     * 
     * @param category 档案门类管理
     * @return 结果
     */
    @Override
    public int updateCategory(Category category)
    {
        category.setUpdateTime(DateUtils.getNowDate());
        category.setUpdateBy(SecurityUtils.getUsername());
        return categoryMapper.updateCategory(category);
    }

    /**
     * 批量删除档案门类管理
     * 
     * @param ids 需要删除的档案门类管理主键
     * @return 结果
     */
    @Override
    public int deleteCategoryByIds(Long[] ids)
    {
        return categoryMapper.deleteCategoryByIds(ids);
    }

    /**
     * 删除档案门类管理信息
     * 
     * @param id 档案门类管理主键
     * @return 结果
     */
    @Override
    public int deleteCategoryById(Long id)
    {
        return categoryMapper.deleteCategoryById(id);
    }


    @Override
    public String selectCategoryNameById(Long id)
    {
        return categoryMapper.selectCategoryById(id).getName();
    }


}
