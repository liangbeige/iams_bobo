package com.iams.manage.controller;

import java.util.List;
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
import com.iams.manage.domain.Repository;
import com.iams.manage.service.IRepositoryService;
import com.iams.common.utils.poi.ExcelUtil;
import com.iams.common.core.page.TableDataInfo;

/**
 * 库房管理Controller
 * 
 * @author zhjm
 * @date 2025-01-06
 */
@RestController
@RequestMapping("/manage/repository")
public class RepositoryController extends BaseController
{
    @Autowired
    private IRepositoryService repositoryService;

    /**
     * 查询库房管理列表
     */
    @PreAuthorize("@ss.hasPermi('manage:repository:list')")
    @GetMapping("/list")
    public TableDataInfo list(Repository repository)
    {
        startPage();
        List<Repository> list = repositoryService.selectRepositoryList(repository);
        return getDataTable(list);
    }

    /**
     * 导出库房管理列表
     */
    @PreAuthorize("@ss.hasPermi('manage:repository:export')")
    @Log(title = "库房管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Repository repository)
    {
        List<Repository> list = repositoryService.selectRepositoryList(repository);
        ExcelUtil<Repository> util = new ExcelUtil<Repository>(Repository.class);
        util.exportExcel(response, list, "库房管理数据");
    }

    /**
     * 获取库房管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('manage:repository:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(repositoryService.selectRepositoryById(id));
    }

    /**
     * 新增库房管理
     */
    @PreAuthorize("@ss.hasPermi('manage:repository:add')")
    @Log(title = "库房管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Repository repository)
    {
        return toAjax(repositoryService.insertRepository(repository));
    }

    /**
     * 修改库房管理
     */
    @PreAuthorize("@ss.hasPermi('manage:repository:edit')")
    @Log(title = "库房管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Repository repository)
    {
        return toAjax(repositoryService.updateRepository(repository));
    }

    /**
     * 删除库房管理
     */
    @PreAuthorize("@ss.hasPermi('manage:repository:remove')")
    @Log(title = "库房管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(repositoryService.deleteRepositoryByIds(ids));
    }
}
