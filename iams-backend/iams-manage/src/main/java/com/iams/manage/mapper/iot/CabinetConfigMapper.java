package com.iams.manage.mapper.iot;


import com.iams.manage.domain.iot.CabinetConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CabinetConfigMapper {
    int insertCabinetConfig(CabinetConfig cabinetConfig);

    List<CabinetConfig> selectCabinetConfigList(CabinetConfig cabinetConfig);

    int selectCabinetByAttr(CabinetConfig cabinetConfig);

    int updateCabinetConfig(CabinetConfig cabinetConfig);
}
