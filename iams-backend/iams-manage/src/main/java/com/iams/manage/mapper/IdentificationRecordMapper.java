package com.iams.manage.mapper;

import java.util.List;
import java.util.Map;

import com.iams.manage.domain.workflow.IdentificationRecord;
import com.iams.manage.domain.workflow.dto.IdentificationRecordDTO;
import org.apache.ibatis.annotations.Param;

/**
 * 档案鉴定记录Mapper接口
 *
 * @author LiuTao
 * @date 2025-06-26
 */
public interface IdentificationRecordMapper {

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
     * 根据档案ID查询鉴定记录
     *
     * @param archiveId 档案ID
     * @return 鉴定记录集合
     */
    public List<IdentificationRecord> selectIdentificationRecordByArchiveId(Long archiveId);

    /**
     * 新增鉴定记录
     *
     * @param identificationRecord 鉴定记录
     * @return 结果
     */
    public int insertIdentificationRecord(IdentificationRecord identificationRecord);

    /**
     * 修改鉴定记录
     *
     * @param identificationRecord 鉴定记录
     * @return 结果
     */
    public int updateIdentificationRecord(IdentificationRecord identificationRecord);

    /**
     * 删除鉴定记录
     *
     * @param id 鉴定记录主键
     * @return 结果
     */
    public int deleteIdentificationRecordById(Long id);

    /**
     * 批量删除鉴定记录
     *
     * @param ids 需要删除的数据主键
     * @return 结果
     */
    public int deleteIdentificationRecordByIds(Long[] ids);

    /**
     * 查询鉴定统计数据
     *
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    public List<Map<String, Object>> selectAppraisalStatistics(@Param("beginTime") String beginTime,
                                                               @Param("endTime") String endTime);

    /**
     * 查询月度鉴定统计
     *
     * @return 月度统计结果
     */
    public List<Map<String, Object>> selectMonthlyAppraisalCount();
}