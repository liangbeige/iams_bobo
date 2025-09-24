package com.iams.manage.service.impl;

import java.util.List;

import com.iams.manage.domain.ArchiveCategory;
import com.iams.manage.mapper.ArchiveCategoryMapper;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.iams.common.exception.ServiceException;
import com.iams.common.utils.DateUtils;
import com.iams.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.iams.manage.mapper.FondsMapper;
import com.iams.manage.domain.Fonds;
import com.iams.manage.service.IFondsService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 全宗管理Service业务层处理
 * 
 * @author zhjm
 * @date 2025-01-05
 */
@Service
public class FondsServiceImpl implements IFondsService 
{

    private static final Logger log = LoggerFactory.getLogger(FondsServiceImpl.class);

    @Autowired
    private FondsMapper fondsMapper;

    @Autowired
    private ArchiveCategoryMapper archiveCategoryMapper;

    /**
     * 查询全宗管理
     * 
     * @param id 全宗管理主键
     * @return 全宗管理
     */
    @Override
    public Fonds selectFondsById(Long id)
    {
        return fondsMapper.selectFondsById(id);
    }

//    /**
//     * 查询全宗管理列表
//     *
//     * @param fonds 全宗管理
//     * @return 全宗管理
//     */
//    @Override
//    public List<Fonds> selectFondsList(Fonds fonds)
//    {
//
//        return fondsMapper.selectFondsList(fonds);
//    }

    /**
     * 新增全宗管理
     * 
     * @param fonds 全宗管理
     * @return 结果
     */
    @Override
    public int insertFonds(Fonds fonds)
    {
        fonds.setCreateTime(DateUtils.getNowDate());
        return fondsMapper.insertFonds(fonds);
    }

    /**
     * 修改全宗管理
     * 
     * @param fonds 全宗管理
     * @return 结果
     */
    @Override
    public int updateFonds(Fonds fonds)
    {
        fonds.setUpdateTime(DateUtils.getNowDate());
        return fondsMapper.updateFonds(fonds);
    }

    /**
     * 批量删除全宗管理
     * 
     * @param ids 需要删除的全宗管理主键
     * @return 结果
     */
    @Override
    public int deleteFondsByIds(Long[] ids)
    {
        return fondsMapper.deleteFondsByIds(ids);
    }

    /**
     * 删除全宗管理信息
     * 
     * @param id 全宗管理主键
     * @return 结果
     */
    @Override
    public int deleteFondsById(Long id)
    {
        return fondsMapper.deleteFondsById(id);
    }


    @Override
    public String importFonds(List<Fonds> fondsList, Boolean isUpdateSupport, String operName)
    {
        if (StringUtils.isNull(fondsList) || fondsList.size() == 0)
        {
            throw new ServiceException("导入用户数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();

        for(Fonds fonds : fondsList)
        {
            try
            {
                Fonds fond = fondsMapper.selectFondsById(fonds.getId());
                if (StringUtils.isNull(fond))
                {
                    fondsMapper.insertFonds(fonds);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、全宗号 " + fonds.getBianhao()+ " 导入成功");
                }
                else if(isUpdateSupport)
                {
                    fondsMapper.updateFonds(fonds);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、全宗号 " + fonds.getBianhao()+ " 更新成功");
                }
                else
                {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、全宗号 " + fonds.getBianhao()+ " 已存在");
                }

            }
            catch (Exception e)
            {
                failureNum++;
                String msg = "<br/>" + failureNum + "、全宗号 " + fonds.getBianhao()+ " 导入失败：";
                failureMsg.append(msg + e.getMessage());
                log.error(msg, e);
            }

        }
        if(failureNum > 0)
        {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new ServiceException(failureMsg.toString());
        }
        else
        {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }

    /**
     * 更新指定全宗的档案数量
     */
    @Override
    @Transactional
    public boolean updateFondsArchiveCount(Long fondsId) {
        try {
            if (fondsId == null) {
                return false;
            }
            int result = fondsMapper.updateFondsArchiveCount(fondsId);
            log.info("更新全宗[{}]档案数量成功", fondsId);
            return result > 0;
        } catch (Exception e) {
            log.error("更新全宗[{}]档案数量失败", fondsId, e);
            return false;
        }
    }

    /**
     * 更新所有全宗的档案数量
     */
    @Override
    @Transactional
    public boolean updateAllFondsArchiveCount() {
        try {
            int result = fondsMapper.updateAllFondsArchiveCount();
            log.info("批量更新所有全宗档案数量成功，影响行数：{}", result);
            return result >= 0;
        } catch (Exception e) {
            log.error("批量更新所有全宗档案数量失败", e);
            return false;
        }
    }

    /**
     * 统计指定全宗下的档案数量
     */
    @Override
    public Long countArchivesByFondsId(Long fondsId) {
        if (fondsId == null) {
            return 0L;
        }
        try {
            Long count = fondsMapper.countArchivesByFondsId(fondsId);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("统计全宗[{}]档案数量失败", fondsId, e);
            return 0L;
        }
    }

    /**
     * 重写查询列表方法，添加自动更新档案数量
     */
    @Override
    public List<Fonds> selectFondsList(Fonds fonds) {
        // 先更新所有全宗的档案数量
        updateAllFondsArchiveCount();
        // 再查询列表
        return fondsMapper.selectFondsList(fonds);
    }

    // 获取每一个全宗的门类code
    // 修改全宗code为code+name
    @Override
    @Transactional
    public boolean changeCategoryCodesToCodeName() {
        // 获取所有全宗
        List<Fonds> fondsList = fondsMapper.selectFondsList(new Fonds());
        for (Fonds fonds : fondsList) {

            // 获取每一个全宗的code字段，用逗号分隔
            List<String> codeList = StringUtils.str2List(fonds.getCategoryCodes(), ",", true, true);
            String codeName = "";

            // 使用code查找门类名称，使用code-name组合成新的code
            for (String code : codeList) {
                String categoryName = archiveCategoryMapper.selectNameByCode(code);
                codeName += code + ":" + categoryName + ",";
            }
            // 更新全宗code
            fonds.setCategoryCodes(codeName);
            fondsMapper.updateFonds(fonds);
        }
        return true;
    }

}
