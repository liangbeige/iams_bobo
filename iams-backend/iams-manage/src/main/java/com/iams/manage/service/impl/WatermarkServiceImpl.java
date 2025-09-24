package com.iams.manage.service.impl;

import java.util.List;
import com.iams.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.iams.manage.mapper.WatermarkMapper;
import com.iams.manage.domain.Watermark;
import com.iams.manage.service.IWatermarkService;

/**
 * 水印管理Service业务层处理
 * 
 * @author zhjm
 * @date 2025-01-05
 */
@Service
public class WatermarkServiceImpl implements IWatermarkService 
{
    @Autowired
    private WatermarkMapper watermarkMapper;

    /**
     * 查询水印管理
     * 
     * @param id 水印管理主键
     * @return 水印管理
     */
    @Override
    public Watermark selectWatermarkById(Long id)
    {
        return watermarkMapper.selectWatermarkById(id);
    }

    /**
     * 查询水印管理列表
     * 
     * @param watermark 水印管理
     * @return 水印管理
     */
    @Override
    public List<Watermark> selectWatermarkList(Watermark watermark)
    {
        return watermarkMapper.selectWatermarkList(watermark);
    }

    /**
     * 新增水印管理
     * 
     * @param watermark 水印管理
     * @return 结果
     */
    @Override
    public int insertWatermark(Watermark watermark)
    {
        watermark.setCreateTime(DateUtils.getNowDate());
        return watermarkMapper.insertWatermark(watermark);
    }

    /**
     * 修改水印管理
     * 
     * @param watermark 水印管理
     * @return 结果
     */
    @Override
    public int updateWatermark(Watermark watermark)
    {
        watermark.setUpdateTime(DateUtils.getNowDate());
        return watermarkMapper.updateWatermark(watermark);
    }

    /**
     * 批量删除水印管理
     * 
     * @param ids 需要删除的水印管理主键
     * @return 结果
     */
    @Override
    public int deleteWatermarkByIds(Long[] ids)
    {
        return watermarkMapper.deleteWatermarkByIds(ids);
    }

    /**
     * 删除水印管理信息
     * 
     * @param id 水印管理主键
     * @return 结果
     */
    @Override
    public int deleteWatermarkById(Long id)
    {
        return watermarkMapper.deleteWatermarkById(id);
    }

    /**
     * 设置默认水印
     *
     * @param id 水印管理主键
     * @return 结果
     */
    @Override
    public int setDefault(Long id)
    {
        return watermarkMapper.setDefault(id);
    }

    /**
     * 获取默认水印
     *
     * @return 结果
     */
    @Override
    public Watermark selectDefaultWatermark()
    {
        return watermarkMapper.selectDefaultWatermark();
    }
}
