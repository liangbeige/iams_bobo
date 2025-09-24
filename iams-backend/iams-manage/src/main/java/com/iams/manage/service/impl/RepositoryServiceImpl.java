package com.iams.manage.service.impl;

import java.util.List;
import com.iams.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.iams.manage.mapper.RepositoryMapper;
import com.iams.manage.domain.Repository;
import com.iams.manage.service.IRepositoryService;

/**
 * 库房管理Service业务层处理
 * 
 * @author zhjm
 * @date 2025-01-06
 */
@Service
public class RepositoryServiceImpl implements IRepositoryService 
{
    @Autowired
    private RepositoryMapper repositoryMapper;

    /**
     * 查询库房管理
     * 
     * @param id 库房管理主键
     * @return 库房管理
     */
    @Override
    public Repository selectRepositoryById(Long id)
    {
        return repositoryMapper.selectRepositoryById(id);
    }

    /**
     * 查询库房管理列表
     * 
     * @param repository 库房管理
     * @return 库房管理
     */
    @Override
    public List<Repository> selectRepositoryList(Repository repository)
    {
        return repositoryMapper.selectRepositoryList(repository);
    }

    /**
     * 新增库房管理
     * 
     * @param repository 库房管理
     * @return 结果
     */
    @Override
    public int insertRepository(Repository repository)
    {
        repository.setCreateTime(DateUtils.getNowDate());
        return repositoryMapper.insertRepository(repository);
    }

    /**
     * 修改库房管理
     * 
     * @param repository 库房管理
     * @return 结果
     */
    @Override
    public int updateRepository(Repository repository)
    {
        repository.setUpdateTime(DateUtils.getNowDate());
        return repositoryMapper.updateRepository(repository);
    }

    /**
     * 批量删除库房管理
     * 
     * @param ids 需要删除的库房管理主键
     * @return 结果
     */
    @Override
    public int deleteRepositoryByIds(Long[] ids)
    {
        return repositoryMapper.deleteRepositoryByIds(ids);
    }

    /**
     * 删除库房管理信息
     * 
     * @param id 库房管理主键
     * @return 结果
     */
    @Override
    public int deleteRepositoryById(Long id)
    {
        return repositoryMapper.deleteRepositoryById(id);
    }
}
