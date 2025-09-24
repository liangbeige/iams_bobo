package com.iams.activiti8.service;

/*

将流程启动抽象为一个类，方便以后扩展
 */
public interface IWorkFlowStarter {
    public String startProcessInstanceByDefinitionKey(String processDefinitionKey, String businessKey, String instanceName);

    public int deleteProcessInstance(String processInstanceId);
}
