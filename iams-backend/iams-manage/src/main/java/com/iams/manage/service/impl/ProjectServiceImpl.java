package com.iams.manage.service.impl;

import java.util.List;
import com.iams.common.utils.DateUtils;
import com.iams.manage.mapper.ArchiveMapper;
import com.iams.manage.service.IArchiveService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.iams.manage.mapper.ProjectMapper;
import com.iams.manage.domain.Project;
import com.iams.manage.service.IProjectService;

/**
 * 项目管理Service业务层处理
 * 
 * @author zhld
 * @date 2025-06-01
 */
@Service
public class ProjectServiceImpl implements IProjectService 
{
    @Autowired
    private ArchiveMapper archiveMapper;

    @Autowired
    private ProjectMapper projectMapper;

//    @Autowired
//    private IArchiveService archiveService;

    /**
     * 查询项目管理
     * 
     * @param id 项目管理主键
     * @return 项目管理
     */
    @Override
    public Project selectProjectById(Long id)
    {
        return projectMapper.selectProjectById(id);
    }

    /**
     * 查询项目管理列表
     * 
     * @param project 项目管理
     * @return 项目管理
     */
    @Override
    public List<Project> selectProjectList(Project project)
    {
        return projectMapper.selectProjectList(project);
    }

    /**
     * 新增项目管理
     * 
     * @param project 项目管理
     * @return 结果
     */
    @Override
    public int insertProject(Project project)
    {
        project.setCreateTime(DateUtils.getNowDate());
        return projectMapper.insertProject(project);
    }

    /**
     * 修改项目管理
     * 
     * @param project 项目管理
     * @return 结果
     */
    @Override
    public int updateProject(Project project)
    {
        project.setUpdateTime(DateUtils.getNowDate());
        return projectMapper.updateProject(project);
    }

    /**
     * 批量删除项目管理
     * 
     * @param ids 需要删除的项目管理主键
     * @return 结果
     */
    @Override
    public int deleteProjectByIds(Long[] ids)
    {
        return projectMapper.deleteProjectByIds(ids);
    }

    /**
     * 删除项目管理信息
     * 
     * @param id 项目管理主键
     * @return 结果
     */
    @Override
    public int deleteProjectById(Long id)
    {
        return projectMapper.deleteProjectById(id);
    }

    @Override
    public int updateArchiveCount(Long projectId) {
        // 改为调用Mapper直接统计当前档案数量
        Long count = archiveMapper.countByProjectId(projectId);
        return projectMapper.updateArchiveCount(projectId, count);
    }

    @PostConstruct
    public void initProjectStats() {
        // 获取所有项目
        List<Project> projects = projectMapper.selectProjectList(new Project());

        // 更新每个项目的档案统计
        for(Project project : projects) {
            updateArchiveCount(project.getId());
        }
    }







}
