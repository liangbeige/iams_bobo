package com.iams.manage.service;

import com.iams.manage.domain.Compartment;

import java.util.List;

public interface ICompartmentService {

    List<Compartment> selectCompartmentList(Compartment compartment);

    Compartment selectCompartmentById(Long id);

    int insertCompartment(Compartment compartment);

    int updateCompartment(Compartment compartment);

    int deleteCompartmentById(Long id);

    int deleteCompartmentByIds(String[] ids);

    List<Compartment> listByCabinetIds(String[] cabinetIds);

    List<Compartment> listCompartment(Compartment compartment);

    int updateSize(String fullLocation, String option);

    Compartment selectCompartmentByLocation(Long repositoryId, int leNo, int divNo);
}
