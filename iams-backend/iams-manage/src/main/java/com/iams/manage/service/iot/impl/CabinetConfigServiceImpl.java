package com.iams.manage.service.iot.impl;

// import com.ruoyi.system.domain.CabinetConfig;
// import com.ruoyi.system.mapper.CabinetConfigMapper;
// import com.ruoyi.system.service.ICabinetConfigService;
import com.iams.manage.domain.iot.CabinetConfig;
import com.iams.manage.mapper.iot.CabinetConfigMapper;
import com.iams.manage.service.iot.ICabinetConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CabinetConfigServiceImpl implements ICabinetConfigService {

    @Autowired
    private CabinetConfigMapper cabinetConfigMapper;

    @Override
    public int insertCabinetConfig(CabinetConfig cabinetConfig) {
        return cabinetConfigMapper.insertCabinetConfig(cabinetConfig);
    }

    @Override
    public List<CabinetConfig> selectCabinetConfigList(CabinetConfig cabinetConfig) {
        return cabinetConfigMapper.selectCabinetConfigList(cabinetConfig);
    }

    @Override
    public int selectCabinetByAttr(CabinetConfig cabinetConfig) {
        return cabinetConfigMapper.selectCabinetByAttr(cabinetConfig);
    }

    @Override
    public int updateCabinetConfig(CabinetConfig cabinetConfig) {
        return cabinetConfigMapper.updateCabinetConfig(cabinetConfig);
    }
}
