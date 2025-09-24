package com.iams.manage.service;

import java.util.List;
import com.iams.manage.domain.Watermark;

/**
 * 水印管理Service接口
 * 
 * @author zhjm
 * @date 2025-01-05
 */
public interface IWatermarkService 
{
    /**
     * 查询水印管理
     * 
     * @param id 水印管理主键
     * @return 水印管理
     */
    public Watermark selectWatermarkById(Long id);

    /**
     * 查询水印管理列表
     * 
     * @param watermark 水印管理
     * @return 水印管理集合
     */
    public List<Watermark> selectWatermarkList(Watermark watermark);

    /**
     * 新增水印管理
     * 
     * @param watermark 水印管理
     * @return 结果
     */
    public int insertWatermark(Watermark watermark);

    /**
     * 修改水印管理
     * 
     * @param watermark 水印管理
     * @return 结果
     */
    public int updateWatermark(Watermark watermark);

    /**
     * 批量删除水印管理
     * 
     * @param ids 需要删除的水印管理主键集合
     * @return 结果
     */
    public int deleteWatermarkByIds(Long[] ids);

    /**
     * 删除水印管理信息
     * 
     * @param id 水印管理主键
     * @return 结果
     */
    public int deleteWatermarkById(Long id);

    /**
     * 设置默认水印
     *
     * @param id 水印管理主键
     * @return 结果
     */
    public int setDefault(Long id);

    /**
     * 获取默认水印
     *
     * @return 结果
     */
    public Watermark selectDefaultWatermark();
}
