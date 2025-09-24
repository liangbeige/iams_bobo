package com.iams.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iams.common.core.domain.TreeSelect;
import com.iams.common.utils.StringUtils;
import com.iams.manage.domain.Archive;
import com.iams.manage.domain.ArchiveCategory;
import com.iams.manage.mapper.ArchiveCategoryMapper;
import com.iams.manage.mapper.ArchiveMapper;
import com.iams.manage.service.IArchiveCategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 档案分类Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class ArchiveCategoryServiceImpl implements IArchiveCategoryService
{
    @Autowired
    private ArchiveCategoryMapper archiveCategoryMapper;

    @Autowired
    private ArchiveMapper archiveMapper;

    /**
     * 查询档案分类
     *
     * @param id 档案分类主键
     * @return 档案分类
     */
    @Override
    public ArchiveCategory selectCategoryById(Long id)
    {
        return archiveCategoryMapper.selectCategoryById(id);
    }

    /**
     * 查询档案分类列表
     *
     * @param archiveCategory 档案分类
     * @return 档案分类
     */
    @Override
    public List<ArchiveCategory> selectCategoryList(ArchiveCategory archiveCategory)
    {
        return archiveCategoryMapper.selectCategoryList(archiveCategory);
    }


    /**
     * 查询所有根节点（无搜索条件）
     *
     * @return 根节点集合
     */
    public List<ArchiveCategory> selectCategoryRoots()
    {
        ArchiveCategory searchCondition = new ArchiveCategory();
        return selectCategoryRoots(searchCondition);
    }

    /**
     * 查询所有根节点
     *
     * @return 根节点集合
     */
//    @Override
//    public List<ArchiveCategory> selectCategoryRoots()
//    {
//        List<ArchiveCategory> roots = archiveCategoryMapper.selectCategoryRoots();
//        // 统计每个根节点的子分类数量
//        for (ArchiveCategory root : roots) {
//            int childCount = archiveCategoryMapper.countChildrenByRootId(root.getId());
//            root.setChildCount(childCount);
//        }
//        return roots;
//    }
    /**
     * 查询所有根节点（支持搜索）
     *
     * @param archiveCategory 搜索条件
     * @return 根节点集合
     */
    @Override
    public List<ArchiveCategory> selectCategoryRoots(ArchiveCategory archiveCategory)
    {
        List<ArchiveCategory> roots;

        // 判断是否有搜索条件
        boolean hasSearchCondition = (StringUtils.isNotEmpty(archiveCategory.getName()) ||
                StringUtils.isNotEmpty(archiveCategory.getCode()));

        if (hasSearchCondition) {
            // 有搜索条件时，使用搜索方法（搜索任意层级节点，返回对应根节点）
            roots = archiveCategoryMapper.searchCategoryRoots(archiveCategory);
        } else {
            // 无搜索条件时，直接查询根节点
            roots = archiveCategoryMapper.selectCategoryRoots(archiveCategory);
        }

        // 统计每个根节点的子分类数量
        for (ArchiveCategory root : roots) {
            int childCount = archiveCategoryMapper.countChildrenByRootId(root.getId());
            root.setChildCount(childCount);
        }
        return roots;
    }

    /**
     * 根据rootId查询整个分类树
     *
     * @param rootId 根节点ID
     * @return 分类树
     */
    @Override
    public ArchiveCategory selectCategoryTreeByRootId(Long rootId)
    {
        List<ArchiveCategory> categories = archiveCategoryMapper.selectCategoryByRootId(rootId);
        List<ArchiveCategory> tree = buildCategoryTree(categories);
        return tree.isEmpty() ? null : tree.get(0);
    }

    /**
     * 新增档案分类
     *
     * @param archiveCategory 档案分类
     * @return 结果
     */
    @Override
    public int insertCategory(ArchiveCategory archiveCategory)
    {
        // 如果是根节点
        if (archiveCategory.getParentId() == null || archiveCategory.getParentId() == 0L) {
            archiveCategory.setParentId(null);
            archiveCategory.setAncestors("");
            int rows = archiveCategoryMapper.insertCategory(archiveCategory);
            // 更新rootId为自身ID
            archiveCategory.setRootId(archiveCategory.getId());
            archiveCategoryMapper.updateCategory(archiveCategory);
            return rows;
        } else {
            // 如果不是根节点，需要设置ancestors和rootId
            ArchiveCategory parent = archiveCategoryMapper.selectCategoryById(archiveCategory.getParentId());
            if (parent != null) {
                archiveCategory.setAncestors(parent.getAncestors() + "," + parent.getId());
                archiveCategory.setRootId(parent.getRootId());
            }
            return archiveCategoryMapper.insertCategory(archiveCategory);
        }
    }

    @Override
    public Long getCategoryRootIdByCodeAndName(String code, String name) {
        // 1. 使用精确查询方法（保留副本名称）
        ArchiveCategory root = archiveCategoryMapper.selectExactRoot(code, name);
        if (root != null) {
            return root.getId();
        }

        // 2. 处理副本情况（保留副本名称）
        List<ArchiveCategory> roots = archiveCategoryMapper.selectCategoryList(new ArchiveCategory());
        for (ArchiveCategory category : roots) {
            if (category.getParentId() == null &&
                    category.getName().equals(name) && // 精确匹配名称
                    category.getCode().equals(code)) {
                return category.getId();
            }
        }

        // 3. 处理名称相似的情况（保留副本名称）
        for (ArchiveCategory category : roots) {
            if (category.getParentId() == null &&
                    category.getCode().equals(code) &&
                    category.getName().contains(name)) { // 包含匹配
                return category.getId();
            }
        }

        // 4. 最后尝试标准化名称匹配（保留副本名称）
        String normalizedName = name
                .replace(" - ", "-")   // 统一横线格式
                .replace("－", "-")    // 统一中文横线
                .replace("﹣", "-")    // 统一其他横线
                .replace(" ", "");     // 移除空格

        for (ArchiveCategory category : roots) {
            String normalizedCategoryName = category.getName()
                    .replace(" - ", "-")
                    .replace("－", "-")
                    .replace("﹣", "-")
                    .replace(" ", "");

            if (category.getParentId() == null &&
                    category.getCode().equals(code) &&
                    normalizedCategoryName.equals(normalizedName)) {
                return category.getId();
            }
        }

        return null;
    }

    /**
     * 修改档案分类
     *
     * @param archiveCategory 档案分类
     * @return 结果
     */
    @Override
    public int updateCategory(ArchiveCategory archiveCategory)
    {
        ArchiveCategory newParentCategory = archiveCategoryMapper.selectCategoryById(archiveCategory.getParentId());
        ArchiveCategory oldCategory = archiveCategoryMapper.selectCategoryById(archiveCategory.getId());
        if (StringUtils.isNotNull(newParentCategory) && StringUtils.isNotNull(oldCategory))
        {
            String newAncestors = newParentCategory.getAncestors() + "," + newParentCategory.getId();
            String oldAncestors = oldCategory.getAncestors();
            archiveCategory.setAncestors(newAncestors);
            updateCategoryChildren(archiveCategory.getId(), newAncestors, oldAncestors);
        }
        int result = archiveCategoryMapper.updateCategory(archiveCategory);
        return result;
    }

    /**
     * 修改子元素关系
     *
     * @param categoryId 被修改的分类ID
     * @param newAncestors 新的父ID集合
     * @param oldAncestors 旧的父ID集合
     */
    public void updateCategoryChildren(Long categoryId, String newAncestors, String oldAncestors)
    {
        List<ArchiveCategory> children = archiveCategoryMapper.selectChildrenCategoryById(categoryId);
        for (ArchiveCategory child : children)
        {
            child.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
        }
        if (children.size() > 0)
        {
            archiveCategoryMapper.updateCategoryChildren(children);
        }
    }

    /**
     * 删除档案分类信息
     *
     * @param id 档案分类主键
     * @return 结果
     */
    @Override
    public int deleteCategory(Long id)
    {
        return archiveCategoryMapper.deleteCategoryById(id);
    }

    /**
     * 校验分类编码是否唯一
     *
     * @param archiveCategory 档案分类
     * @return 结果
     */
    @Override
    public boolean checkCategoryCodeUnique(ArchiveCategory archiveCategory)
    {
        Long categoryId = StringUtils.isNull(archiveCategory.getId()) ? -1L : archiveCategory.getId();
        ArchiveCategory info = archiveCategoryMapper.checkCategoryCodeUnique(archiveCategory.getCode(), archiveCategory.getRootId());
        if (StringUtils.isNotNull(info) && info.getId().longValue() != categoryId.longValue())
        {
            return false;
        }
        return true;
    }

    /**
     * 是否存在子节点
     *
     * @param id 分类ID
     * @return 结果 true 存在 false 不存在
     */
    @Override
    public boolean hasChildById(Long id)
    {
        int result = archiveCategoryMapper.hasChildById(id);
        return result > 0;
    }

    /**
     * 构建前端所需要下拉树结构
     *
     * @param categories 分类列表
     * @return 下拉树结构列表
     */
    @Override
    public List<TreeSelect> buildCategoryTreeSelect(List<ArchiveCategory> categories)
    {
        List<ArchiveCategory> categoryTrees = buildCategoryTree(categories);
        return categoryTrees.stream().map(category -> convertToTreeSelect(category)).collect(Collectors.toList());
    }

    /**
     * 转换为TreeSelect对象
     *
     * @param category 分类对象
     * @return TreeSelect对象
     */
    private TreeSelect convertToTreeSelect(ArchiveCategory category)
    {
        TreeSelect treeSelect = new TreeSelect();
        treeSelect.setId(category.getId());
        treeSelect.setLabel(category.getCode() + " " + category.getName());
        if (StringUtils.isNotEmpty(category.getChildren())) {
            treeSelect.setChildren(category.getChildren().stream()
                    .map(child -> convertToTreeSelect(child))
                    .collect(Collectors.toList()));
        }
        return treeSelect;
    }

    /**
     * 构建前端所需要树结构
     *
     * @param categories 分类列表
     * @return 树结构列表
     */
    @Override
    public List<ArchiveCategory> buildCategoryTree(List<ArchiveCategory> categories)
    {
        List<ArchiveCategory> returnList = new ArrayList<ArchiveCategory>();
        List<Long> tempList = categories.stream().map(ArchiveCategory::getId).collect(Collectors.toList());
        for (ArchiveCategory category : categories)
        {
            // 如果是顶级节点, 遍历该父节点的所有子节点
            if (!tempList.contains(category.getParentId()))
            {
                recursionFn(categories, category);
                returnList.add(category);
            }
        }
        if (returnList.isEmpty())
        {
            returnList = categories;
        }
        return returnList;
    }

    /**
     * 递归列表
     */
    private void recursionFn(List<ArchiveCategory> list, ArchiveCategory t)
    {
        // 得到子节点列表
        List<ArchiveCategory> childList = getChildList(list, t);
        t.setChildren(childList);
        for (ArchiveCategory tChild : childList)
        {
            if (hasChild(list, tChild))
            {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<ArchiveCategory> getChildList(List<ArchiveCategory> list, ArchiveCategory t)
    {
        List<ArchiveCategory> tlist = new ArrayList<ArchiveCategory>();
        Iterator<ArchiveCategory> it = list.iterator();
        while (it.hasNext())
        {
            ArchiveCategory n = (ArchiveCategory) it.next();
            if (StringUtils.isNotNull(n.getParentId()) && n.getParentId().longValue() == t.getId().longValue())
            {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<ArchiveCategory> list, ArchiveCategory t)
    {
        return getChildList(list, t).size() > 0;
    }
    /**
     * 批量保存档案分类树（新建模式）
     *
     * @param categories 分类列表
     * @return 保存的记录数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveCategoryTree(List<ArchiveCategory> categories) {
        if (categories == null || categories.isEmpty()) {
            return 0;
        }

        // 前端临时ID -> 数据库真实ID的映射
        Map<Long, Long> idMapping = new HashMap<>();
        // 前端临时ID -> 分类对象的映射，用于后续查找
        Map<Long, ArchiveCategory> tempIdToCategoryMap = new HashMap<>();

        // 先将所有分类按临时ID存储起来
        for (ArchiveCategory category : categories) {
            if (category.getId() != null) {
                tempIdToCategoryMap.put(category.getId(), category);
            }
        }

        // 按层级排序，确保父节点先于子节点处理
        List<ArchiveCategory> sortedCategories = sortCategoriesByLevel(categories);

        int totalRows = 0;
        Long realRootId = null;

        for (ArchiveCategory category : sortedCategories) {
            Long tempId = category.getId(); // 保存前端的临时ID

            // 处理父子关系
            if (category.getParentId() != null) {
                // 如果parentId在映射表中，说明父节点已经插入，使用真实ID
                Long realParentId = idMapping.get(category.getParentId());
                if (realParentId != null) {
                    category.setParentId(realParentId);

                    // 重新构建ancestors
                    ArchiveCategory parent = archiveCategoryMapper.selectCategoryById(realParentId);
                    if (parent != null) {
                        String parentAncestors = parent.getAncestors();
                        category.setAncestors(StringUtils.isEmpty(parentAncestors) ?
                                String.valueOf(realParentId) :
                                parentAncestors + "," + realParentId);
                        category.setRootId(parent.getRootId());
                    }
                }
            } else {
                // 根节点
                category.setParentId(null);
                category.setAncestors("");
            }

            // 清空ID，让数据库自动生成
            category.setId(null);

            // 插入数据库
            int rows = archiveCategoryMapper.insertCategory(category);
            if (rows > 0) {
                totalRows += rows;
                Long realId = category.getId(); // 获取数据库生成的真实ID

                // 如果是根节点，更新rootId为自身ID
                if (category.getParentId() == null) {
                    realRootId = realId;
                    category.setRootId(realId);
                    archiveCategoryMapper.updateCategory(category);
                } else if (realRootId != null) {
                    // 非根节点使用根节点的ID
                    category.setRootId(realRootId);
                    archiveCategoryMapper.updateCategory(category);
                }

                // 保存ID映射关系
                if (tempId != null) {
                    idMapping.put(tempId, realId);
                }
            }
        }

        return totalRows;
    }

    /**
     * 更新档案分类树（编辑模式）
     *
     * @param rootId 根节点ID
     * @param categories 分类列表
     * @return 处理的记录数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCategoryTree(Long rootId, List<ArchiveCategory> categories) {
        if (categories == null || categories.isEmpty()) {
            return 0;
        }

        // 获取现有的所有分类
        List<ArchiveCategory> existingCategories = archiveCategoryMapper.selectCategoryByRootId(rootId);
        Map<Long, ArchiveCategory> existingMap = existingCategories.stream()
                .collect(Collectors.toMap(ArchiveCategory::getId, c -> c));

        // 前端ID -> 数据库ID的映射
        Map<Long, Long> idMapping = new HashMap<>();
        // 需要新增的分类
        List<ArchiveCategory> toInsert = new ArrayList<>();
        // 需要更新的分类
        List<ArchiveCategory> toUpdate = new ArrayList<>();
        // 需要删除的ID集合
        Set<Long> toDelete = new HashSet<>(existingMap.keySet());

        // 先处理根节点，建立映射
        for (ArchiveCategory category : categories) {
            if (category.getId() != null && category.getId().equals(rootId)) {
                idMapping.put(category.getId(), category.getId());
                toUpdate.add(category);
                toDelete.remove(category.getId());
                break;
            }
        }

        // 按层级排序
        List<ArchiveCategory> sortedCategories = sortCategoriesByLevel(categories);

        for (ArchiveCategory category : sortedCategories) {
            Long frontendId = category.getId();

            // 判断是否是已存在的节点
            if (frontendId != null && existingMap.containsKey(frontendId)) {
                // 更新现有节点
                idMapping.put(frontendId, frontendId);
                toUpdate.add(category);
                toDelete.remove(frontendId);
            } else {
                // 新增节点
                toInsert.add(category);
            }
        }

        int totalRows = 0;

        // 1. 删除不再存在的节点
        for (Long id : toDelete) {
            archiveCategoryMapper.deleteCategoryById(id);
            totalRows++;
        }

        // 2. 更新现有节点
        for (ArchiveCategory category : toUpdate) {
            // 处理父子关系
            if (category.getParentId() != null && idMapping.containsKey(category.getParentId())) {
                category.setParentId(idMapping.get(category.getParentId()));
            }

            // 重新构建ancestors
            if (category.getParentId() != null) {
                ArchiveCategory parent = archiveCategoryMapper.selectCategoryById(category.getParentId());
                if (parent != null) {
                    String parentAncestors = parent.getAncestors();
                    category.setAncestors(StringUtils.isEmpty(parentAncestors) ?
                            String.valueOf(parent.getId()) :
                            parentAncestors + "," + parent.getId());
                }
            } else {
                category.setAncestors("");
            }

            archiveCategoryMapper.updateCategory(category);
            totalRows++;
        }

        // 3. 插入新节点
        for (ArchiveCategory category : toInsert) {
            Long tempId = category.getId();

            // 处理父子关系
            if (category.getParentId() != null && idMapping.containsKey(category.getParentId())) {
                category.setParentId(idMapping.get(category.getParentId()));
            }

            // 重新构建ancestors和rootId
            if (category.getParentId() != null) {
                ArchiveCategory parent = archiveCategoryMapper.selectCategoryById(category.getParentId());
                if (parent != null) {
                    String parentAncestors = parent.getAncestors();
                    category.setAncestors(StringUtils.isEmpty(parentAncestors) ?
                            String.valueOf(parent.getId()) :
                            parentAncestors + "," + parent.getId());
                    category.setRootId(parent.getRootId());
                }
            } else {
                category.setAncestors("");
                category.setRootId(rootId);
            }

            // 清空ID，让数据库生成
            category.setId(null);

            int rows = archiveCategoryMapper.insertCategory(category);
            if (rows > 0) {
                totalRows += rows;
                // 保存映射关系，供后续子节点使用
                if (tempId != null) {
                    idMapping.put(tempId, category.getId());
                }
            }
        }

        return totalRows;
    }

    /**
     * 按层级排序分类，确保父节点在子节点之前
     */
    private List<ArchiveCategory> sortCategoriesByLevel(List<ArchiveCategory> categories) {
        // 构建临时的父子关系映射
        Map<Long, List<ArchiveCategory>> childrenMap = new HashMap<>();
        List<ArchiveCategory> roots = new ArrayList<>();

        for (ArchiveCategory category : categories) {
            if (category.getParentId() == null) {
                roots.add(category);
            } else {
                childrenMap.computeIfAbsent(category.getParentId(), k -> new ArrayList<>()).add(category);
            }
        }

        // 按层级遍历，生成排序后的列表
        List<ArchiveCategory> sorted = new ArrayList<>();
        Queue<ArchiveCategory> queue = new LinkedList<>(roots);

        while (!queue.isEmpty()) {
            ArchiveCategory current = queue.poll();
            sorted.add(current);

            // 添加子节点到队列
            List<ArchiveCategory> children = childrenMap.get(current.getId());
            if (children != null) {
                queue.addAll(children);
            }
        }

        return sorted;
    }


    @Override
    public Long getCategoryRootIdByCode(String code)
    {
         return archiveCategoryMapper.selectRootIdByCode(code);
    }

    @Override
    public String getCategoryNameByCode(String code)
    {
        return archiveCategoryMapper.selectNameByCode(code);
    }

    @Override
    public List<Map<String, String>> getCategoryNameListByArchiveId(Long archiveId)
    {
        // 先获取档案的分类ID
        List<String> categoryCodes = Arrays.asList(archiveMapper.selectArchiveById(archiveId).getCategoryId().split(","));

        List<String> categoryNames = archiveCategoryMapper.selectNameListByCategoryCodes(categoryCodes);

        List<Map<String, String>> categoryList = new ArrayList<>();
        for (int i = 0; i < categoryCodes.size(); i++) {
            Map<String, String> categoryMap = new HashMap<>();
            categoryMap.put("code", categoryCodes.get(i));
            categoryMap.put("name", categoryNames.get(i));
            categoryList.add(categoryMap);
        }

        return categoryList;
    }


    /**
     * 根据代码列表查询门类
     */
    @Override
    public List<ArchiveCategory> selectCategoriesByCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return new ArrayList<>();
        }
        return archiveCategoryMapper.selectCategoriesByCodes(codes);
    }

    /**
     * 根据代码查询门类
     */
    @Override
    public ArchiveCategory selectCategoryByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        return archiveCategoryMapper.selectCategoryByCode(code);
    }

    @Override
    public ArchiveCategory selectCategoryByCodeWithRoot(String code, String rootName, String categoryId){
        ArchiveCategory archiveCategory = selectCategoryRoots().stream()
                .filter(c -> c.getName().equals(rootName) && c.getCode().equals(code))
                .findFirst()
                .orElse(null);
        Long rootId = archiveCategory == null ? null : archiveCategory.getId();

        ArchiveCategory archiveCategoryWithRoot = new ArchiveCategory();
        archiveCategoryWithRoot.setRootId(rootId);
        archiveCategoryWithRoot.setCode(categoryId);


        return archiveCategoryMapper.selectCategoryList(archiveCategoryWithRoot).get(0);


    }

    /**
     * 复制树
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long copyCategoryTree(Long sourceRootId) {
        // 1. 获取源树的所有节点
        List<ArchiveCategory> sourceCategories = archiveCategoryMapper.selectCategoryByRootId(sourceRootId);

        if (sourceCategories.isEmpty()) {
            throw new RuntimeException("源分类树不存在");
        }

        // 2. 找到根节点，修改名称
        ArchiveCategory sourceRoot = sourceCategories.stream()
                .filter(c -> c.getParentId() == null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到根节点"));

        // 3. 创建ID映射表
        Map<Long, Long> idMapping = new HashMap<>();

        // 4. 按层级排序
        List<ArchiveCategory> sortedCategories = sortCategoriesByLevel(sourceCategories);

        Long newRootId = null;

        // 5. 逐个复制节点
        for (ArchiveCategory sourceCategory : sortedCategories) {
            ArchiveCategory newCategory = new ArchiveCategory();

            // 复制所有属性（包括code，因为code可以重复）
            BeanUtils.copyProperties(sourceCategory, newCategory);

            // 清除ID，让数据库生成
            newCategory.setId(null);

            // 处理根节点
            if (sourceCategory.getParentId() == null) {
                // 根节点添加"副本"后缀以区分
                newCategory.setName(sourceCategory.getName() + " - 副本");
                newCategory.setParentId(null);
                newCategory.setAncestors("");
                newCategory.setRootId(null); // 暂时设为null，后面更新
            } else {
                // 子节点：更新parentId为新的父节点ID
                Long newParentId = idMapping.get(sourceCategory.getParentId());
                if (newParentId == null) {
                    throw new RuntimeException("父节点映射失败");
                }
                newCategory.setParentId(newParentId);

                // 重建ancestors
                ArchiveCategory newParent = archiveCategoryMapper.selectCategoryById(newParentId);
                if (newParent != null) {
                    String parentAncestors = newParent.getAncestors();
                    newCategory.setAncestors(StringUtils.isEmpty(parentAncestors) ?
                            String.valueOf(newParentId) :
                            parentAncestors + "," + newParentId);
                    newCategory.setRootId(newRootId); // 使用新的根ID
                }
            }

            // 插入新节点
            archiveCategoryMapper.insertCategory(newCategory);
            Long newId = newCategory.getId();

            // 如果是根节点，更新rootId
            if (sourceCategory.getParentId() == null) {
                newRootId = newId;
                newCategory.setRootId(newRootId);
                archiveCategoryMapper.updateCategory(newCategory);
            }

            // 保存ID映射
            idMapping.put(sourceCategory.getId(), newId);
        }

        return newRootId;
    }

}