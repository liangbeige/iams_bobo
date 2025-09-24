package com.iams.manage.instener;

import com.iams.common.utils.DateUtils;
import com.iams.common.utils.spring.SpringUtils;

import com.iams.manage.domain.ArchiveLog;
import com.iams.manage.domain.PendingEvaluation;
import com.iams.manage.domain.workflow.IdentificationRecord;
import com.iams.manage.mapper.ArchiveLogMapper;

import com.iams.manage.mapper.IdentificationRecordMapper;
import com.iams.manage.mapper.PendingEvaluationMapper;
import com.iams.system.mapper.SysUserMapper;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.Expression;

public class ArchiveIdentificationStateListener implements ExecutionListener {
    private Expression state;
    @Override
    public void notify(DelegateExecution delegateExecution) {

        // 获取审批数据
        String businessKey = delegateExecution.getProcessInstanceBusinessKey();
//
//        IdentificationRecordMapper identificationRecordMapper = SpringUtils.getBean(IdentificationRecordMapper.class);
//        IdentificationRecord identificationRecord = identificationRecordMapper.selectIdentificationRecordByBusinessKey(businessKey);
//
//
//        // 更新状态
//        identificationRecord.setStatus(state.getValue(delegateExecution).toString());

//        identificationRecordMapper.updateIdentificationRecord(identificationRecord);
    }
}
