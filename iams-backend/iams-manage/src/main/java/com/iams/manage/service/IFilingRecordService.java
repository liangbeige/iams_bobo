package com.iams.manage.service;

import java.util.List;
import com.iams.manage.domain.workflow.FilingRecord;
import com.iams.manage.domain.workflow.dto.FilingRecordDTO;

/**
 * 归档申请记录Service接口
 *
 * @author LiuTao
 * @date 2025-04-11
 */
public interface IFilingRecordService
{
    /**
     * 查询归档申请记录
     *
     * @param id 归档申请记录主键
     * @return 归档申请记录
     */
    public FilingRecord selectFilingRecordById(Long id);

    /**
     * 查询归档申请记录列表
     *
     * @param filingRecord 归档申请记录
     * @return 归档申请记录集合
     */
    public List<FilingRecordDTO> selectFilingRecordList(FilingRecord filingRecord);

    /**
     * 新增归档申请记录
     *
     * @param filingRecord 归档申请记录
     * @return 结果
     */
    public int insertFilingRecord(FilingRecordDTO filingRecordDTO);

    /**
     * 修改归档申请记录
     *
     * @param filingRecordDTO 归档申请记录
     * @return 结果
     */
    public int updateFilingRecord(FilingRecordDTO filingRecordDTO);

    /**
     * 批量删除归档申请记录
     *
     * @param ids 需要删除的归档申请记录主键集合
     * @return 结果
     */
    public int deleteFilingRecordByIds(Long[] ids);

    /**
     * 删除归档申请记录信息
     *
     * @param id 归档申请记录主键
     * @return 结果
     */
    public int deleteFilingRecordById(Long id);
}
