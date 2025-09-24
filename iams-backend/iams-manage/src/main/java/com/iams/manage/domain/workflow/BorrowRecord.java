package com.iams.manage.domain.workflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.Date;

@Getter
@Setter
public class BorrowRecord extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 借阅记录ID
     */
    private Long id;

    /**
     * 借阅用户的 ID
     */
    @Excel(name = "借阅用户的 ID")
    @NotNull
    private Long userId;


    /**
     * 借阅用户的用户名
     */
    @Excel(name = "借阅用户的用户名")
    @NotNull
    private String userName;


    /**
     * 借阅的档案ID
     */
    @Excel(name = "借阅的档案ID")
    @NotNull
    private Long archiveId;


    /**
     * 借阅的档案档号
     */
    @Excel(name = "借阅的档案档号")
    @NotNull
    private String archiveDanghao;

    /**
     * 借阅开始日期
     */
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "借阅开始日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date startDate;

    /**
     * 借阅结束日期
     */
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "借阅结束日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date endDate;

    /**
     * 借阅目的
     */
    @NotNull
    @Excel(name = "借阅目的")
    private String purpose;

    /**
     * 借阅状态
     */
    @Excel(name = "借阅状态")
    private String status;

    /**
     * 实际借出日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Excel(name = "实际借出日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date borrowDate;

    /**
     * 实际归还日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Excel(name = "实际归还日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date returnDate;

    /**
     * 逾期天数
     */
    @Excel(name = "逾期天数")
    private Long overdueDays;

    /**
     * 申请启动时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "申请启动时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date startApplyTime;

    /**
     * 申请结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "申请结束时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date endApplyTime;

    /**
     * 流程名
     */
    @Excel(name = "流程名")
    private String processName;

    /**
     * 业务键
     */
    @Excel(name = "业务键")
    private String businessKey;

    /**
     * 实例ID
     */
    @Excel(name = "实例ID")
    private String instanceId;


    public BorrowRecord() {
    }

    public BorrowRecord(String businessKey)
    {
        this.businessKey = businessKey;
    }

    public BorrowRecord(Long userId, Long archiveId)
    {
        this.userId = userId;
        this.archiveId = archiveId;
    }

    public BorrowRecord(String userName, String archiveDanghao)
    {
        this.userName = userName;
        this.archiveDanghao = archiveDanghao;
    }

    public BorrowRecord(Long userId)
    {
        this.userId = userId;
    }

    public BorrowRecord(Long userId, String status)
    {
        this.userId = userId;
        this.status = status;
    }
}