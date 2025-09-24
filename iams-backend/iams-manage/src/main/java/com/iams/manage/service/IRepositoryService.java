package com.iams.manage.service;

import java.util.List;
import com.iams.manage.domain.Repository;

/**
 * 库房管理Service接口
 * 
 * @author zhjm
 * @date 2025-01-06
 */
public interface IRepositoryService 
{
    /**
     * 查询库房管理
     * 
     * @param id 库房管理主键
     * @return 库房管理
     */
    public Repository selectRepositoryById(Long id);

    /**
     * 查询库房管理列表
     * 
     * @param repository 库房管理
     * @return 库房管理集合
     */
    public List<Repository> selectRepositoryList(Repository repository);

    /**
     * 新增库房管理
     * 
     * @param repository 库房管理
     * @return 结果
     */
    public int insertRepository(Repository repository);

    /**
     * 修改库房管理
     * 
     * @param repository 库房管理
     * @return 结果
     */
    public int updateRepository(Repository repository);

    /**
     * 批量删除库房管理
     * 
     * @param ids 需要删除的库房管理主键集合
     * @return 结果
     */
    public int deleteRepositoryByIds(Long[] ids);

    /**
     * 删除库房管理信息
     * 
     * @param id 库房管理主键
     * @return 结果
     */
    public int deleteRepositoryById(Long id);
}
