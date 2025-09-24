//package com.iams.manage.instener;
//
//import com.iams.common.utils.DateUtils;
//import com.iams.common.utils.spring.SpringUtils;
//import com.iams.manage.domain.Archive;
//import com.iams.manage.domain.ArchiveLog;
//import com.iams.manage.domain.PendingEvaluation;
//import com.iams.manage.mapper.ArchiveLogMapper;
//import com.iams.manage.mapper.ArchiveMapper;
//
//
//import com.iams.manage.mapper.IdentificationRecordMapper;
//import com.iams.manage.domain.workflow.IdentificationRecord;
//import com.iams.manage.mapper.PendingEvaluationMapper;
//import com.iams.system.mapper.SysUserMapper;
//import org.activiti.engine.delegate.ExecutionListener;
//import org.activiti.engine.delegate.DelegateExecution;
//
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//
//public class ArchiveIdentificationEndListener implements ExecutionListener {
//
//    @Override
//    public void notify(DelegateExecution delegateExecution)  {
//        String businessKey = delegateExecution.getProcessInstanceBusinessKey();
//
//        // 鉴定审批信息
//        IdentificationRecordMapper identificationRecordMapper = SpringUtils.getBean(IdentificationRecordMapper.class);
//        IdentificationRecord identificationRecord = identificationRecordMapper.selectIdentificationRecordByBusinessKey(businessKey);
//
//        // 对应档案信息
//        ArchiveMapper archiveMapper = SpringUtils.getBean(ArchiveMapper.class);
//        Archive archive = archiveMapper.selectArchiveById(identificationRecord.getArchiveId());
//
//        // 填写完成审批的时间，直接更新状态
//        identificationRecord.setEndApplyTime(DateUtils.getNowDate());
//        identificationRecordMapper.updateIdentificationRecord(identificationRecord);
//
//
//        // 从代办表中获取这一项
//        PendingEvaluationMapper pendingEvaluationMapper = SpringUtils.getBean(PendingEvaluationMapper.class);
//        PendingEvaluation pendingEvaluation = pendingEvaluationMapper.selectPendingEvaluationByArchiveId(archive.getId());
//        Long pendingEvaluation_id = pendingEvaluation.getId();
//
//        // 添加档案日志
//        SysUserMapper sysUserMapper = SpringUtils.getBean(SysUserMapper.class);
//        ArchiveLogMapper archiveLogMapper = SpringUtils.getBean(ArchiveLogMapper.class);
//        ArchiveLog archiveLog = new ArchiveLog();
//        archiveLog.setTaskType("档案鉴定");
//
//        // 如果是批准，那么就对档案进行操作
//        if(identificationRecord.getStatus().equals("已批准")) {
//
//            archive.setStatus("Reviewed");
//
//            if (identificationRecord.getPurpose().contains("保密期限")) {
//                if (identificationRecord.getHandleMethod().contains("降级")) {
//                    int cur_secret_level = Integer.parseInt(archive.getSecretLevel());
//                    if (cur_secret_level > 0) {
//                        archive.setSecretLevel(String.valueOf(cur_secret_level - 1));
//                    } else {
//                        archive.setSecretLevel("0");
//                    }
//                }
//
//                if (identificationRecord.getHandleMethod().contains("解密")) {
//                    archive.setSecretLevel("0");
//                }
//            } else if (identificationRecord.getPurpose().contains("保管期限")) {
//                if (identificationRecord.getHandleMethod().contains("续存")) {
//                    String handleMethod = identificationRecord.getHandleMethod();
//
//                    // 使用正则表达式提取“续存：XX年”中的数字部分（XX）
//                    Pattern pattern = Pattern.compile("续存：(\\d+)年");
//                    Matcher matcher = pattern.matcher(handleMethod);
//
//                    if (matcher.find()) {
//                        // 提取到的年份数字
//                        String yearsToAdd = matcher.group(1); // 这里获取的是XX
//
//                        // 将提取出的年份字符串转换为整数
//                        int additionalYears = Integer.parseInt(yearsToAdd);
//
//                        // 假设保存期限是一个Integer类型的字段
//                        int currentRetentionPeriod = Integer.parseInt(archive.getRetentionPeriod()); // 获取当前保存期限
//
//                        // 将提取的年份加到保存期限上
//                        archive.setRetentionPeriod(String.valueOf(currentRetentionPeriod + additionalYears));
//                    }
//                } else if (identificationRecord.getHandleMethod().contains("销毁")) {
//                    archive.setStatus("Destroyed");
//                }
//
//            }
//            archiveMapper.updateArchive(archive);
//            pendingEvaluationMapper.deletePendingEvaluationById(pendingEvaluation_id);
//            archiveLog.setTaskStatus("已批准");
//        }
//        else if(identificationRecord.getStatus().contains("驳回"))
//        {
//            // 驳回只添加审批结束时间，此处不做处置
//            // 修改代办信息表
//            pendingEvaluation.setStatus("申请被驳回，需要重新发起");
//            pendingEvaluationMapper.updatePendingEvaluation(pendingEvaluation);
//            archiveLog.setTaskStatus("已驳回");
//        }
//
//
//        archiveLog.setInitiator(identificationRecord.getUserId()+": "+ sysUserMapper.selectUserById(identificationRecord.getUserId()).getUserName());
//        archiveLog.setArchiveId(identificationRecord.getArchiveId());
//        archiveLog.setStartDate(identificationRecord.getStartApplyTime());
//        archiveLog.setEndDate(identificationRecord.getEndApplyTime());
//        GetTaskAssignee getTaskAssignee = SpringUtils.getBean(GetTaskAssignee.class);
//        archiveLog.setHandler(getTaskAssignee.getTaskAssignee(businessKey));
//        archiveLogMapper.insertArchiveLog(archiveLog);
//    }
//
//}
