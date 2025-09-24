package com.iams.manage.domain.TaskDTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iams.activiti8.domain.dto.ActTaskDTO;
import com.iams.common.annotation.Excel;
import lombok.Getter;
import lombok.Setter;
import org.activiti.api.task.model.Task;
import org.activiti.engine.runtime.ProcessInstance;

import java.util.Date;

/*
这个类主要用于向前端待办事项中展示数据，成员属性会相对杂一些，但不影响数据库。

ActTskDTO
    包含流程信息：流程名+业务key+实例ID，
    包含任务信息：任务名+任务ID+任务状态+创建时间

ArchiveTaskDTO: 继承自 ActTaskDTO，
  新增：
    档案相关信息：档案ID+档案名 （前端可构建点击事件，查看具体的档案，所以放两个字段应该足够了？）
    任务发起人：发起人ID+发起人姓名+部门名（同理，相比档案多一个字段，直观展示部门名）
    原因/目的：原因/目的
    时间范围：开始时间+结束时间  （这一项比较特殊，借阅申请需要一个时间范围）
    申请流程时间：申请时间+审批时间
 */
@Getter
@Setter
public class ArchiveTaskDTO extends ActTaskDTO {

    private static final long serialVersionUID = 1L;

    @Excel(name = "档案ID")
    private Long archiveId;
    @Excel(name = "档案名")
    private String archiveName;
    @Excel(name = "档号")
    private String archiveDangHao;

    @Excel(name = "申请人ID")
    private Long applyUserId;
    @Excel(name = "申请人用户名")
    private String applyUserName;
    @Excel(name = "申请人部门")
    private String applyUserDepartment;
    @Excel(name = "申请原因")
    private String purpose;

    // 仅用于档案借阅申请，需要明确借阅期限，按照天计算
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "借阅开始时间")
    private Date startTime;
    @Excel(name = "借阅结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endTime;

    // 其他申请仅记录申请时间，需要非常详细的时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "申请启动时间")
    private Date applyStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "申请结束时间")
    private Date applyEndTime;

    public ArchiveTaskDTO() {
        super();
    }

    public ArchiveTaskDTO(Task task, ProcessInstance processInstance) {
        super(task, processInstance);
    }
}