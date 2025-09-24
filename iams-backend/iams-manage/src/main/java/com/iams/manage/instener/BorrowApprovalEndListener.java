package com.iams.manage.instener;


import com.iams.common.utils.DateUtils;
import com.iams.common.utils.SecurityUtils;
import com.iams.common.utils.spring.SpringUtils;
import com.iams.manage.domain.Archive;
import com.iams.manage.domain.ArchiveLog;
import com.iams.manage.domain.workflow.BorrowRecord;
import com.iams.manage.mapper.ArchiveLogMapper;
import com.iams.manage.mapper.ArchiveMapper;
import com.iams.manage.service.ICabinetService;
import com.iams.manage.service.ICompartmentService;
import com.iams.system.mapper.SysUserMapper;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import com.iams.manage.service.IBorrowRecordService;

public class BorrowApprovalEndListener implements ExecutionListener{


    @Override
    public void notify(DelegateExecution delegateExecution){

        String businessKey = delegateExecution.getProcessInstanceBusinessKey();
        BorrowRecord borrowRecord = SpringUtils.getBean(IBorrowRecordService.class).selectBorrowRecordByBusinessKey(businessKey);
        String status = borrowRecord.getStatus();

        // 档案日志类
        ArchiveLogMapper archiveLogMapper = SpringUtils.getBean(ArchiveLogMapper.class);

        ArchiveMapper archiveMapper = SpringUtils.getBean(ArchiveMapper.class);
        Archive archive = archiveMapper.selectArchiveById(borrowRecord.getArchiveId());

        ArchiveLog archiveLog = new ArchiveLog();
        archiveLog.setTaskType("档案借阅");

        // 审批结束时间
        borrowRecord.setEndApplyTime(DateUtils.getNowDate());



        if(status.equals("已批准"))
        {
            // 批准状态，添加实际借出时间
            borrowRecord.setBorrowDate(DateUtils.getNowDate());
            archiveLog.setTaskStatus("已批准");

            archive.setAvailability(1);
            archiveMapper.updateArchive(archive);

            // 添加下架记录
            ArchiveLog xiajiaLog = new ArchiveLog();
            xiajiaLog.setTaskType("下架");
            xiajiaLog.setArchiveId(archive.getId());
            xiajiaLog.setTaskStatus(archive.getShitiLocation() + "-" + archive.getExactLocation());
            xiajiaLog.setInitiator(SecurityUtils.getUsername());
            xiajiaLog.setStartDate(DateUtils.getNowDate());
            xiajiaLog.setRemark("被借阅，自动下架");

            // 修改档案柜和储物格内档案数量
//            SpringUtils.getBean(ICabinetService.class).updateSize(archive.getShitiLocation(), "delete");
//            SpringUtils.getBean(ICompartmentService.class).updateSize(archive.getShitiLocation() + '-' + archive.getExactLocation(), "delete");
        }
        else if(status.contains("驳回"))
        {
            // 驳回只添加审批结束时间，此处不做处置
            archiveLog.setTaskStatus("已驳回");
        }


        SpringUtils.getBean(IBorrowRecordService.class).updateBorrowRecord(borrowRecord);



        // 借阅成功，添加借阅记录
        // 流程监听器我们只针对驳回添加档案日志，终止事件中只针对成功添加档案日志


        // 添加用户信息（名字）
        SysUserMapper sysUserMapper = SpringUtils.getBean(SysUserMapper.class);
        archiveLog.setInitiator(borrowRecord.getUserId().toString() + ":" + sysUserMapper.selectUserById(borrowRecord.getUserId()).getUserName());
        archiveLog.setArchiveId(borrowRecord.getArchiveId());


        archiveLog.setStartDate(borrowRecord.getBorrowDate());
        archiveLog.setEndDate(borrowRecord.getEndDate());

        // 添加任务经办人
        GetTaskAssignee getTaskAssignee = SpringUtils.getBean(GetTaskAssignee.class);
        archiveLog.setHandler(getTaskAssignee.getTaskAssignee(businessKey));
        archiveLogMapper.insertArchiveLog(archiveLog);
    }
}
