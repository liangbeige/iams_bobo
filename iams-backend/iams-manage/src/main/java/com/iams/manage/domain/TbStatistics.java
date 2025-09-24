package com.iams.manage.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iams.common.annotation.Excel;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TbStatistics {
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate statDate;

    private Integer totalBorrows;
    private Integer totalReturns;
    private Integer totalLoans;
    private Integer totalLost;
    private Integer archivedCount;
    private Integer pendingCount;
    private Integer electronicCount;
    private Integer physicalCount;

    /** 库房档案柜列表 */
    @Excel(name = "库房档案柜列表")
    private String seriesData;

    /** 活跃用户 */
    @Excel(name = "活跃用户")
    private String activeUserList;

    /** 档案柜数量 */
    @Excel(name = "档案柜数量")
    private Long cabinetCount;

    /** 库房数量 */
    @Excel(name = "库房数量")
    private Long repositoryCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
