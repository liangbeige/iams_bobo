package com.iams.manage.instener;

import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetTaskAssignee {

    @Autowired
    private HistoryService historyService;
    public String getTaskAssignee(String businessKey) {

        List<HistoricTaskInstance> historicTasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceBusinessKey(businessKey)
                .orderByTaskCreateTime()
                .asc()
                .list();

        StringBuilder assignees = new StringBuilder();

        for (HistoricTaskInstance historicTask : historicTasks) {
            String assignee = historicTask.getAssignee();
            if (assignee != null) {
                assignees.append(assignee).append(", ");
            }
        }
        return assignees.toString();
    }
}
