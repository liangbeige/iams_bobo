package com.iams.manage.instener;


import com.iams.common.utils.DateUtils;
import com.iams.common.utils.spring.SpringUtils;
import com.iams.manage.domain.ArchiveLog;
import com.iams.manage.domain.workflow.BorrowRecord;
import com.iams.manage.mapper.ArchiveLogMapper;
import com.iams.manage.service.IBorrowRecordService;
import com.iams.system.mapper.SysUserMapper;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.Expression;

public class BorrowApprovalStateListener implements ExecutionListener{

    private Expression state;


    /**
     * 流程监听器：
     *     1、及时修改任务状态：批准（approved）、驳回（rejected）
     *  @param delegateExecution
     */
    @Override
    public void notify(DelegateExecution delegateExecution) {

        String businessKey = delegateExecution.getProcessInstanceBusinessKey();
        BorrowRecord borrowRecord = SpringUtils.getBean(IBorrowRecordService.class).selectBorrowRecordByBusinessKey(businessKey);
        // 更新申请状态
        borrowRecord.setStatus(state.getValue(delegateExecution).toString());
        SpringUtils.getBean(IBorrowRecordService.class).updateBorrowRecord(borrowRecord);
    }
}


