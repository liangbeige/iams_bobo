package com.iams.manage.controller;

import com.iams.common.annotation.Log;
import com.iams.common.core.controller.BaseController;
import com.iams.common.core.domain.AjaxResult;
import com.iams.common.core.page.TableDataInfo;
import com.iams.common.enums.BusinessType;
import com.iams.common.utils.StringUtils;
import com.iams.common.utils.poi.ExcelUtil;
import com.iams.manage.domain.ArchiveCategory;
import com.iams.manage.service.IArchiveCategoryService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 档案分类Controller
 *
 * @author iams
 */
@RestController
@RequestMapping("/manage/category")
public class ArchiveCategoryController extends BaseController
{
    @Autowired
    private IArchiveCategoryService archiveCategoryService;

    /**
     * 获取档案分类根节点列表
     */
    @GetMapping("/roots")
    public AjaxResult getRootList(ArchiveCategory archiveCategory)
    {
        List<ArchiveCategory> list = archiveCategoryService.selectCategoryRoots(archiveCategory);
        return AjaxResult.success(list);
    }

    /**
     * 根据rootId获取整个分类树
     */
    @GetMapping("/tree/{rootId}")
    public AjaxResult getTreeByRootId(@PathVariable("rootId") Long rootId)
    {
        ArchiveCategory tree = archiveCategoryService.selectCategoryTreeByRootId(rootId);
        return AjaxResult.success(tree);
    }

    /**
     * 获取分类树形结构（用于下拉选择）
     */
    @GetMapping("/tree-select/{rootId}")
    public AjaxResult treeSelect(@PathVariable("rootId") Long rootId)
    {
        ArchiveCategory tree = archiveCategoryService.selectCategoryTreeByRootId(rootId);
        List<ArchiveCategory> categories = new ArrayList<>();
        if (tree != null) {
            categories.add(tree);
        }
        return AjaxResult.success(archiveCategoryService.buildCategoryTreeSelect(categories));
    }

    /**
     * 查询档案分类列表（分页）
     */
    @GetMapping("/list")
    public TableDataInfo list(ArchiveCategory archiveCategory)
    {
        startPage();
        List<ArchiveCategory> list = archiveCategoryService.selectCategoryList(archiveCategory);
        return getDataTable(list);
    }

    /**
     * 导出档案分类列表
     */
    @Log(title = "档案分类", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ArchiveCategory archiveCategory)
    {
        List<ArchiveCategory> list = archiveCategoryService.selectCategoryList(archiveCategory);
        ExcelUtil<ArchiveCategory> util = new ExcelUtil<ArchiveCategory>(ArchiveCategory.class);
        util.exportExcel(response, list, "档案分类数据");
    }

    /**
     * 获取档案分类详细信息
     */
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(archiveCategoryService.selectCategoryById(id));
    }

    /**
     * 新增档案分类根节点
     */
    @Log(title = "档案分类", businessType = BusinessType.INSERT)
    @PostMapping("/root")
    public AjaxResult addRoot(@RequestBody ArchiveCategory archiveCategory)
    {
//        if (!archiveCategoryService.checkCategoryCodeUnique(archiveCategory))
//        {
//            return error("新增分类'" + archiveCategory.getName() + "'失败，分类编码已存在");
//        }
        archiveCategory.setCreateBy(getUsername());

        int rows = archiveCategoryService.insertCategory(archiveCategory);
        if (rows > 0) {
            // 插入成功，返回创建后的完整对象（包含自动生成的ID和rootId）
            return success(archiveCategory);
        } else {
            return error("新增分类失败");
        }
    }

    /**
     * 更新档案分类树
     */
    @Log(title = "档案分类树", businessType = BusinessType.UPDATE)
    @PutMapping("/tree/{rootId}")
    public AjaxResult updateCategoryTree(@PathVariable("rootId") Long rootId,
                                         @RequestBody List<ArchiveCategory> categories)
    {
        if (categories == null || categories.isEmpty()) {
            return error("分类数据不能为空");
        }

        // 设置更新人
        String username = getUsername();
        for (ArchiveCategory category : categories) {
            category.setUpdateBy(username);
            if (StringUtils.isEmpty(category.getCreateBy())) {
                category.setCreateBy(username);
            }
        }

        try {
            int rows = archiveCategoryService.updateCategoryTree(rootId, categories);
            if (rows > 0) {
                return success("更新成功，共处理 " + rows + " 个分类");
            } else {
                return error("更新失败");
            }
        } catch (Exception e) {
            logger.error("更新分类树失败", e);
            return error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 更新单个档案分类
     */
    @Log(title = "档案分类", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public AjaxResult updateCategory(@PathVariable("id") Long id,
                                     @RequestBody ArchiveCategory archiveCategory)
    {
        archiveCategory.setId(id);
        if (!archiveCategoryService.checkCategoryCodeUnique(archiveCategory))
        {
            return error("修改分类'" + archiveCategory.getName() + "'失败，分类编码已存在");
        }
        archiveCategory.setUpdateBy(getUsername());
        return toAjax(archiveCategoryService.updateCategory(archiveCategory));
    }

    /**
     * 删除档案分类
     */
    @Log(title = "档案分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id)
    {
        if (archiveCategoryService.hasChildById(id))
        {
            return error("存在下级分类，不允许删除");
        }
        return toAjax(archiveCategoryService.deleteCategory(id));
    }

    /**
     * 检查编码是否唯一
     */
    @GetMapping("/check-code")
    public AjaxResult checkCodeUnique(ArchiveCategory archiveCategory)
    {
        return AjaxResult.success(archiveCategoryService.checkCategoryCodeUnique(archiveCategory));
    }

    @GetMapping("/by-code/{categoryCode}")
    public AjaxResult getCategoryTreeByCode(@PathVariable String categoryCode) {
        ArchiveCategory tree = archiveCategoryService.selectCategoryTreeByRootId(archiveCategoryService.getCategoryRootIdByCode(categoryCode));
        return AjaxResult.success(tree);
    }

    @GetMapping("/by-unique-key/{uniqueKey}")
    public AjaxResult getCategoryTreeByUniqueKey(@PathVariable String uniqueKey) {
        try {
            // 标准化冒号格式
            String standardizedKey = uniqueKey.replace("：", ":");

            // 解析 uniqueKey
            String[] parts = standardizedKey.split(":");
            if (parts.length < 2) {
                return AjaxResult.error("无效的门类标识格式");
            }
            String code = parts[0].trim();
            String name = parts[1].trim();

            // 获取根节点ID
            Long rootId = archiveCategoryService.getCategoryRootIdByCodeAndName(code, name);
            if (rootId == null) {
                return AjaxResult.error("未找到对应的门类根节点: " + standardizedKey);
            }

            // 获取树结构
            ArchiveCategory tree = archiveCategoryService.selectCategoryTreeByRootId(rootId);
            return AjaxResult.success(tree);
        } catch (Exception e) {
            logger.error("获取门类树失败", e);
            return AjaxResult.error("处理门类标识失败: " + uniqueKey);
        }
    }

    @GetMapping("/by-archiveId/{id}")
    public AjaxResult getCategoryTreeByArchiveId(@PathVariable Long id) {
        return AjaxResult.success(archiveCategoryService.getCategoryNameListByArchiveId(id));
    }

    /**
     * 根据分类编码获取分类的name，code: code, name: name包装到data中返回
     */
    @GetMapping("/by-code/subclass/{code}")
    public AjaxResult getCategoryTreeByCodeSubclass(@PathVariable String code) {
        String name = archiveCategoryService.getCategoryNameByCode(code);

        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("name", name);
        return AjaxResult.success(result);
    }

    //复制树
    @PostMapping("/copy/{rootId}")
    public AjaxResult copyCategoryTree(@PathVariable Long rootId) {
        Long newRootId = archiveCategoryService.copyCategoryTree(rootId);
        return AjaxResult.success("复制成功", newRootId);
    }

    @GetMapping("/getByCodeWithRoot")
    public AjaxResult getByCodeWithRoot(@RequestParam("rootCode") String code,
                                             @RequestParam("rootName") String rootName,
                                             @RequestParam("categoryId") String categoryId) {

        ArchiveCategory archiveCategory = archiveCategoryService.selectCategoryByCodeWithRoot(code, rootName, categoryId);

        Map<String, Object> result = new HashMap<>();
        result.put("code", archiveCategory.getCode());
        result.put("name", archiveCategory.getName());

        return AjaxResult.success(result);
    }

}