package com.iams.manage.instener;

import com.iams.common.utils.DateUtils;
import com.iams.common.utils.spring.SpringUtils;
import com.iams.manage.domain.ArchiveLog;
import com.iams.manage.domain.workflow.DestroyRecord;
import com.iams.manage.mapper.ArchiveLogMapper;
import com.iams.manage.mapper.DestroyRecordMapper;
import com.iams.system.mapper.SysUserMapper;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.Expression;

public class ArchiveDestroyStateListener implements ExecutionListener {

    private Expression state;
    @Override
    public void notify(DelegateExecution delegateExecution) {

        String businessKey = delegateExecution.getProcessInstanceBusinessKey();

        DestroyRecordMapper archiveDestroyApprovalMapper = SpringUtils.getBean(DestroyRecordMapper.class);

//        DestroyRecord destroyRecord = archiveDestroyApprovalMapper.selectBorrowRecordByBusinessKey(businessKey);

//        destroyRecord.setStatus(state.getValue(delegateExecution).toString());
//
//        archiveDestroyApprovalMapper.updateDestroyRecord(destroyRecord);
    }
}
