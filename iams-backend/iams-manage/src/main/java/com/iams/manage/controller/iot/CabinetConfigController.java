package com.iams.manage.controller.iot;


import com.iams.common.core.domain.AjaxResult;
import com.iams.manage.domain.iot.CabinetConfig;
import com.iams.manage.service.iot.ICabinetConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 档案柜配置管理
 */
@RestController
@RequestMapping("/system/cabinetConfig")
public class CabinetConfigController {

    @Autowired
    private ICabinetConfigService cabinetConfigService;

    @PostMapping("/save")
    public AjaxResult saveConfig(@RequestParam String configData,
                                 @RequestParam String warehouse,
                                 @RequestParam String company,
                                 @RequestParam String scheme) {
        CabinetConfig config = new CabinetConfig();
        config.setConfigData(configData);
        config.setCompany(company);
        config.setWarehouse(warehouse);
        config.setScheme(scheme);
        int number = cabinetConfigService.selectCabinetByAttr(config);
        if (number != 0) {
            return AjaxResult.success(cabinetConfigService.updateCabinetConfig(config));
        }
        return AjaxResult.success(cabinetConfigService.insertCabinetConfig(config));
//        return AjaxResult.success(1);
    }

    @GetMapping("/list")
    public AjaxResult listConfig(CabinetConfig config) {
        return AjaxResult.success(cabinetConfigService.selectCabinetConfigList(config));
    }
}
