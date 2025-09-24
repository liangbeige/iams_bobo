package com.iams.manage.service.impl.converter;

import com.iams.manage.domain.TaskDTO.ArchiveTaskDTO;
import com.iams.manage.domain.workflow.FilingRecord;
import com.iams.manage.domain.workflow.dto.FilingRecordDTO;
import com.iams.manage.mapper.ArchiveMapper;
import com.iams.manage.mapper.FilingRecordMapper;
import com.iams.manage.service.IEntityDtoConverter;
import com.iams.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FilingRecordConverterImpl implements IEntityDtoConverter<FilingRecord, FilingRecordDTO> {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private ArchiveMapper archiveMapper;

    @Autowired
    private FilingRecordMapper filingRecordMapper;

    @Override
    public FilingRecordDTO entityToDto(FilingRecord entity){
        FilingRecordDTO dto = new FilingRecordDTO();


        dto.setId(entity.getId());

        dto.setUserId(entity.getUserId());
        dto.setUserName(sysUserMapper.selectUserById(entity.getUserId()).getUserName());
        dto.setUserDepartment(sysUserMapper.selectUserById(entity.getUserId()).getDept().getDeptName());

        dto.setArchiveId(entity.getArchiveId());
        dto.setArchiveName(archiveMapper.selectArchiveById(entity.getArchiveId()).getName());
        dto.setArchiveDangHao(archiveMapper.selectArchiveById(entity.getArchiveId()).getDanghao());


        dto.setPurpose(entity.getPurpose());
        dto.setStatus(entity.getStatus());

        dto.setStartApplyTime(entity.getStartApplyTime());
        dto.setEndApplyTime(entity.getEndApplyTime());

        dto.setInstanceId(entity.getInstanceId());
        dto.setRemark(entity.getRemark());
        return dto;
    }

    @Override
    public FilingRecord dtoToEntity(FilingRecordDTO dto){
        FilingRecord entity = new FilingRecord();

        entity.setUserId(dto.getUserId());
        entity.setArchiveId(dto.getArchiveId());
        entity.setPurpose(dto.getPurpose());


        entity.setProcessName(dto.getProcessName());

        entity.setRemark(dto.getRemark());

        return entity;
    }


    @Override
    public ArchiveTaskDTO taskInfoCompletion(ArchiveTaskDTO archiveTaskDTO)
    {
        FilingRecordDTO dto = entityToDto(filingRecordMapper.selectFilingRecordByBusinessKey(archiveTaskDTO.getBusinessKey()));
        archiveTaskDTO.setArchiveId(dto.getArchiveId());
        archiveTaskDTO.setArchiveName(dto.getArchiveName());
        archiveTaskDTO.setArchiveDangHao(dto.getArchiveDangHao());

        archiveTaskDTO.setApplyUserId(dto.getUserId());
        archiveTaskDTO.setApplyUserName(dto.getUserName());
        archiveTaskDTO.setApplyUserDepartment(dto.getUserDepartment());
        archiveTaskDTO.setPurpose(dto.getPurpose());

        archiveTaskDTO.setApplyStartTime(dto.getStartApplyTime());
        archiveTaskDTO.setApplyEndTime(dto.getEndApplyTime());

        archiveTaskDTO.setRemark(dto.getRemark());

        return archiveTaskDTO;
    }
}
