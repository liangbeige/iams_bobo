package com.iams.manage.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.iams.common.core.domain.entity.SysUser;
import com.iams.common.utils.DateUtils;
import com.iams.manage.domain.ArchiveCategory;
import com.iams.manage.service.IArchiveCategoryService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.iams.common.annotation.Log;
import com.iams.common.core.controller.BaseController;
import com.iams.common.core.domain.AjaxResult;
import com.iams.common.enums.BusinessType;
import com.iams.manage.domain.Fonds;
import com.iams.manage.service.IFondsService;
import com.iams.common.utils.poi.ExcelUtil;
import com.iams.common.core.page.TableDataInfo;
import org.springframework.web.multipart.MultipartFile;

/**
 * 全宗管理Controller
 * 
 * @author zhjm
 * @date 2025-01-05
 */
@RestController
@RequestMapping("/manage/fonds")
public class FondsController extends BaseController
{
    @Autowired
    private IFondsService fondsService;


    @Autowired
    private IArchiveCategoryService archiveCategoryService;

    /**
     * 查询全宗管理列表
     */

//    @PreAuthorize("@ss.hasPermi('manage:fonds:list')")
    @GetMapping("/list")
    public TableDataInfo list(Fonds fonds) {
        startPage();
        List<Fonds> list = fondsService.selectFondsList(fonds);
        return getDataTable(list);
    }

    /**
     * 手动更新所有全宗的档案数量
     */
    @PreAuthorize("@ss.hasPermi('manage:fonds:edit')")
    @Log(title = "全宗管理", businessType = BusinessType.UPDATE)
    @PostMapping("/updateArchiveCount")
    public AjaxResult updateArchiveCount() {
        boolean result = fondsService.updateAllFondsArchiveCount();
        return result ? success("档案数量更新成功") : error("档案数量更新失败");
    }

    /**
     * 更新指定全宗的档案数量
     */
    @PreAuthorize("@ss.hasPermi('manage:fonds:edit')")
    @Log(title = "全宗管理", businessType = BusinessType.UPDATE)
    @PostMapping("/updateArchiveCount/{fondsId}")
    public AjaxResult updateFondsArchiveCount(@PathVariable Long fondsId) {
        boolean result = fondsService.updateFondsArchiveCount(fondsId);
        return result ? success("档案数量更新成功") : error("档案数量更新失败");
    }

    /**
     * 导出全宗管理列表
     */
    @PreAuthorize("@ss.hasPermi('manage:fonds:export')")
    @Log(title = "全宗管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Fonds fonds)
    {
        fondsService.updateAllFondsArchiveCount(); //导出前先更新档案数量
        List<Fonds> list = fondsService.selectFondsList(fonds);
        ExcelUtil<Fonds> util = new ExcelUtil<Fonds>(Fonds.class);
        util.exportExcel(response, list, "全宗管理数据");
    }

    /**
     * 获取全宗管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('manage:fonds:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {

        return success(fondsService.selectFondsById(id));
    }

    /**
     * 新增全宗管理
     */
    @PreAuthorize("@ss.hasPermi('manage:fonds:add')")
    @Log(title = "全宗管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Fonds fonds)
    {
        return toAjax(fondsService.insertFonds(fonds));
    }


    /**
     * 导入全宗
     */
    @PreAuthorize("@ss.hasPermi('manage:fonds:add')")
    @Log(title = "全宗管理", businessType = BusinessType.INSERT)
    @PostMapping("/importDate")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<Fonds> util = new ExcelUtil<Fonds>(Fonds.class);
        List<Fonds> fondsList = util.importExcel(file.getInputStream());
        String operName = getUsername();
        String message = fondsService.importFonds(fondsList, updateSupport, operName);
        return success(message);
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<Fonds> util = new ExcelUtil<Fonds>(Fonds.class);
        util.importTemplateExcel(response, "全宗数据");
    }

    /**
     * 修改全宗管理
     */
    @PreAuthorize("@ss.hasPermi('manage:fonds:edit')")
    @Log(title = "全宗管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Fonds fonds)
    {
        return toAjax(fondsService.updateFonds(fonds));
    }

    /**
     * 删除全宗管理
     */
    @PreAuthorize("@ss.hasPermi('manage:fonds:remove')")
    @Log(title = "全宗管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(fondsService.deleteFondsByIds(ids));
    }

    /**
     * 获取全宗门类配置
     */
    @PreAuthorize("@ss.hasPermi('manage:fonds:query')")
    @GetMapping("/category-config/{fondsId}")
    public AjaxResult getCategoryConfig(@PathVariable("fondsId") Long fondsId) {
        try {
            Fonds fonds = fondsService.selectFondsById(fondsId);
            if (fonds == null) {
                return error("全宗不存在");
            }

            List<String> configItems = fonds.getCategoryConfigItems();
            if (configItems.isEmpty()) {
                return success(new ArrayList<>());
            }

            // 获取所有根节点
            ArchiveCategory query = new ArchiveCategory();
            List<ArchiveCategory> allRoots = archiveCategoryService.selectCategoryRoots(query);

            // 创建映射：code:name → 门类对象
            Map<String, ArchiveCategory> categoryMap = allRoots.stream()
                    .collect(Collectors.toMap(
                            root -> root.getCode() + ":" + root.getName(),
                            root -> root
                    ));

            // 筛选已配置的门类
            List<ArchiveCategory> configuredCategories = configItems.stream()
                    .map(categoryMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return success(configuredCategories);
        } catch (Exception e) {
            logger.error("获取全宗门类配置失败", e);
            return error("获取门类配置失败");
        }
    }

    /**
     * 保存全宗门类配置
     */
    @PreAuthorize("@ss.hasPermi('manage:fonds:edit')")
    @PostMapping("/category-config/{fondsId}")
    public AjaxResult saveCategoryConfig(@PathVariable("fondsId") Long fondsId, @RequestBody Map<String, Object> params) {
        try {
            Fonds fonds = fondsService.selectFondsById(fondsId);
            if (fonds == null) {
                return error("全宗不存在");
            }

            // 获取前端传来的门类代码列表
            @SuppressWarnings("unchecked")
            List<String> categoryCodes = (List<String>) params.get("categoryCodes");

            if (categoryCodes == null) {
                categoryCodes = new ArrayList<>();
            }

            // 验证门类代码是否存在
            for (String code : categoryCodes) {
                ArchiveCategory category = archiveCategoryService.selectCategoryByCode(code);
                if (category == null) {
                    return error("门类代码 " + code + " 不存在");
                }
                // 验证是否为根节点
                if (category.getParentId() != null && category.getParentId() != 0) {
                    return error("门类 " + code + " 不是根节点");
                }
            }

            // 更新全宗的门类配置
            fonds.setCategoryCodeList(categoryCodes);
            fonds.setUpdateTime(DateUtils.getNowDate());
            fonds.setUpdateBy(getUsername());

            int result = fondsService.updateFonds(fonds);

            return result > 0 ? success("保存成功") : error("保存失败");

        } catch (Exception e) {
            logger.error("保存全宗门类配置失败", e);
            return error("保存失败：" + e.getMessage());
        }
    }


    @PostMapping("/CategoryCodesToCodeName")
    public AjaxResult CategoryCodesToCodeName() {
        return success(fondsService.changeCategoryCodesToCodeName());
    }
}
