package com.iams.manage.service;


import com.iams.manage.domain.TaskDTO.ArchiveTaskDTO;

/**
 * 针对activiti任务信息进行补全
 */
public interface ITaskInfoCompletion {
    public ArchiveTaskDTO taskInfoCompletion(ArchiveTaskDTO archiveTaskDTO);
}
