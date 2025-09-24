package com.iams.manage.service.impl.converter;

import com.iams.manage.domain.TaskDTO.ArchiveTaskDTO;
import com.iams.manage.domain.workflow.DestroyRecord;
import com.iams.manage.domain.workflow.dto.DestroyRecordDTO;
import com.iams.manage.mapper.ArchiveMapper;
import com.iams.manage.mapper.DestroyRecordMapper;
import com.iams.manage.service.IEntityDtoConverter;
import com.iams.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DestroyRecordConverterImpl implements IEntityDtoConverter<DestroyRecord, DestroyRecordDTO> {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private ArchiveMapper archiveMapper;

    @Autowired
    private DestroyRecordMapper destroyRecordMapper;


    @Override
    public DestroyRecordDTO entityToDto(DestroyRecord entity) {
        DestroyRecordDTO dto = new DestroyRecordDTO();
        // 档案销毁记录ID
        dto.setId(entity.getId());
        // 用户信息
        dto.setUserId(entity.getUserId());
        dto.setUserName(sysUserMapper.selectUserById(entity.getUserId()).getUserName());
        dto.setUserDepartment(sysUserMapper.selectUserById(entity.getUserId()).getDept().getDeptName());

        // 档案信息
        dto.setArchiveId(entity.getArchiveId());
        dto.setArchiveName(archiveMapper.selectArchiveById(entity.getArchiveId()).getName());
        dto.setArchiveDangHao(archiveMapper.selectArchiveById(entity.getArchiveId()).getDanghao());

        dto.setPurpose(entity.getPurpose());
        dto.setStatus(entity.getStatus());

        dto.setStartApplyTime(entity.getStartApplyTime());
        dto.setEndApplyTime(entity.getEndApplyTime());

//        dto.setInstanceId(entity.getInstanceId());
//        dto.setRemark(entity.getRemark());

        return dto;
    }

    /*
    主要用于 流程实例启动时，将流程实例信息写入数据库中
     */
    @Override
    public DestroyRecord dtoToEntity(DestroyRecordDTO dto) {
        DestroyRecord entity = new DestroyRecord();


        if(dto.getId() != null)
            entity.setId(dto.getId());

        entity.setUserId(dto.getUserId());
        entity.setArchiveId(dto.getArchiveId());
        entity.setPurpose(dto.getPurpose());

//        entity.setProcessName(dto.getProcessName());
        // 备注也得写一下
//        entity.setRemark(dto.getRemark());

        return entity;
    }

    @Override
    public ArchiveTaskDTO taskInfoCompletion(ArchiveTaskDTO archiveTaskDTO)
    {
//        DestroyRecordDTO dto = entityToDto(destroyRecordMapper.selectBorrowRecordByBusinessKey(archiveTaskDTO.getBusinessKey()));
//        archiveTaskDTO.setArchiveId(dto.getArchiveId());
//        archiveTaskDTO.setArchiveName(dto.getArchiveName());
//        archiveTaskDTO.setArchiveDangHao(dto.getArchiveDangHao());
//
//        archiveTaskDTO.setApplyUserId(dto.getUserId());
//        archiveTaskDTO.setApplyUserName(dto.getUserName());
//        archiveTaskDTO.setApplyUserDepartment(dto.getUserDepartment());
//        archiveTaskDTO.setPurpose(dto.getPurpose());
//
//        archiveTaskDTO.setApplyStartTime(dto.getStartApplyTime());
//        archiveTaskDTO.setApplyEndTime(dto.getEndApplyTime());

//        archiveTaskDTO.setRemark(dto.getRemark());

        return archiveTaskDTO;
    }


}
