package com.iams.manage.service;

import java.util.List;
import java.util.Map;

import com.iams.manage.domain.Cabinet;

/**
 * 档案柜管理Service接口
 * 
 * @author zhjm
 * @date 2025-01-06
 */
public interface ICabinetService 
{
    /**
     * 查询档案柜管理
     * 
     * @param id 档案柜管理主键
     * @return 档案柜管理
     */
    public Cabinet selectCabinetById(Long id);

    /**
     * 查询档案柜管理列表
     * 
     * @param cabinet 档案柜管理
     * @return 档案柜管理集合
     */
    public List<Cabinet> selectCabinetList(Cabinet cabinet);

    /**
     * 新增档案柜管理
     * 
     * @param cabinet 档案柜管理
     * @return 结果
     */
    public int insertCabinet(Cabinet cabinet);

    /**
     * 修改档案柜管理
     * 
     * @param cabinet 档案柜管理
     * @return 结果
     */
    public int updateCabinet(Cabinet cabinet);

    /**
     * 批量删除档案柜管理
     * 
     * @param ids 需要删除的档案柜管理主键集合
     * @return 结果
     */
    public int deleteCabinetByIds(Long[] ids);

    /**
     * 删除档案柜管理信息
     * 
     * @param id 档案柜管理主键
     * @return 结果
     */
    public int deleteCabinetById(Long id);

    public Cabinet selectCabinetByLocation(Long repositoryId, Integer quNo, Integer colNo, String zyNo);

    List<Long> selectCabinetIds(Cabinet cabinet);

    List<Cabinet> getCabinetByQuNoList(String[] quNos);

    public int updateSize(String location, String option);

    boolean synchronizeLocationStats(Map<String, Integer> locationData);

//    public Cabinet selectCabinetByLocation(Integer pai, Integer lie);

}
