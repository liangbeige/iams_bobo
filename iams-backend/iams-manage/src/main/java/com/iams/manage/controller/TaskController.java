package com.iams.manage.controller;

import com.github.pagehelper.Page;
import com.iams.common.core.controller.BaseController;
import com.iams.common.core.domain.AjaxResult;
import com.iams.common.core.page.PageDomain;
import com.iams.common.core.page.TableDataInfo;
import com.iams.common.core.page.TableSupport;
import com.iams.manage.domain.TaskDTO.ActFormDataDTO;
import com.iams.manage.domain.TaskDTO.ArchiveTaskDTO;
import com.iams.manage.service.impl.TaskListImpl;
import org.activiti.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/task")
public class TaskController extends BaseController {


    @Autowired
    private TaskListImpl taskList;
    

    @Autowired
    private RuntimeService runtimeService;

    //获取我的代办任务
    @GetMapping(value = "/list")
    public TableDataInfo getTasks() {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Page<ArchiveTaskDTO> hashMaps = taskList.selectTaskList(pageDomain);

        return getDataTable(hashMaps);
    }

    private void deleteInstance()
    {
        String instanceId = "efccc38a-15f5-11f0-a3b5-68342170327c";
        runtimeService.deleteProcessInstance(instanceId, "no");
    }


    //渲染表单
    @GetMapping(value = "/formDataShow/{taskID}")
    public AjaxResult formDataShow(@PathVariable("taskID") String taskID) {

        return AjaxResult.success(taskList.formDataShow(taskID));
    }

    //保存表单
    @PostMapping(value = "/formDataSave/{taskID}")
    public AjaxResult formDataSave(@PathVariable("taskID") String taskID,
                                   @RequestBody List<ActFormDataDTO> formData ) throws ParseException {
        return toAjax(taskList.formDataSave(taskID, formData));
    }


    @GetMapping(value = "/historyFormDataShow/{taskID}")
    public AjaxResult historyFormDataShow(@PathVariable("taskID") String taskID) {

        return AjaxResult.success(taskList.historyFormDataShow(taskID));
    }



}
