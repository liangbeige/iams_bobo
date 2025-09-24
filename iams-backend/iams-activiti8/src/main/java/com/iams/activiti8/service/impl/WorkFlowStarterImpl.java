package com.iams.activiti8.service.impl;

import com.iams.activiti8.service.IWorkFlowStarter;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;

import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.engine.RuntimeService;

import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class WorkFlowStarterImpl implements IWorkFlowStarter {

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private RuntimeService runtimeService;


//    @Override
//    public String startProcessInstanceByDefinitionKey(String processDefinitionKey, String businessKey, String instanceName) {
//        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder
//                .start()
//                .withProcessDefinitionId(processDefinitionKey)
//                .withBusinessKey(businessKey)
//                .withName(instanceName)
//                .build());
//        return processInstance.getId();
//    }
    @Override
    public String startProcessInstanceByDefinitionKey(String processDefinitionKey, String businessKey, String instanceName) {

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
            processDefinitionKey,      // 流程定义 KEY
            businessKey,               // 业务键
            Collections.singletonMap("name", instanceName));
    return processInstance.getId();
}



    @Override
    public int deleteProcessInstance(String processInstanceId) {

        // 检查是否为在运行时的流程，如果是就删除，如果不是，捕捉异常不做处理
        try{
            runtimeService.deleteProcessInstance(processInstanceId, "删除实例");
        } catch (Exception e) {
            return 0;
        }
        return 1;
    }

}
