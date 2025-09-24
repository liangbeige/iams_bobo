package com.iams.manage.mapper;

import java.time.LocalDate;
import java.util.List;

import com.iams.manage.domain.TbStatistics;
import com.iams.manage.domain.Statistics;
import com.iams.manage.domain.StatisticsVO.DailyStatisticsVO;
import org.apache.ibatis.annotations.*;

/**
 * 统计数据Mapper接口
 *
 * @author LiuTao
 * @date 2025-04-04
 */
@Mapper
public interface StatisticsMapper
{
    /**
     * 查询统计数据
     *
     * @param id 统计数据主键
     * @return 统计数据
     */
    public Statistics selectStatisticsById(Long id);

    /**
     * 查询统计数据列表
     *
     * @param statistics 统计数据
     * @return 统计数据集合
     */
    public List<Statistics> selectStatisticsList(Statistics statistics);

    /**
     * 新增统计数据
     *
     * @param statistics 统计数据
     * @return 结果
     */
    public int insertStatistics(Statistics statistics);

    /**
     * 修改统计数据
     *
     * @param statistics 统计数据
     * @return 结果
     */
    public int updateStatistics(Statistics statistics);

    /**
     * 删除统计数据
     *
     * @param id 统计数据主键
     * @return 结果
     */
    public int deleteStatisticsById(Long id);

    /**
     * 批量删除统计数据
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteStatisticsByIds(Long[] ids);

    /**
     * 获取最新统计数据
     */
    public TbStatistics selectLatestStatistics();

    /**
     * 获取指定日期的24小时统计数据
     */
    public  List<TbStatistics> selectHourlyStatistics(@Param("date") LocalDate date);

    /**
     * 获取指定月份每天0点的统计数据
     */
    public List<TbStatistics> selectMonthlyZeroHourStatistics(
            @Param("year") int year,
            @Param("month") int month
    );

    /**
     * 获取最近几天每天0点的统计数据
     * 修复：使用正确的PostgreSQL语法
     */
    public List<TbStatistics> selectRecentZeroHourStatistics(@Param("days") int days);
}