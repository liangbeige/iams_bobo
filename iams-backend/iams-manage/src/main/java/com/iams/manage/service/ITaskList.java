package com.iams.manage.service;

import com.github.pagehelper.Page;
import com.iams.activiti8.domain.dto.ActWorkflowFormDataDTO;
import com.iams.common.core.page.PageDomain;
import com.iams.manage.domain.TaskDTO.ActFormDataDTO;
import com.iams.manage.domain.TaskDTO.ArchiveTaskDTO;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ITaskList {

    public Page<ArchiveTaskDTO> selectTaskList(PageDomain pageDomain);

    public List<String> formDataShow(String taskID);

    public int formDataSave(String taskID, List<ActFormDataDTO> awfs) throws ParseException;

    public List<Map<String, Object>> historyFormDataShow(String taskID);

    public List<String> getCandidateUsers(String businessKey);
}
