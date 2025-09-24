package com.iams.manage.instener;

import com.iams.common.utils.spring.SpringUtils;
import com.iams.manage.domain.Archive;
import com.iams.manage.domain.ArchiveLog;
import com.iams.manage.domain.workflow.FilingRecord;
import com.iams.manage.mapper.ArchiveLogMapper;
import com.iams.manage.mapper.ArchiveMapper;
import com.iams.manage.mapper.FilingRecordMapper;
import com.iams.system.mapper.SysUserMapper;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

import java.util.List;

public class ArchiveFilingEndListener implements ExecutionListener {

    @Override
    public void notify(DelegateExecution delegateExecution) {

        String businessKey = delegateExecution.getProcessInstanceBusinessKey();

        FilingRecordMapper filingRecordMapper = SpringUtils.getBean(FilingRecordMapper.class);
        FilingRecord filingRecord = filingRecordMapper.selectFilingRecordByBusinessKey(businessKey);


        ArchiveMapper archiveMapper = SpringUtils.getBean(ArchiveMapper.class);
        Archive archive = archiveMapper.selectArchiveById(filingRecord.getArchiveId());
        // 终止事件——修改档案状态为已归档

        archive.setStatus("Archived");
        archiveMapper.updateArchive(archive);

        SysUserMapper sysUserMapper = SpringUtils.getBean(SysUserMapper.class);

        // 添加档案日志——已批准的记录
        ArchiveLogMapper archiveLogMapper = SpringUtils.getBean(ArchiveLogMapper.class);
        ArchiveLog archiveLog = new ArchiveLog();
        archiveLog.setInitiator(filingRecord.getUserId()+": "+ sysUserMapper.selectUserById(filingRecord.getUserId()).getUserName());
        archiveLog.setArchiveId(filingRecord.getArchiveId());
        archiveLog.setTaskType("档案归档");
        archiveLog.setTaskStatus("已批准");
        archiveLog.setStartDate(filingRecord.getStartApplyTime());
        archiveLog.setEndDate(filingRecord.getEndApplyTime());

        // 添加任务经办人
        GetTaskAssignee getTaskAssignee = SpringUtils.getBean(GetTaskAssignee.class);
        archiveLog.setHandler(getTaskAssignee.getTaskAssignee(businessKey));

        archiveLogMapper.insertArchiveLog(archiveLog);
    }
}
