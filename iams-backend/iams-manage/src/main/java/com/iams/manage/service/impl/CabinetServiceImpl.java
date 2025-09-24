package com.iams.manage.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.iams.common.utils.DateUtils;
import com.iams.manage.domain.Compartment;
import com.iams.manage.mapper.CompartmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.iams.manage.mapper.CabinetMapper;
import com.iams.manage.domain.Cabinet;
import com.iams.manage.service.ICabinetService;

/**
 * 档案柜管理Service业务层处理
 * 
 * @author zhjm
 * @date 2025-01-06
 */
@Service
public class CabinetServiceImpl implements ICabinetService 
{
    @Autowired
    private CabinetMapper cabinetMapper;

    @Autowired
    private CompartmentMapper compartmentMapper;

    /**
     * 查询档案柜管理
     * 
     * @param id 档案柜管理主键
     * @return 档案柜管理
     */
    @Override
    public Cabinet selectCabinetById(Long id)
    {
        return cabinetMapper.selectCabinetById(id);
    }

    /**
     * 查询档案柜管理列表
     * 
     * @param cabinet 档案柜管理
     * @return 档案柜管理
     */
    @Override
    public List<Cabinet> selectCabinetList(Cabinet cabinet)
    {
        return cabinetMapper.selectCabinetList(cabinet);
    }

    /**
     * 新增档案柜管理
     * 
     * @param cabinet 档案柜管理
     * @return 结果
     */
    @Override
    public int insertCabinet(Cabinet cabinet)
    {
        cabinet.setCreateTime(DateUtils.getNowDate());
        return cabinetMapper.insertCabinet(cabinet);
    }

    /**
     * 修改档案柜管理
     * 
     * @param cabinet 档案柜管理
     * @return 结果
     */
    @Override
    public int updateCabinet(Cabinet cabinet)
    {
        cabinet.setUpdateTime(DateUtils.getNowDate());
        return cabinetMapper.updateCabinet(cabinet);
    }

    /**
     * 批量删除档案柜管理
     * 
     * @param ids 需要删除的档案柜管理主键
     * @return 结果
     */
    @Override
    public int deleteCabinetByIds(Long[] ids)
    {
        return cabinetMapper.deleteCabinetByIds(ids);
    }

    /**
     * 删除档案柜管理信息
     * 
     * @param id 档案柜管理主键
     * @return 结果
     */
    @Override
    public int deleteCabinetById(Long id)
    {
        return cabinetMapper.deleteCabinetById(id);
    }

    /**
     * 根据仓库ID、排和列查询档案柜
     *
     * @param repositoryId 仓库ID
     * @param quNo         区
     * @param colNo        列
     * @param zyNo         左右
     * @return 档案柜
     */
    @Override
    public Cabinet selectCabinetByLocation(Long repositoryId, Integer quNo, Integer colNo, String zyNo)
    {
        return cabinetMapper.selectCabinetByLocation(repositoryId, quNo, colNo,  zyNo);
    }

    @Override
    public List<Long> selectCabinetIds(Cabinet cabinet) {
        return cabinetMapper.selectCabinetIds(cabinet);
    }

    @Override
    public List<Cabinet> getCabinetByQuNoList(String[] quNoStrs) {
        Long[] quNos = Arrays.stream(quNoStrs)
                .map(Long::valueOf)
                .toArray(Long[]::new);
        return cabinetMapper.getCabinetByQuNoList(quNos);
    }

    @Override
    public int updateSize(String location, String option) {
        try {
            // 解析字符串
            String[] parts = location.split("-");
            System.out.println(Arrays.toString(parts));

            Integer quNo = Integer.parseInt(parts[0]);
            Integer colNo = Integer.parseInt(parts[1]);
            String zyNo = parts[2];

            // 查询档案柜
            Cabinet cabinet = selectCabinetByLocation(10000L, quNo, colNo, zyNo);
            if (cabinet == null) {
                return 0;
            }

            // 更新 size 字段
            if (option.equals("add")) {
                cabinet.setSize(cabinet.getSize() + 1);
            } else if (option.equals("delete")) {
                cabinet.setSize(cabinet.getSize() - 1);
            } else {
                return 0;
            }
            int rows = updateCabinet(cabinet);
            return Math.max(rows, 0);
        } catch (Exception e) {
//            return error("Error updating cabinet size: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public boolean synchronizeLocationStats(Map<String, Integer> locationData) {
        try {
            // 1. 清空旧的统计数据, 初始化 size 为 0
            List<Cabinet> cabinetList = cabinetMapper.getUsedCabinet();
            for (Cabinet cabinet : cabinetList) {
                cabinet.setSize(0L);
                updateCabinet(cabinet);
            }
            List<Compartment> compartmentList = compartmentMapper.getUsedCompartment();
            for (Compartment compartment : compartmentList) {
                compartment.setSize(0L);
                compartmentMapper.updateCompartment(compartment);
            }

            // 2. 批量插入新的统计数据
            for (Map.Entry<String, Integer> entry : locationData.entrySet()) {
                String location = entry.getKey();
                Integer count = entry.getValue();

                // 解析位置信息
                String[] locationParts = location.split("-");
                if (locationParts.length >= 2) {
                    // 更新数据
                    Cabinet cabinet = selectCabinetByLocation(10000L, Integer.parseInt(locationParts[0]), Integer.parseInt(locationParts[1]), locationParts[2]);
                    if (cabinet != null) {
                        cabinet.setSize(cabinet.getSize() + count);
                        updateCabinet(cabinet);
                    }
                    Compartment compartment = compartmentMapper.selectCompartmentByLocation(cabinet.getId(), Integer.parseInt(locationParts[3]), Integer.parseInt(locationParts[4]));
                    if (compartment != null) {
                        compartment.setSize(compartment.getSize() + count);
                        compartmentMapper.updateCompartment(compartment);
                    }
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


//    @Override
//    public Cabinet selectCabinetByLocation(Integer pai, Integer lie) {
//        return cabinetMapper.selectCabinetByLocation(pai, lie);
//    }
}
