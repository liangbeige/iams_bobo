package com.iams.manage.service;

import java.util.List;
import java.util.Map;
import com.iams.manage.domain.workflow.IdentificationRecord;
import com.iams.manage.domain.workflow.dto.IdentificationRecordDTO;

/**
 * 档案鉴定记录Service接口
 *
 * @author LiuTao
 * @date 2025-06-26
 */
public interface IIdentificationRecordService {

    /**
     * 查询鉴定记录
     *
     * @param id 鉴定记录主键
     * @return 鉴定记录
     */
    public IdentificationRecord selectIdentificationRecordById(Long id);

    /**
     * 查询鉴定记录列表
     *
     * @param identificationRecord 鉴定记录
     * @return 鉴定记录集合
     */
    public List<IdentificationRecord> selectIdentificationRecordList(IdentificationRecord identificationRecord);

    /**
     * 提交档案鉴定（支持批量）
     *
     * @param identificationRecordDTO 鉴定记录DTO
     * @return 结果
     */
    public int submitIdentificationRecord(IdentificationRecordDTO identificationRecordDTO);

    /**
     * 修改鉴定记录
     *
     * @param identificationRecord 鉴定记录
     * @return 结果
     */
    public int updateIdentificationRecord(IdentificationRecord identificationRecord);

    /**
     * 批量删除鉴定记录
     *
     * @param ids 需要删除的鉴定记录主键
     * @return 结果
     */
    public int deleteIdentificationRecordByIds(Long[] ids);

    /**
     * 删除鉴定记录信息
     *
     * @param id 鉴定记录主键
     * @return 结果
     */
    public int deleteIdentificationRecordById(Long id);

    /**
     * 根据档案ID查询鉴定记录
     *
     * @param archiveId 档案ID
     * @return 鉴定记录集合
     */
    public List<IdentificationRecord> selectIdentificationRecordByArchiveId(Long archiveId);

    /**
     * 获取鉴定统计数据
     *
     * @param params 查询参数
     * @return 统计数据
     */
    public Map<String, Object> getIdentificationStatistics(Map<String, Object> params);
}