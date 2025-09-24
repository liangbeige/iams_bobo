package com.iams.manage.service;

import java.util.List;
import com.iams.manage.domain.Fonds;

/**
 * 全宗管理Service接口
 * 
 * @author zhjm
 * @date 2025-01-05
 */
public interface IFondsService 
{
    /**
     * 查询全宗管理
     * 
     * @param id 全宗管理主键
     * @return 全宗管理
     */
    public Fonds selectFondsById(Long id);

    /**
     * 查询全宗管理列表
     * 
     * @param fonds 全宗管理
     * @return 全宗管理集合
     */
    public List<Fonds> selectFondsList(Fonds fonds);

    /**
     * 新增全宗管理
     * 
     * @param fonds 全宗管理
     * @return 结果
     */
    public int insertFonds(Fonds fonds);

    /**
     * 修改全宗管理
     * 
     * @param fonds 全宗管理
     * @return 结果
     */
    public int updateFonds(Fonds fonds);

    /**
     * 批量删除全宗管理
     * 
     * @param ids 需要删除的全宗管理主键集合
     * @return 结果
     */
    public int deleteFondsByIds(Long[] ids);

    /**
     * 删除全宗管理信息
     * 
     * @param id 全宗管理主键
     * @return 结果
     */
    public int deleteFondsById(Long id);


    public String importFonds(List<Fonds> fondsList, Boolean isUpdateSupport, String operName);

    /**
     * 更新指定全宗的档案数量
     * @param fondsId 全宗ID
     * @return 更新结果
     */
    boolean updateFondsArchiveCount(Long fondsId);

    /**
     * 更新所有全宗的档案数量
     * @return 更新结果
     */
    boolean updateAllFondsArchiveCount();

    /**
     * 统计指定全宗下的档案数量
     * @param fondsId 全宗ID
     * @return 档案数量
     */
    Long countArchivesByFondsId(Long fondsId);

    public boolean changeCategoryCodesToCodeName();
}
