package com.iams.manage.service.impl.converter;

import com.iams.manage.domain.TaskDTO.ArchiveTaskDTO;
import com.iams.manage.domain.workflow.IdentificationRecord;
import com.iams.manage.domain.workflow.dto.IdentificationRecordDTO;
import com.iams.manage.mapper.ArchiveMapper;
import com.iams.manage.mapper.IdentificationRecordMapper;
import com.iams.manage.service.IEntityDtoConverter;
import com.iams.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IdentificationRecordConverterImpl implements IEntityDtoConverter<IdentificationRecord, IdentificationRecordDTO> {
    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private ArchiveMapper archiveMapper;

    @Autowired
    private IdentificationRecordMapper identificationRecordMapper;


    @Override
    public IdentificationRecordDTO entityToDto(IdentificationRecord entity) {
        IdentificationRecordDTO dto = new IdentificationRecordDTO();

//        dto.setId(entity.getId());

//        dto.setUserId(entity.getUserId());
//        dto.setUserName(sysUserMapper.selectUserById(entity.getUserId()).getUserName());
//        dto.setUserDepartment(sysUserMapper.selectUserById(entity.getUserId()).getDept().getDeptName());
//
//        dto.setArchiveId(entity.getArchiveId());
//        dto.setArchiveName(archiveMapper.selectArchiveById(entity.getArchiveId()).getName());
//        dto.setArchiveDangHao(archiveMapper.selectArchiveById(entity.getArchiveId()).getDanghao());
//
//        dto.setPurpose(entity.getPurpose());
//        dto.setHandleMethod(entity.getHandleMethod());
//
//        dto.setStatus(entity.getStatus());
//
//
//        dto.setStartApplyTime(entity.getStartApplyTime());
//        dto.setEndApplyTime(entity.getEndApplyTime());
//
//        dto.setInstanceId(entity.getInstanceId());
//        dto.setRemark(entity.getRemark());

        return dto;
    }


    @Override
    public IdentificationRecord dtoToEntity(IdentificationRecordDTO dto) {
        IdentificationRecord entity = new IdentificationRecord();

//        entity.setUserId(dto.getUserId());
//        entity.setArchiveId(dto.getArchiveId());
//        entity.setPurpose(dto.getPurpose());
//        entity.setHandleMethod(dto.getHandleMethod());
//
//        entity.setProcessName(dto.getProcessName());
//        entity.setRemark(dto.getRemark());

        return entity;
    }

    @Override
    public ArchiveTaskDTO taskInfoCompletion(ArchiveTaskDTO archiveTaskDTO){

//        IdentificationRecordDTO dto = entityToDto(identificationRecordMapper.selectIdentificationRecordByBusinessKey(archiveTaskDTO.getBusinessKey()));
//        archiveTaskDTO.setArchiveId(dto.getArchiveId());
//        archiveTaskDTO.setArchiveName(dto.getArchiveName());
//        archiveTaskDTO.setArchiveDangHao(dto.getArchiveDangHao());
//
//        archiveTaskDTO.setApplyUserId(dto.getUserId());
//        archiveTaskDTO.setApplyUserName(dto.getUserName());
//        archiveTaskDTO.setApplyUserDepartment(dto.getUserDepartment());
//        archiveTaskDTO.setPurpose(dto.getPurpose()+":"+dto.getHandleMethod());
//
//        archiveTaskDTO.setApplyStartTime(dto.getStartApplyTime());
//        archiveTaskDTO.setApplyEndTime(dto.getEndApplyTime());
//
//        archiveTaskDTO.setRemark(dto.getRemark());
        return archiveTaskDTO;


    }

}
