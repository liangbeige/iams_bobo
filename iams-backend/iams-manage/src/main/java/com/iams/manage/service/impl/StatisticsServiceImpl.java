package com.iams.manage.service.impl;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.iams.common.utils.DateUtils;
import com.iams.manage.domain.TbStatistics;
import com.iams.manage.domain.Archive;
import com.iams.manage.domain.StatisticsVO.DailyStatisticsVO;
import com.iams.manage.domain.StatisticsVO.MonthlyStatisticsVO;
import com.iams.manage.domain.StatisticsVO.StatisticsVO;
import com.iams.manage.domain.StatisticsVO.UserCount;
import com.iams.manage.domain.workflow.BorrowRecord;
import com.iams.manage.mapper.ArchiveMapper;
import com.iams.manage.mapper.BorrowRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.iams.manage.mapper.StatisticsMapper;
import com.iams.manage.domain.Statistics;
import com.iams.manage.service.IStatisticsService;

/**
 * 统计数据Service业务层处理
 * 
 * @author LiuTao
 * @date 2025-04-04
 */
@Service
public class StatisticsServiceImpl implements IStatisticsService 
{
    @Autowired
    private StatisticsMapper statisticsMapper;

    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    @Autowired
    private ArchiveMapper archiveMapper;



    /**
     * 查询统计数据
     * 
     * @param id 统计数据主键
     * @return 统计数据
     */
    @Override
    public Statistics selectStatisticsById(Long id)
    {
        return statisticsMapper.selectStatisticsById(id);
    }

    /**
     * 查询统计数据列表
     * 需要根据前端的需求进行部分筛选
     * @param statistics 统计数据
     * @return 统计数据
     */
    @Override
    public List<Statistics> selectStatisticsList(Statistics statistics)
    {
        return statisticsMapper.selectStatisticsList(statistics);
    }

    /**
     * 新增统计数据
     * 
     * @param statistics 统计数据
     * @return 结果
     */
    @Override
    public int insertStatistics(Statistics statistics)
    {
        return statisticsMapper.insertStatistics(statistics);
    }


    /**
     * 新增统计数据
     *
     *
     * @return 结果
     */
    @Override
    @Scheduled(cron = "0 0 * * * ?")
    public int insertStatistics()
    {
        Statistics statistics = new Statistics();
        List<BorrowRecord> borrowRecords = borrowRecordMapper.selectBorrowRecordList(new BorrowRecord());
        List<Archive> archives = archiveMapper.selectArchiveList(new Archive());
        Long totalBorrows = Long.valueOf(borrowRecords.size());
        // 依据status的不同分别计算总数
        // 总借出 总归还 逾期未还
        Long totalReturns = 0L;
        Long totalLoans = 0L;
        Long totalOverdue = 0L;

        // 借阅信息统计
        for(BorrowRecord borrowRecord : borrowRecords)
        {
            switch (borrowRecord.getStatus())
                {
                    case "已批准":
                        if (borrowRecord.getRemark() == null || !borrowRecord.getRemark().contains("电子借阅"))
                        {
                            totalLoans++;
                        }
                        break;
                    case "已逾期":
                        totalOverdue++;
                        break;
                    case "已结束":
                        if (borrowRecord.getRemark() == null || !borrowRecord.getRemark().contains("电子借阅"))
                        {
                            totalReturns++;
                        }
                        break;
                }
        }
        statistics.setTotalReturns(totalReturns);
        statistics.setTotalLoans(totalLoans);
        statistics.setTotalLost(totalOverdue);
        statistics.setTotalBorrows(totalBorrows);

        statistics.setStatDate(DateUtils.getNowDate());

        // 档案信息统计
        // 归档情况
        // 实体与电子档数量情况
        Long totalArchived = 0L;
        Long eleArchive = 0L;
        Long phyArchive = 0L;
        Long cabinetCount = 0L;
        for(Archive archive : archives)
        {
            if(archive.getStatus().equals("Archived"))
            {
                totalArchived++;
            }
            if (archive.getExactLocation() != null && !archive.getExactLocation().isEmpty())
            {
                cabinetCount++;
            }
            switch (archive.getCarrierType())
            {
                case "electronic":
                    eleArchive++;
                    break;
                case "tangible":
                    phyArchive++;
                    break;
                case "Mixture":
                    phyArchive++;
                    eleArchive++;
                    break;
            }
        }
        statistics.setArchivedCount(totalArchived);
        statistics.setTotalLost(0L);
        statistics.setActiveUserList("[{\"count\": 820, \"skuId\": \"0\", \"amount\": 0, \"skuName\": \"用户1\"}, {\"count\": 762, \"skuId\": \"0\", \"amount\": 0, \"skuName\": \"用户2\"}, {\"count\": 749, \"skuId\": \"0\", \"amount\": 0, \"skuName\": \"用户3\"}, {\"count\": 742, \"skuId\": \"0\", \"amount\": 0, \"skuName\": \"用户4\"}, {\"count\": 718, \"skuId\": \"0\", \"amount\": 0, \"skuName\": \"用户5\"}, {\"count\": 714, \"skuId\": \"0\", \"amount\": 0, \"skuName\": \"用户6\"}, {\"count\": 700, \"skuId\": \"0\", \"amount\": 0, \"skuName\": \"用户7\"}, {\"count\": 673, \"skuId\": \"0\", \"amount\": 0, \"skuName\": \"用户20\"}, {\"count\": 422, \"skuId\": \"0\", \"amount\": 0, \"skuName\": \"用户120\"}, {\"count\": 348, \"skuId\": \"0\", \"amount\": 0, \"skuName\": \"用户652\"}]");
        statistics.setPendingCount(Long.valueOf(archives.size()) - totalArchived);
        statistics.setElectronicCount(eleArchive);
        statistics.setPhysicalCount(phyArchive);
        statistics.setCabinetCount(cabinetCount);
        statistics.setCreatedAt(DateUtils.getNowDate());

        //statistics.setTotalReturns(Long.valueOf(borrowRecordMapper.selectBorrowRecordList(new BorrowRecord()).size()));
        return statisticsMapper.insertStatistics(statistics);
    }


    /**
     * 修改统计数据
     * 
     * @param statistics 统计数据
     * @return 结果
     */
    @Override
    public int updateStatistics(Statistics statistics)
    {
        return statisticsMapper.updateStatistics(statistics);
    }

    /**
     * 批量删除统计数据
     * 
     * @param ids 需要删除的统计数据主键
     * @return 结果
     */
    @Override
    public int deleteStatisticsByIds(Long[] ids)
    {
        return statisticsMapper.deleteStatisticsByIds(ids);
    }

    /**
     * 删除统计数据信息
     * 
     * @param id 统计数据主键
     * @return 结果
     */
    @Override
    public int deleteStatisticsById(Long id)
    {
        return statisticsMapper.deleteStatisticsById(id);
    }

    @Override
    public Statistics selectLatestStatistics()
    {
//        return statisticsMapper.selectLatestStatistics();
        return null;
    }

    /**
     * 获取最新统计数据
     */
    public StatisticsVO getLatestStatistics() {
        TbStatistics latest = statisticsMapper.selectLatestStatistics();
        return convertToStatisticsVO(latest);
    }

    /**
     * 获取日统计数据 - 显示当天24小时档案在架数量情况
     */
    public DailyStatisticsVO getDailyStatistics(LocalDate date) {
        // 获取指定日期的所有小时统计数据
        List<TbStatistics> hourlyData = statisticsMapper.selectHourlyStatistics(date);

        DailyStatisticsVO result = new DailyStatisticsVO();
        result.setStatDate(date);
        result.setHourlyData(hourlyData);

        return result;
    }

    /**
     * 获取月统计数据 - 显示当月每天0点的档案在架数量情况
     */
    public MonthlyStatisticsVO getMonthlyStatistics(int year, int month) {
        // 获取指定月份每天0点的统计数据
        List<TbStatistics> dailyData = statisticsMapper.selectMonthlyZeroHourStatistics(year, month);

        MonthlyStatisticsVO result = new MonthlyStatisticsVO();
        result.setYear(year);
        result.setMonth(month);

        // 转换为VO格式
        List<DailyStatisticsVO> dailyVOList = dailyData.stream()
                .map(this::convertToDailyStatisticsVO)
                .collect(Collectors.toList());

        result.setDailyData(dailyVOList);

        return result;
    }

    /**
     * 获取最近几天的借出数量数据
     */
    public List<StatisticsVO> getRecentBorrowData(int days) {
        // 获取最近几天每天0点的统计数据
        List<TbStatistics> recentData = statisticsMapper.selectRecentZeroHourStatistics(days);

        return recentData.stream()
                .map(this::convertToStatisticsVO)
                .collect(Collectors.toList());
    }

    /**
     * 转换为统计VO
     */
    private StatisticsVO convertToStatisticsVO(TbStatistics statistics) {
        if (statistics == null) {
            return new StatisticsVO();
        }

        StatisticsVO vo = new StatisticsVO();
        vo.setId(statistics.getId());
        vo.setStatDate(statistics.getStatDate());
        vo.setTotalBorrows(statistics.getTotalBorrows());
        vo.setTotalReturns(statistics.getTotalReturns());
        vo.setTotalLoans(statistics.getTotalLoans());
        vo.setTotalLost(statistics.getTotalLost());
        vo.setArchivedCount(statistics.getArchivedCount());
        vo.setPendingCount(statistics.getPendingCount());
        vo.setElectronicCount(statistics.getElectronicCount());
        vo.setPhysicalCount(statistics.getPhysicalCount());
        vo.setCreatedAt(statistics.getCreatedAt());
        vo.setCabinetCount(statistics.getCabinetCount());

        return vo;
    }

    /**
     * 转换为日统计VO
     */
    private DailyStatisticsVO convertToDailyStatisticsVO(TbStatistics statistics) {
        DailyStatisticsVO vo = new DailyStatisticsVO();
        vo.setStatDate(statistics.getStatDate());
        vo.setTotalBorrows(statistics.getTotalBorrows());
        vo.setTotalReturns(statistics.getTotalReturns());
        vo.setTotalLoans(statistics.getTotalLoans());
        vo.setTotalLost(statistics.getTotalLost());
        vo.setArchivedCount(statistics.getArchivedCount());
        vo.setPendingCount(statistics.getPendingCount());
        vo.setElectronicCount(statistics.getElectronicCount());
        vo.setPhysicalCount(statistics.getPhysicalCount());

        return vo;
    }
}
