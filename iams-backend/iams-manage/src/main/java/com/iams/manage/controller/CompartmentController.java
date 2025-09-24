package com.iams.manage.controller;

import com.iams.common.core.domain.AjaxResult;
import com.iams.manage.domain.Compartment;
import com.iams.manage.service.ICompartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/manage/compartment")
public class CompartmentController {

    @Autowired
    private ICompartmentService compartmentService;

    @RequestMapping("/list-by-cabinetIds")
    public AjaxResult listByCabinetIds(@RequestParam String cabinetIds) {
        String[] ids = cabinetIds.split(",");
        List<Compartment> compartments;
        try {
            compartments = compartmentService.listByCabinetIds(ids);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return AjaxResult.error("未找到");
        }
        return AjaxResult.success(compartments);
    }

    @RequestMapping("/list")
    public AjaxResult list(Compartment compartment) {
        return AjaxResult.success(compartmentService.listCompartment(compartment));
    }

    @RequestMapping("/size")
    public AjaxResult updateSize(@RequestParam String location, @RequestParam String option) {
        return AjaxResult.success(compartmentService.updateSize(location, option));
    }
}
