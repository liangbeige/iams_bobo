package com.iams.manage.domain.workflow.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.iams.common.annotation.Excel;
import com.iams.manage.validator.ValidArchiveInfo;
import com.iams.manage.validator.ValidUserInfo;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/*
四大流程的基类DTO
 */

@Getter
@Setter
//@ValidUserInfo(message = "用户ID、用户名和部门不一致")
//@ValidArchiveInfo(message = "档案ID、名称和档号不一致")
public class CommonRecordBaseDTO {

    @Excel(name = "申请ID")
    private Long id;

    // 用户信息
    @NotNull(message = "用户ID不能为空")
    @Excel(name = "用户ID")
    private Long userId;

    @Excel(name = "用户姓名")
    private String userName;

    @Excel(name = "用户部门")
    private String userDepartment;


    // 档案信息
    @NotNull(message = "档案ID不能为空")
    @Excel(name = "档案ID")
    private Long archiveId;

    @Excel(name = "档案名称")
    private String archiveName;

    @Excel(name = "档案档号")
    private String archiveDangHao;



    // 借阅申请的状态
    @Excel(name = "申请状态")
    private String status;

    // 借阅申请的原因
    @Excel(name = "借阅原因")
    @NotNull(message = "借阅原因不能为空")
    private String purpose;

    // 申请发起的时间与审批结束时间
    @Excel(name = "审批发起时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startApplyTime;


    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "审批结束时间")
    private Date endApplyTime;


    // 流程信息，前端拟发起的流程：borrowArchive1、borrowArchive2等
    @Excel(name = "流程名")
    @NotNull
    private String processName;

    // 流程实例ID，需要让用户查看历史审批结果，不做非空要求
    @Excel(name = "实例ID")
    private String instanceId;

    @Excel(name = "业务键")
    private String businessKey;

    @Excel(name = "备注")
    private String remark;
}
