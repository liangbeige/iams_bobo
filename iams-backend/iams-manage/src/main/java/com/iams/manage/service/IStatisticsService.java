package com.iams.manage.service;

import java.time.LocalDate;
import java.util.List;
import com.iams.manage.domain.Statistics;
import com.iams.manage.domain.StatisticsVO.DailyStatisticsVO;
import com.iams.manage.domain.StatisticsVO.MonthlyStatisticsVO;
import com.iams.manage.domain.StatisticsVO.StatisticsVO;

/**
 * 统计数据Service接口
 * 
 * @author LiuTao
 * @date 2025-04-04
 */
public interface IStatisticsService 
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


    public int insertStatistics();


    /**
     * 修改统计数据
     * 
     * @param statistics 统计数据
     * @return 结果
     */
    public int updateStatistics(Statistics statistics);

    /**
     * 批量删除统计数据
     * 
     * @param ids 需要删除的统计数据主键集合
     * @return 结果
     */
    public int deleteStatisticsByIds(Long[] ids);

    /**
     * 删除统计数据信息
     * 
     * @param id 统计数据主键
     * @return 结果
     */
    public int deleteStatisticsById(Long id);

    public Statistics selectLatestStatistics();

    DailyStatisticsVO getDailyStatistics(LocalDate localDate);

    MonthlyStatisticsVO getMonthlyStatistics(int year, int month);

    List<StatisticsVO> getRecentBorrowData(int days);

    StatisticsVO getLatestStatistics();
}
