package com.iams.manage.controller;


import com.iams.common.core.domain.AjaxResult;
import com.iams.common.utils.SecurityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(("/activiti"))
public class ActivitiFormPreFilledController {

    @GetMapping
    public AjaxResult getFormData() {
        AjaxResult ajax = AjaxResult.success();

        ajax.put("userId", SecurityUtils.getLoginUser().getUserId());
        ajax.put("userName", SecurityUtils.getLoginUser().getUsername());
//        ajax.put("deptName", SecurityUtils.getLoginUser().getUser().getDept().getDeptName());
        return ajax;
    }
}
