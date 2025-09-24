package com.iams.manage.domain.StatisticsVO;

import lombok.Data;

import java.util.List;

@Data
public class MonthlyStatisticsVO {
    private Integer year;
    private Integer month;

    private List<DailyStatisticsVO> dailyData;
}