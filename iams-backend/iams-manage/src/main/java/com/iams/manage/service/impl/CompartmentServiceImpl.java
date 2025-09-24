package com.iams.manage.service.impl;

import com.iams.manage.domain.Cabinet;
import com.iams.manage.domain.Compartment;
import com.iams.manage.mapper.CompartmentMapper;
import com.iams.manage.service.ICompartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class CompartmentServiceImpl implements ICompartmentService {

    @Autowired
    private CompartmentMapper compartmentMapper;

    @Autowired
    private CabinetServiceImpl cabinetService;

    @Override
    public List<Compartment> selectCompartmentList(Compartment compartment) {

        return compartmentMapper.selectCompartmentList(compartment);
    }

    @Override
    public Compartment selectCompartmentById(Long id) {
        return compartmentMapper.selectCompartmentById(id);
    }

    @Override
    public int insertCompartment(Compartment compartment) {
        return compartmentMapper.insertCompartment(compartment);
    }

    @Override
    public int updateCompartment(Compartment compartment) {
        return compartmentMapper.updateCompartment(compartment);
    }

    @Override
    public int deleteCompartmentById(Long id) {
        return compartmentMapper.deleteCompartmentById(id);
    }

    @Override
    public int deleteCompartmentByIds(String[] ids) {
        return compartmentMapper.deleteCompartmentByIds(ids);
    }

    public List<Compartment> listByCabinetIds(String[] cabinetIds) {
        Long[] ids = Arrays.stream(cabinetIds)
                .map(Long::valueOf)
                .toArray(Long[]::new);
        return compartmentMapper.listByCabinetIds(ids);
    }

    @Override
    public List<Compartment> listCompartment(Compartment compartment) {
        return compartmentMapper.selectCompartmentList(compartment);
    }

    @Override
    public int updateSize(String fullLocation, String option) {
        try {
            // 解析字符串
            String[] parts = fullLocation.split("-");

            Integer quNo = Integer.parseInt(parts[0]);
            Integer colNo = Integer.parseInt(parts[1]);
            String zyNo = parts[2];
            int leNo = Integer.parseInt(parts[3]);
            int divNo = Integer.parseInt(parts[4]);

            // 查询档案柜
            Cabinet cabinet = cabinetService.selectCabinetByLocation(10000L, quNo, colNo, zyNo);
            if (cabinet == null) {
                return 0;
            }

            Compartment compartment = selectCompartmentByLocation(cabinet.getId(), leNo, divNo);

            // 更新 size 字段
            if (option.equals("add")) {
                compartment.setSize(compartment.getSize() + 1);
            } else if (option.equals("delete")) {
                compartment.setSize(compartment.getSize() - 1);
            } else {
                return 0;
            }
            int rows = updateCompartment(compartment);
            return Math.max(rows, 0);
        } catch (Exception e) {
//            return error("Error updating cabinet size: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public Compartment selectCompartmentByLocation(Long cabinetId, int leNo, int divNo) {
        return compartmentMapper.selectCompartmentByLocation(cabinetId, leNo, divNo);
    }


}
