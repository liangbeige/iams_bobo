package com.iams.manage.mapper.iot;


import com.iams.manage.domain.iot.GdlIpInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 固定列IP信息管理Mapper接口
 *
 * @author likang
 * @date 2025-04-18
 */
@Mapper
public interface GdlIpInfoMapper
{
    /**
     * 查询固定列IP信息管理
     *
     * @param gdlNo 固定列IP信息管理主键
     * @return 固定列IP信息管理
     */
    public GdlIpInfo selectGdlIpInfoByGdlNo(int gdlNo);

    /**
     * 查询固定列IP信息管理列表
     *
     * @param gdlIpInfo 固定列IP信息管理
     * @return 固定列IP信息管理集合
     */
    public List<GdlIpInfo> selectGdlIpInfoList(GdlIpInfo gdlIpInfo);

    /**
     * 新增固定列IP信息管理
     *
     * @param gdlIpInfo 固定列IP信息管理
     * @return 结果
     */
    public int insertGdlIpInfo(GdlIpInfo gdlIpInfo);

    /**
     * 修改固定列IP信息管理
     *
     * @param gdlIpInfo 固定列IP信息管理
     * @return 结果
     */
    public int updateGdlIpInfo(GdlIpInfo gdlIpInfo);

    /**
     * 删除固定列IP信息管理
     *
     * @param gdlNo 固定列IP信息管理主键
     * @return 结果
     */
    public int deleteGdlIpInfoByGdlNo(int gdlNo);

    /**
     * 批量删除固定列IP信息管理
     *
     * @param gdlNos 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteGdlIpInfoByGdlNos(int[] gdlNos);
}
