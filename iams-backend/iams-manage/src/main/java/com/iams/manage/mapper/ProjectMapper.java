package com.iams.manage.mapper;

import java.util.List;
import com.iams.manage.domain.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 项目管理Mapper接口
 * 
 * @author zhld
 * @date 2025-06-01
 */
@Mapper
public interface ProjectMapper 
{
    /**
     * 查询项目管理
     * 
     * @param id 项目管理主键
     * @return 项目管理
     */
    public Project selectProjectById(Long id);

    /**
     * 查询项目管理列表
     * 
     * @param project 项目管理
     * @return 项目管理集合
     */
    public List<Project> selectProjectList(Project project);

    /**
     * 新增项目管理
     * 
     * @param project 项目管理
     * @return 结果
     */
    public int insertProject(Project project);

    /**
     * 修改项目管理
     * 
     * @param project 项目管理
     * @return 结果
     */
    public int updateProject(Project project);

    /**
     * 删除项目管理
     * 
     * @param id 项目管理主键
     * @return 结果
     */
    public int deleteProjectById(Long id);

    /**
     * 批量删除项目管理
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteProjectByIds(Long[] ids);

//    /**
//     * 更新项目档案数量
//     *
//     * @param projectId 项目ID
//     * @param count 档案数量
//     * @return 结果
//     */
//    int updateArchiveCount(@Param("projectId") Long projectId, @Param("count") Long count);

    /**
     * 更新项目档案数量
     * @param projectId 项目ID
     * @param count 档案数量
     * @return 更新结果
     */
    int updateArchiveCount(Long projectId, Long count);
}
