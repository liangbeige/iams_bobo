package com.iams.manage.mapper;

import com.iams.manage.domain.Compartment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CompartmentMapper {
    List<Compartment> selectCompartmentList(Compartment compartment);

    Compartment selectCompartmentById(Long id);

    int insertCompartment(Compartment compartment);

    int updateCompartment(Compartment compartment);

    int deleteCompartmentById(Long id);

    int deleteCompartmentByIds(String[] ids);

    List<Compartment> listByCabinetIds(@Param("array") Long[] cabinetIds);

    Compartment selectCompartmentByLocation(Long cabinetId, int leNo, int divNo);

    List<Compartment> getUsedCompartment();
}
