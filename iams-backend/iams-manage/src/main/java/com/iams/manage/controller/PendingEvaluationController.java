package com.iams.manage.controller;


import com.iams.common.core.controller.BaseController;
import com.iams.common.core.page.TableDataInfo;
import com.iams.common.utils.poi.ExcelUtil;
import com.iams.manage.domain.workflow.dto.PendingEvaluationDTO;
import com.iams.manage.service.IPendingEvaluationService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/task/pendingEvaluation")
public class PendingEvaluationController extends BaseController {

    @Autowired
    private IPendingEvaluationService pendingEvaluationService;


    @GetMapping("/list")
    public TableDataInfo list()
    {
        startPage();
        return getDataTable(pendingEvaluationService.selectPendingEvaluationList());
    }

    @PostMapping("/export")
    public void export(HttpServletResponse response)
    {
        List<PendingEvaluationDTO> list = pendingEvaluationService.selectPendingEvaluationList();
        ExcelUtil<PendingEvaluationDTO> util = new ExcelUtil<PendingEvaluationDTO>(PendingEvaluationDTO.class);
        util.exportExcel(response, list, "待审批列表");
    }

}
