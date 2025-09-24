package com.iams.manage.domain.StatisticsVO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iams.manage.domain.TbStatistics;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class DailyStatisticsVO {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate statDate;

    private List<TbStatistics> hourlyData;

    // 基础数据（用于单条记录）
    private Integer totalBorrows;
    private Integer totalReturns;
    private Integer totalLoans;
    private Integer totalLost;
    private Integer archivedCount;
    private Integer pendingCount;
    private Integer electronicCount;
    private Integer physicalCount;
}