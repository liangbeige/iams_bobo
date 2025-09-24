package com.iams.manage.service.impl;

import com.github.pagehelper.Page;
import com.iams.activiti8.domain.FormTemplates;
import com.iams.activiti8.service.IFormTemplatesService;
import com.iams.common.core.page.PageDomain;
import com.iams.manage.domain.TaskDTO.ActFormDataDTO;
import com.iams.manage.domain.TaskDTO.ArchiveTaskDTO;
import com.iams.manage.service.ITaskList;
import com.iams.manage.service.impl.converter.TaskInfoCompletionImpl;

import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskListImpl implements ITaskList {


    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private TaskInfoCompletionImpl taskInfoCompletion;

    @Autowired
    private IFormTemplatesService formTemplatesService;


    @Override
    public Page<ArchiveTaskDTO> selectTaskList(PageDomain pageDomain) {
        Page<ArchiveTaskDTO> list = new Page<>();

        org.activiti.api.runtime.shared.query.Page<Task> pageTasks = taskRuntime.tasks(Pageable.of((pageDomain.getPageNum() - 1) * pageDomain.getPageSize(), pageDomain.getPageSize()));
        List<Task> tasks = pageTasks.getContent();
        int totalItems = pageTasks.getTotalItems();
        list.setTotal(totalItems);
        if (totalItems != 0) {
            Set<String> processInstanceIdIds = tasks.parallelStream().map(Task::getProcessInstanceId).collect(Collectors.toSet());
            List<ProcessInstance> processInstanceList = runtimeService.createProcessInstanceQuery().processInstanceIds(processInstanceIdIds).list();

            List<ArchiveTaskDTO> actTaskDTOS = tasks.stream()
                    .map(t -> new ArchiveTaskDTO(t, processInstanceList.parallelStream().filter(pi -> t.getProcessInstanceId().equals(pi.getId())).findAny().get()))
                    .toList();
            List<ArchiveTaskDTO> collect = actTaskDTOS.stream().map(taskInfoCompletion::taskInfoCompletion).toList();

            list.addAll(collect);
        }
        return list;
    }


    @Override
    public List<String> formDataShow(String taskID) {

//        Task task = taskRuntime.task(taskID);
//        String formKey = task.getFormKey();
//
//        FormTemplates formTemplates = formTemplatesService.selectFormTemplatesFormKey(formKey);
//
        List<String> formFields = new ArrayList<>();

        formFields.add("zhan wei fu");
        formFields.add("zhan wei fu");
        return formFields;
    }


    @Override
    public int formDataSave(String taskID, List<ActFormDataDTO> awfs) throws ParseException {
        Task task = taskRuntime.task(taskID);

        Map<String, Object> variables = new HashMap<>();

        for(ActFormDataDTO awf : awfs){
            if("i".equals(awf.getControlIsParam())){
                variables.put(awf.getControlId() , awf.getControlValue());
            }
        }

        if(task.getAssignee() == null)
        {
            taskRuntime.claim(TaskPayloadBuilder.claim()
                    .withTaskId(task.getId())
                    .build());
        }
        runtimeService.setVariables(task.getProcessInstanceId(), variables);

        taskRuntime.complete(TaskPayloadBuilder.complete()
                .withTaskId(taskID)
                .withVariables(variables)
                .build());
        return 1;
    }


    @Override
    public List<Map<String, Object>> historyFormDataShow(String taskID){

        String processInstanceId = taskRuntime.task(taskID).getProcessInstanceId();

        List<HistoricTaskInstance> historicTasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByTaskCreateTime()
                .asc()
                .list();

        // 2. 获取每个任务的审批信息
        List<Map<String, Object>> taskHistoryList = new ArrayList<>();

        for (HistoricTaskInstance historicTask : historicTasks) {

            Map<String, Object> taskHistory = new HashMap<>();
            // 基本任务信息
            taskHistory.put("taskId", historicTask.getId());
            taskHistory.put("taskName", historicTask.getName());
            taskHistory.put("assignee", historicTask.getAssignee());
            taskHistory.put("startTime", historicTask.getStartTime());
            taskHistory.put("endTime", historicTask.getEndTime());

            // 获取该任务的变量
            List<HistoricVariableInstance> taskVariables = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .taskId(historicTask.getId())
                    .list();

            Map<String, Object> variables = new HashMap<>();
            for (HistoricVariableInstance variable : taskVariables) {
                variables.put(variable.getVariableName(), variable.getValue());
            }

            // 添加审批结果和意见
            if (variables.containsKey("approvalResult")) {
                taskHistory.put("approvalResult", variables.get("approvalResult"));
            }
            if (variables.containsKey("approvalComment")) {
                taskHistory.put("approvalComment", variables.get("approvalComment"));
            }

            taskHistoryList.add(taskHistory);
        }

        return taskHistoryList;
    }

    @Override
    public List<String> getCandidateUsers(String businessKey) {

        List<String> candidateUsers = new ArrayList<>();

        ProcessInstance processInstances = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(businessKey).singleResult();
        org.activiti.engine.task.Task task = taskService.createTaskQuery().processInstanceId(processInstances.getId()).singleResult();
        List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
        for (IdentityLink identityLink : identityLinks)
        {
            candidateUsers.add(identityLink.getGroupId());
        }
        return candidateUsers;
    }

}
