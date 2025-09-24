package com.iams.manage.mapper;

import java.util.List;
import com.iams.manage.domain.Cabinet;
import org.apache.ibatis.annotations.Param;

/**
 * 档案柜管理Mapper接口
 * 
 * @author zhjm
 * @date 2025-01-06
 */
public interface CabinetMapper 
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
     * 删除档案柜管理
     * 
     * @param id 档案柜管理主键
     * @return 结果
     */
    public int deleteCabinetById(Long id);

    /**
     * 批量删除档案柜管理
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCabinetByIds(Long[] ids);

    List<Long> selectCabinetIds(Cabinet cabinet);

    public Cabinet selectCabinetByLocation(Long repositoryId, Integer quNo, Integer colNo, String zyNo);

    List<Cabinet> getCabinetByQuNoList(@Param("array") Long[] quNos);

    public List<Cabinet> getUsedCabinet();

//    public Cabinet selectCabinetByLocation(Integer quNo, Integer colNo, char zyNo);
}
