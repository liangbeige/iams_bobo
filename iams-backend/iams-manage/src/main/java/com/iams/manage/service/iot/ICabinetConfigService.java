package com.iams.manage.service.iot;

import com.iams.manage.domain.iot.CabinetConfig;

import java.util.List;

public interface ICabinetConfigService {
    int insertCabinetConfig(CabinetConfig cabinetConfig);

    List<CabinetConfig> selectCabinetConfigList(CabinetConfig cabinetConfig);

    int selectCabinetByAttr(CabinetConfig cabinetConfig);

    int updateCabinetConfig(CabinetConfig cabinetConfig);
}
