package com.iams.manage.domain.StatisticsVO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StatisticsVO {
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate statDate;

    private Integer totalBorrows;
    private Integer totalReturns;
    private Integer totalLoans;
    private Integer totalLost;
    private Integer archivedCount;
    private Integer pendingCount;
    private Integer electronicCount;
    private Integer physicalCount;
    private Long cabinetCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

}