package com.iams.manage.instener;

import com.iams.common.utils.DateUtils;
import com.iams.common.utils.spring.SpringUtils;
import com.iams.manage.domain.Archive;
import com.iams.manage.domain.ArchiveLog;
import com.iams.manage.domain.workflow.DestroyRecord;
import com.iams.manage.mapper.ArchiveLogMapper;
import com.iams.manage.mapper.DestroyRecordMapper;
import com.iams.manage.service.IArchiveService;
import com.iams.system.mapper.SysUserMapper;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

import java.util.List;
/*
 * 正常批准状态下的结束事件，不针对status进行修改，这个属于前面的state监听的工作
 */
public class ArchiveDestroyEndListener implements ExecutionListener {

    @Override
    public void notify(DelegateExecution delegateExecution) {

        // 销毁流程终止事件，先拿record
        String businessKey = delegateExecution.getProcessInstanceBusinessKey();
        DestroyRecordMapper destroyRecordMapper = SpringUtils.getBean(DestroyRecordMapper.class);
//        DestroyRecord destroyRecord = destroyRecordMapper.selectBorrowRecordByBusinessKey(businessKey);

        // 添加档案日志
        SysUserMapper sysUserMapper = SpringUtils.getBean(SysUserMapper.class);
        ArchiveLogMapper archiveLogMapper = SpringUtils.getBean(ArchiveLogMapper.class);
        ArchiveLog archiveLog = new ArchiveLog();
        archiveLog.setTaskType("档案销毁");


        // 添加申请结束时间
//        destroyRecord.setEndApplyTime(DateUtils.getNowDate());
//        destroyRecordMapper.updateDestroyRecord(destroyRecord);

//        String status = destroyRecord.getStatus();
//        if(status.equals("已批准"))
        {
            // 更新档案状态为已销毁
            IArchiveService archiveService = SpringUtils.getBean(IArchiveService.class);
//            Archive archive = archiveService.selectArchiveById(destroyRecord.getArchiveId());
//            archive.setStatus("Destroyed");
            // 更新数据库
//            archiveService.updateArchive(archive);
            archiveLog.setTaskStatus("已批准");
        }
//        else if(status.contains("驳回"))
        {
            // 驳回只添加审批结束时间，此处不做处置
            archiveLog.setTaskStatus("已驳回");
        }


//        archiveLog.setInitiator(destroyRecord.getUserId()+": "+sysUserMapper.selectUserById(destroyRecord.getUserId()).getUserName());
//        archiveLog.setArchiveId(destroyRecord.getArchiveId());
//        archiveLog.setStartDate(destroyRecord.getStartApplyTime());
//        archiveLog.setEndDate(destroyRecord.getEndApplyTime());

        // 添加任务经办人
        // 获取任务服务和当前流程实例的所有任务
        GetTaskAssignee getTaskAssignee = SpringUtils.getBean(GetTaskAssignee.class);
        archiveLog.setHandler(getTaskAssignee.getTaskAssignee(businessKey));
        archiveLogMapper.insertArchiveLog(archiveLog);
    }
}
