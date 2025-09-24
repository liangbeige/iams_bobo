package com.iams.manage.controller;

import java.util.List;

import com.github.pagehelper.PageHelper;
import com.iams.manage.domain.Archive;
import com.iams.manage.service.IArchiveService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.iams.common.annotation.Log;
import com.iams.common.core.controller.BaseController;
import com.iams.common.core.domain.AjaxResult;
import com.iams.common.enums.BusinessType;
import com.iams.manage.domain.Project;
import com.iams.manage.service.IProjectService;
import com.iams.common.utils.poi.ExcelUtil;
import com.iams.common.core.page.TableDataInfo;

/**
 * 项目管理Controller
 * 
 * @author zhld
 * @date 2025-06-01
 */
@RestController
@RequestMapping("/manage/project")
public class ProjectController extends BaseController {
    @Autowired
    private IProjectService projectService;
    @Autowired
    private IArchiveService archiveService;

    /**
     * 查询项目管理列表
     */
    //@PreAuthorize("@ss.hasPermi('manage:project:list')")
    @GetMapping("/list")
    public TableDataInfo list(Project project) {
        startPage();
        List<Project> list = projectService.selectProjectList(project);
        return getDataTable(list);
    }

    @GetMapping("/list-all")
    public TableDataInfo listAll(Project project) {
        List<Project> list = projectService.selectProjectList(project);
        return getDataTable(list);
    }

    /**
     * 导出项目管理列表
     */
    @PreAuthorize("@ss.hasPermi('manage:project:export')")
    @Log(title = "项目管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Project project) {
        List<Project> list = projectService.selectProjectList(project);
        ExcelUtil<Project> util = new ExcelUtil<Project>(Project.class);
        util.exportExcel(response, list, "项目管理数据");
    }

    /**
     * 获取项目管理详细信息
     */
    //@PreAuthorize("@ss.hasPermi('manage:project:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(projectService.selectProjectById(id));
    }

    /**
     * 新增项目管理
     */
    @PreAuthorize("@ss.hasPermi('manage:project:add')")
    @Log(title = "项目管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Project project) {
        return toAjax(projectService.insertProject(project));
    }

    /**
     * 修改项目管理
     */
    @PreAuthorize("@ss.hasPermi('manage:project:edit')")
    @Log(title = "项目管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Project project) {
        return toAjax(projectService.updateProject(project));
    }

    /**
     * 删除项目管理
     */
    @PreAuthorize("@ss.hasPermi('manage:project:remove')")
    @Log(title = "项目管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(projectService.deleteProjectByIds(ids));

    }
    @GetMapping("/archives")
    public TableDataInfo listProjectArchives(
            @RequestParam Long projectId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        // 创建查询条件 - 只查询指定项目的档案
        Archive query = new Archive();
        query.setProjectId(projectId);

        // 执行分页查询
        PageHelper.startPage(pageNum, pageSize);
        List<Archive> list = archiveService.selectArchiveList(query);

        // 返回结果
        return getDataTable(list);
    }


}
