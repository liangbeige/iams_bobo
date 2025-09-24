package com.iams.manage.service.iot;

import com.iams.manage.domain.iot.GdlIpInfo;

import java.util.List;

/**
 * 固定列IP信息管理Service接口
 *
 * @author likang
 * @date 2025-04-18
 */
public interface IGdlIpInfoService
{
    /**
     * 查询固定列IP信息管理
     *
     * @param gdlNo 固定列IP信息管理主键
     * @return 固定列IP信息管理
     */
    public GdlIpInfo selectGdlIpInfoByGdlNo(String gdlNo);

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
     * 批量删除固定列IP信息管理
     *
     * @param gdlNos 需要删除的固定列IP信息管理主键集合
     * @return 结果
     */
    public int deleteGdlIpInfoByGdlNos(String[] gdlNos);

    /**
     * 删除固定列IP信息管理信息
     *
     * @param gdlNo 固定列IP信息管理主键
     * @return 结果
     */
    public int deleteGdlIpInfoByGdlNo(String gdlNo);
}
