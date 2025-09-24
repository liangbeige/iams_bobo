package com.iams.manage.domain.workflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iams.common.annotation.Excel;
import com.iams.manage.validator.ValidBorrowTime;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;


@Getter
@Setter
@ValidBorrowTime
public class BorrowRecordDTO extends CommonRecordBaseDTO {


    // 借阅起始时间
    @NotNull(message = "借阅时间开始不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "借阅时间开始")
    private Date startDate;

    @NotNull(message = "借阅时间结束不能为空")
    @Excel(name = "借阅时间结束")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;


    // 实际借出时间
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "借出时间")
    private Date borrowDate;

    // 实际归还时间
    @Excel(name = "归还时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date returnDate;

    @Excel(name = "逾期天数")
    private Long overdueDays;

    @Excel(name = "备注")
    private String remark;

}
