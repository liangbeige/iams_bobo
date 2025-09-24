package com.iams.manage.mapper;

import java.util.List;
import com.iams.manage.domain.workflow.FilingRecord;

/**
 * 归档申请记录Mapper接口
 *
 * @author LiuTao
 * @date 2025-04-11
 */
public interface FilingRecordMapper
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
    public List<FilingRecord> selectFilingRecordList(FilingRecord filingRecord);

    /**
     * 新增归档申请记录
     *
     * @param filingRecord 归档申请记录
     * @return 结果
     */
    public int insertFilingRecord(FilingRecord filingRecord);

    /**
     * 修改归档申请记录
     *
     * @param filingRecord 归档申请记录
     * @return 结果
     */
    public int updateFilingRecord(FilingRecord filingRecord);

    /**
     * 删除归档申请记录
     *
     * @param id 归档申请记录主键
     * @return 结果
     */
    public int deleteFilingRecordById(Long id);

    /**
     * 批量删除归档申请记录
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteFilingRecordByIds(Long[] ids);


    public FilingRecord selectFilingRecordByBusinessKey(String businessKey);
}
