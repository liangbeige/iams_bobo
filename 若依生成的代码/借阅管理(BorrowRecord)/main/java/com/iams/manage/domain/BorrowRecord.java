package com.iams.manage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;

/**
 * 借阅记录对象 tb_borrow_record
 * 
 * @author 张金明
 * @date 2025-01-15
 */
public class BorrowRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 借阅记录ID */
    @Excel(name = "借阅记录ID")
    private Long id;

    /** 借阅用户的 ID */
    @Excel(name = "借阅用户的 ID")
    private Long userId;

    /** 借阅用户的用户名 */
    @Excel(name = "借阅用户的用户名")
    private String userName;

    /** 借阅的档案ID */
    @Excel(name = "借阅的档案ID")
    private Long archiveId;

    /** 申请时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "申请时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date applyTime;

    /** 借阅开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "借阅开始日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date startDate;

    /** 借阅结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "借阅结束日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date endDate;

    /** 借阅目的 */
    @Excel(name = "借阅目的")
    private String purpose;

    /** 借阅状态 */
    @Excel(name = "借阅状态")
    private String status;

    /** 实际借出日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "实际借出日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date borrowDate;

    /** 实际归还日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "实际归还日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date returnDate;

    /** 逾期天数 */
    @Excel(name = "逾期天数")
    private Long overdueDays;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }
    public void setUserName(String userName) 
    {
        this.userName = userName;
    }

    public String getUserName() 
    {
        return userName;
    }
    public void setArchiveId(Long archiveId) 
    {
        this.archiveId = archiveId;
    }

    public Long getArchiveId() 
    {
        return archiveId;
    }
    public void setApplyTime(Date applyTime) 
    {
        this.applyTime = applyTime;
    }

    public Date getApplyTime() 
    {
        return applyTime;
    }
    public void setStartDate(Date startDate) 
    {
        this.startDate = startDate;
    }

    public Date getStartDate() 
    {
        return startDate;
    }
    public void setEndDate(Date endDate) 
    {
        this.endDate = endDate;
    }

    public Date getEndDate() 
    {
        return endDate;
    }
    public void setPurpose(String purpose) 
    {
        this.purpose = purpose;
    }

    public String getPurpose() 
    {
        return purpose;
    }
    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }
    public void setBorrowDate(Date borrowDate) 
    {
        this.borrowDate = borrowDate;
    }

    public Date getBorrowDate() 
    {
        return borrowDate;
    }
    public void setReturnDate(Date returnDate) 
    {
        this.returnDate = returnDate;
    }

    public Date getReturnDate() 
    {
        return returnDate;
    }
    public void setOverdueDays(Long overdueDays) 
    {
        this.overdueDays = overdueDays;
    }

    public Long getOverdueDays() 
    {
        return overdueDays;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("userId", getUserId())
            .append("userName", getUserName())
            .append("archiveId", getArchiveId())
            .append("applyTime", getApplyTime())
            .append("startDate", getStartDate())
            .append("endDate", getEndDate())
            .append("purpose", getPurpose())
            .append("status", getStatus())
            .append("borrowDate", getBorrowDate())
            .append("returnDate", getReturnDate())
            .append("overdueDays", getOverdueDays())
            .append("createTime", getCreateTime())
            .append("createBy", getCreateBy())
            .append("updateTime", getUpdateTime())
            .append("updateBy", getUpdateBy())
            .append("remark", getRemark())
            .toString();
    }
}
