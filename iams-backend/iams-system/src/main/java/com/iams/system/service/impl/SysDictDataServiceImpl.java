package com.iams.system.service.impl;

import java.util.List;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.iams.common.utils.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.iams.common.core.domain.entity.SysDictData;
import com.iams.common.utils.DictUtils;
import com.iams.system.mapper.SysDictDataMapper;
import com.iams.system.service.ISysDictDataService;

/**
 * 字典 业务层处理
 * 
 * @author ruoyi
 */
@Service
public class SysDictDataServiceImpl implements ISysDictDataService
{
    private static final Logger log = LoggerFactory.getLogger(SysDictDataServiceImpl.class);

    @Autowired
    private SysDictDataMapper dictDataMapper;

    /**
     * 根据条件分页查询字典数据
     * 
     * @param dictData 字典数据信息
     * @return 字典数据集合信息
     */
    @Override
    public List<SysDictData> selectDictDataList(SysDictData dictData)
    {
        return dictDataMapper.selectDictDataList(dictData);
    }

    /**
     * 根据字典类型和字典键值查询字典数据信息
     * 
     * @param dictType 字典类型
     * @param dictValue 字典键值
     * @return 字典标签
     */
    @Override
    public String selectDictLabel(String dictType, String dictValue)
    {
        return dictDataMapper.selectDictLabel(dictType, dictValue);
    }

    /**
     * 根据字典数据ID查询信息
     * 
     * @param dictCode 字典数据ID
     * @return 字典数据
     */
    @Override
    public SysDictData selectDictDataById(Long dictCode)
    {
        return dictDataMapper.selectDictDataById(dictCode);
    }

    /**
     * 批量删除字典数据信息
     * 
     * @param dictCodes 需要删除的字典数据ID
     */
    @Override
    public void deleteDictDataByIds(Long[] dictCodes)
    {
        for (Long dictCode : dictCodes)
        {
            SysDictData data = selectDictDataById(dictCode);
            dictDataMapper.deleteDictDataById(dictCode);
            List<SysDictData> dictDatas = dictDataMapper.selectDictDataByType(data.getDictType());
            DictUtils.setDictCache(data.getDictType(), dictDatas);
        }
    }

    /**
     * 新增保存字典数据信息
     * 
     * @param data 字典数据信息
     * @return 结果
     */
    @Override
    public int insertDictData(SysDictData data)
    {
        int row = dictDataMapper.insertDictData(data);
        if (row > 0)
        {
            List<SysDictData> dictDatas = dictDataMapper.selectDictDataByType(data.getDictType());
            DictUtils.setDictCache(data.getDictType(), dictDatas);
        }
        return row;
    }

    /**
     * 修改保存字典数据信息
     * 
     * @param data 字典数据信息
     * @return 结果
     */
    @Override
    public int updateDictData(SysDictData data)
    {
        int row = dictDataMapper.updateDictData(data);
        if (row > 0)
        {
            List<SysDictData> dictDatas = dictDataMapper.selectDictDataByType(data.getDictType());
            DictUtils.setDictCache(data.getDictType(), dictDatas);
        }
        return row;
    }


    @Override
    public String importDictData(List<SysDictData> dictDataList, Boolean isUpdateSupport, String operName, String dictType)
    {
        if(StringUtils.isNull(dictDataList) || dictDataList.size() == 0)
        {
            throw new RuntimeException("导入数据不能为空！");
        }

        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for(SysDictData dictData : dictDataList)
        {
            if(!dictData.getDictType().equals(dictType))
            {
                failureNum ++;
                failureMsg.append("<br/>" + failureNum + "、字典编码 " + dictData.getDictCode() + " 导入失败：字典类型错误");
                continue;
            }

            try
            {

                SysDictData data = dictDataMapper.selectDictDataById(dictData.getDictCode());
                if (StringUtils.isNull(data))
                {
                    dictDataMapper.insertDictData(dictData);
                    successNum ++;
                    successMsg.append("<br/>" + successNum + "、字典编码 " + dictData.getDictCode() + " 导入成功");
                }
                else if(isUpdateSupport)
                {
                    dictDataMapper.updateDictData(dictData);
                    successNum ++;
                    successMsg.append("<br/>" + successNum + "、字典编码 " + dictData.getDictCode() + " 更新成功");
                }
                else
                {
                    failureNum ++;
                    failureMsg.append("<br/>" + failureNum + "、字典编码 " + dictData.getDictCode() + " 导入失败：字典编码已存在");
                }
            }catch (Exception e)
            {
                failureNum ++;
                String msg = "<br/>" + failureNum + "、字典编码 " + dictData.getDictCode() + " 导入失败：";
                failureMsg.append(msg + e.getMessage());
                log.error(msg, e);
            }
        }
        if(failureNum> 0)
        {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new RuntimeException(failureMsg.toString());
        }
        else
        {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }


        return successMsg.toString();
    }
}
