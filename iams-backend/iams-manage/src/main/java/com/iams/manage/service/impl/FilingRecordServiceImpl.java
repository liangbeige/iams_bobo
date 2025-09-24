package com.iams.manage.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.iams.activiti8.service.impl.WorkFlowStarterImpl;
import com.iams.common.utils.DateUtils;
import com.iams.common.utils.uuid.UUID;
import com.iams.manage.domain.workflow.dto.FilingRecordDTO;
import com.iams.manage.service.impl.converter.FilingRecordConverterImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.iams.manage.mapper.FilingRecordMapper;
import com.iams.manage.domain.workflow.FilingRecord;
import com.iams.manage.service.IFilingRecordService;

/**
 * 归档申请记录Service业务层处理
 *
 * @author LiuTao
 * @date 2025-04-11
 */
@Service
public class FilingRecordServiceImpl implements IFilingRecordService
{
    @Autowired
    private FilingRecordMapper filingRecordMapper;


    @Autowired
    private FilingRecordConverterImpl filingRecordConverterImpl;

    @Autowired
    private WorkFlowStarterImpl workFlowStarter;

    /**
     * 查询归档申请记录
     *
     * @param id 归档申请记录主键
     * @return 归档申请记录
     */
    @Override
    public FilingRecord selectFilingRecordById(Long id)
    {
        return filingRecordMapper.selectFilingRecordById(id);
    }

    /**
     * 查询归档申请记录列表
     *
     * @param filingRecord 归档申请记录
     * @return 归档申请记录
     */
    @Override
    public List<FilingRecordDTO> selectFilingRecordList(FilingRecord filingRecord)
    {
        List<FilingRecord> filingRecordList = filingRecordMapper.selectFilingRecordList(filingRecord);

        return filingRecordList.stream()
                .map(filingRecordEntity -> filingRecordConverterImpl.entityToDto(filingRecordEntity))
                .collect(Collectors.toList());
    }

    /**
     * 新增归档申请记录
     *
     * @param filingRecordDTO 归档申请记录
     * @return 结果
     */
    @Override
    public int insertFilingRecord(FilingRecordDTO filingRecordDTO)
    {
        List<FilingRecord> filingRecordList = filingRecordMapper.selectFilingRecordList(new FilingRecord(filingRecordDTO.getUserId(), filingRecordDTO.getArchiveId()));
        for(FilingRecord filingRecordEntity : filingRecordList)
        {
            if(filingRecordEntity.getStatus().equals("已批准")) {
                throw new RuntimeException("该档案已被归档，请勿重复归档");
            }
        }
        // 传入DTO，转换成实体
        FilingRecord filingRecord = filingRecordConverterImpl.dtoToEntity(filingRecordDTO);
        filingRecord.setBusinessKey(UUID.randomUUID().toString());

        String instanceId = workFlowStarter.startProcessInstanceByDefinitionKey(filingRecord.getProcessName(), filingRecord.getBusinessKey(), filingRecordDTO.getUserName()+"归档申请");

        filingRecord.setInstanceId(instanceId);
        filingRecord.setStartApplyTime(DateUtils.getNowDate());
        filingRecord.setStatus("待审批");

        return filingRecordMapper.insertFilingRecord(filingRecord);
    }

    /**
     * 修改归档申请记录
     *
     * @param filingRecordDTO 归档申请记录
     * @return 结果
     */
    @Override
    public int updateFilingRecord(FilingRecordDTO filingRecordDTO)
    {
        FilingRecord filingRecord = filingRecordConverterImpl.dtoToEntity(filingRecordDTO);
        return filingRecordMapper.updateFilingRecord(filingRecord);
    }

    /**
     * 批量删除归档申请记录
     *
     * @param ids 需要删除的归档申请记录主键
     * @return 结果
     */
    @Override
    public int deleteFilingRecordByIds(Long[] ids)
    {
        return filingRecordMapper.deleteFilingRecordByIds(ids);
    }

    /**
     * 删除归档申请记录信息
     *
     * @param id 归档申请记录主键
     * @return 结果
     */
    @Override
    public int deleteFilingRecordById(Long id)
    {
        return filingRecordMapper.deleteFilingRecordById(id);
    }
}
