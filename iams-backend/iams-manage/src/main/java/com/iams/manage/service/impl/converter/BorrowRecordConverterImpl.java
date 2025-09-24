package com.iams.manage.service.impl.converter;

import com.iams.manage.domain.Archive;
import com.iams.manage.domain.TaskDTO.ArchiveTaskDTO;
import com.iams.manage.domain.workflow.BorrowRecord;
import com.iams.manage.domain.workflow.dto.BorrowRecordDTO;
import com.iams.manage.mapper.ArchiveMapper;
import com.iams.manage.mapper.BorrowRecordMapper;
import com.iams.manage.service.IEntityDtoConverter;
import com.iams.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/*


 */

@Service
public class BorrowRecordConverterImpl implements IEntityDtoConverter<BorrowRecord, BorrowRecordDTO>
{

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private ArchiveMapper archiveMapper;

    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    @Override
    public BorrowRecordDTO entityToDto(BorrowRecord entity)
    {
        BorrowRecordDTO dto = new BorrowRecordDTO();

        if (entity == null){
            return null;
        }

        // 申请的ID
        dto.setId(entity.getId());

        // 用户信息
        dto.setUserId(entity.getUserId());
        dto.setUserName(sysUserMapper.selectUserById(entity.getUserId()).getUserName());
//        dto.setUserDepartment(sysUserMapper.selectUserById(entity.getUserId()).getDept().getDeptName());

//        // 档案信息
//        dto.setArchiveId(entity.getArchiveId());
//        dto.setArchiveName(archiveMapper.selectArchiveById(entity.getArchiveId()).getName());
//        dto.setArchiveDangHao(archiveMapper.selectArchiveById(entity.getArchiveId()).getDanghao());
        // 档案信息
        dto.setArchiveId(entity.getArchiveId());

        Archive archive = archiveMapper.selectArchiveById(entity.getArchiveId());
        if (archive != null) {
            dto.setArchiveName(archive.getName());
            dto.setArchiveDangHao(archive.getDanghao());
        } else {
            // 可以根据业务需求决定如何处理空的情况，比如设置为空字符串或默认值
            dto.setArchiveName(null); // 或者 "未知档案"
            dto.setArchiveDangHao(null); // 或者 "未知档号"
        }

        // 借阅时间范围
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());

        // 借阅原因 申请状态
        dto.setPurpose(entity.getPurpose());
        dto.setStatus(entity.getStatus());
        dto.setOverdueDays(entity.getOverdueDays());

        // 申请的时间范围
        dto.setStartApplyTime(entity.getStartApplyTime());
        dto.setEndApplyTime(entity.getEndApplyTime());


        dto.setBorrowDate(entity.getBorrowDate());
        dto.setReturnDate(entity.getReturnDate());

        // 以后可能通过该数据查看审批历史，要进行保留。
        dto.setInstanceId(entity.getInstanceId());
        dto.setBusinessKey(entity.getBusinessKey());
        dto.setRemark(entity.getRemark());
        dto.setProcessName(entity.getProcessName());
        return dto;
    }

    @Override
    public BorrowRecord dtoToEntity(BorrowRecordDTO dto)
    {


        BorrowRecord entity = new BorrowRecord();

        // 前端传来的ID要么为null，要么为具体的修改请求，那么就可以直接赋给实体
        if(dto.getId() != null)
            entity.setId(dto.getId());

        // 档案与人员ID
        entity.setUserId(dto.getUserId());
        entity.setArchiveId(dto.getArchiveId());

        // 借阅时间范围(计划借出)
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());

        // 借阅原因
        entity.setPurpose(dto.getPurpose());

        // 需要启动的流程名，非空注解
        entity.setProcessName(dto.getProcessName());

        // 备注也得写一下
        entity.setRemark(dto.getRemark());

        // DTO 没有实例ID，不能给实体赋值
        return entity;
    }


    @Override
    public ArchiveTaskDTO taskInfoCompletion(ArchiveTaskDTO archiveTaskDTO)
    {
        BorrowRecordDTO dto = entityToDto(borrowRecordMapper.selectBorrowRecordByBusinessKey(archiveTaskDTO.getBusinessKey()));

        if (dto == null)
            return null;
        archiveTaskDTO.setArchiveId(dto.getArchiveId());
        archiveTaskDTO.setArchiveName(dto.getArchiveName());
        archiveTaskDTO.setArchiveDangHao(dto.getArchiveDangHao());

        archiveTaskDTO.setApplyUserId(dto.getUserId());
        archiveTaskDTO.setApplyUserName(dto.getUserName());
        archiveTaskDTO.setApplyUserDepartment(dto.getUserDepartment());
        archiveTaskDTO.setPurpose(dto.getPurpose());

        archiveTaskDTO.setStartTime(dto.getStartDate());
        archiveTaskDTO.setEndTime(dto.getEndDate());

        archiveTaskDTO.setApplyStartTime(dto.getStartApplyTime());
        archiveTaskDTO.setApplyEndTime(dto.getEndApplyTime());


        archiveTaskDTO.setRemark(dto.getRemark());


        return archiveTaskDTO;
    }
}
