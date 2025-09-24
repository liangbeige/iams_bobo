package com.iams.manage.service.iot.impl;


import com.iams.manage.domain.iot.GdlIpInfo;
import com.iams.manage.mapper.iot.GdlIpInfoMapper;
import com.iams.manage.service.iot.IGdlIpInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 固定列IP信息管理Service业务层处理
 *
 * @author likang
 * @date 2025-04-18
 */
@Service
public class GdlIpInfoServiceImpl implements IGdlIpInfoService
{
    @Autowired
    private GdlIpInfoMapper gdlIpInfoMapper;

    /**
     * 查询固定列IP信息管理
     *
     * @param gdlNo 固定列IP信息管理主键
     * @return 固定列IP信息管理
     */
    @Override
    public GdlIpInfo selectGdlIpInfoByGdlNo(String gdlNo)
    {
        int gdlNoInt = Integer.parseInt(gdlNo);
        return gdlIpInfoMapper.selectGdlIpInfoByGdlNo(gdlNoInt);
    }

    /**
     * 查询固定列IP信息管理列表
     *
     * @param gdlIpInfo 固定列IP信息管理
     * @return 固定列IP信息管理
     */
    @Override
    public List<GdlIpInfo> selectGdlIpInfoList(GdlIpInfo gdlIpInfo)
    {
        return gdlIpInfoMapper.selectGdlIpInfoList(gdlIpInfo);
    }

    /**
     * 新增固定列IP信息管理
     *
     * @param gdlIpInfo 固定列IP信息管理
     * @return 结果
     */
    @Override
    public int insertGdlIpInfo(GdlIpInfo gdlIpInfo)
    {
        List<GdlIpInfo> gdlIpInfos = gdlIpInfoMapper.selectGdlIpInfoList(null);
        int findLastIndex = gdlIpInfos.size();
        gdlIpInfo.setGdlNo(findLastIndex + 1);
        return gdlIpInfoMapper.insertGdlIpInfo(gdlIpInfo);
    }

    /**
     * 修改固定列IP信息管理
     *
     * @param gdlIpInfo 固定列IP信息管理
     * @return 结果
     */
    @Override
    public int updateGdlIpInfo(GdlIpInfo gdlIpInfo)
    {
        return gdlIpInfoMapper.updateGdlIpInfo(gdlIpInfo);
    }

    /**
     * 批量删除固定列IP信息管理
     *
     * @param gdlNos 需要删除的固定列IP信息管理主键
     * @return 结果
     */
    @Override
    public int deleteGdlIpInfoByGdlNos(String[] gdlNos)
    {
        int[] gdlNosInt = new int[gdlNos.length];
        for (int i = 0; i < gdlNos.length; i++) {
            gdlNosInt[i] = Integer.parseInt(gdlNos[i]);
        }
        return gdlIpInfoMapper.deleteGdlIpInfoByGdlNos(gdlNosInt);
    }

    /**
     * 删除固定列IP信息管理信息
     *
     * @param gdlNo 固定列IP信息管理主键
     * @return 结果
     */
    @Override
    public int deleteGdlIpInfoByGdlNo(String gdlNo)
    {
        int gdlNoInt = Integer.parseInt(gdlNo);
        return gdlIpInfoMapper.deleteGdlIpInfoByGdlNo(gdlNoInt);
    }
}
