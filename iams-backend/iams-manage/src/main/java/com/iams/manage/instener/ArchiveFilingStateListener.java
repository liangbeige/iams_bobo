package com.iams.manage.instener;

import cn.hutool.core.io.unit.DataUnit;
import com.iams.common.utils.DateUtils;
import com.iams.common.utils.spring.SpringUtils;

import com.iams.manage.domain.ArchiveLog;

import com.iams.manage.domain.workflow.FilingRecord;
import com.iams.manage.mapper.ArchiveLogMapper;
import com.iams.manage.mapper.FilingRecordMapper;
import com.iams.system.mapper.SysUserMapper;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.Expression;


public class ArchiveFilingStateListener implements ExecutionListener {


    private Expression state;
    @Override
    public void notify(DelegateExecution delegateExecution){
        String businessKey = delegateExecution.getProcessInstanceBusinessKey();

        FilingRecordMapper filingRecordMapper = SpringUtils.getBean(FilingRecordMapper.class);

        FilingRecord filingRecord = filingRecordMapper.selectFilingRecordByBusinessKey(businessKey);

        // 修改申请状态
        filingRecord.setStatus(state.getValue(delegateExecution).toString());

        // 如果是批准需要再加一点东西：比如结束时间
        // 批准的日志会在终止事件中添加
        if(state.getValue(delegateExecution).toString().equals("已批准"))
        {
            //archiveFilingApproval.setEndTime(DateUtils.getNowDate());
            filingRecord.setEndApplyTime(DateUtils.getNowDate());
        }
        // 如果是驳回需要再加一点东西：比如结束时间 与 档案日志
        else if(state.getValue(delegateExecution).toString().contains("驳回"))
        {
            filingRecord.setEndApplyTime(DateUtils.getNowDate());

            ArchiveLogMapper archiveLogMapper = SpringUtils.getBean(ArchiveLogMapper.class);
            ArchiveLog archiveLog = new ArchiveLog();


            SysUserMapper sysUserMapper = SpringUtils.getBean(SysUserMapper.class);

            archiveLog.setInitiator(filingRecord.getUserId()+": "+sysUserMapper.selectUserById(filingRecord.getUserId()).getUserName());
            archiveLog.setArchiveId(filingRecord.getArchiveId());
            archiveLog.setTaskType("档案归档");
            archiveLog.setTaskStatus("已驳回");
            archiveLog.setStartDate(filingRecord.getStartApplyTime());
            archiveLog.setEndDate(filingRecord.getEndApplyTime());

            // 添加任务经办人
            GetTaskAssignee getTaskAssignee = SpringUtils.getBean(GetTaskAssignee.class);
            archiveLog.setHandler(getTaskAssignee.getTaskAssignee(businessKey));
            archiveLogMapper.insertArchiveLog(archiveLog);

            archiveLogMapper.insertArchiveLog(archiveLog);
        }

        // 调用接口更新
        filingRecordMapper.updateFilingRecord(filingRecord);
    }
}
