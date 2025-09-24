package com.iams.manage.mapper;

import java.util.List;
import com.iams.manage.domain.PendingEvaluation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 待鉴定信息Mapper接口
 *
 * @author LiuTao
 * @date 2025-04-14
 */
@Mapper
public interface PendingEvaluationMapper
{
    /**
     * 查询待鉴定信息
     *
     * @param id 待鉴定信息主键
     * @return 待鉴定信息
     */
    public PendingEvaluation selectPendingEvaluationById(Long id);

    /**
     * 查询待鉴定信息列表
     *
     * @param pendingEvaluation 待鉴定信息
     * @return 待鉴定信息集合
     */
    public List<PendingEvaluation> selectPendingEvaluationList(PendingEvaluation pendingEvaluation);

    /**
     * 新增待鉴定信息
     *
     * @param pendingEvaluation 待鉴定信息
     * @return 结果
     */
    public int insertPendingEvaluation(PendingEvaluation pendingEvaluation);

    /**
     * 修改待鉴定信息
     *
     * @param pendingEvaluation 待鉴定信息
     * @return 结果
     */
    public int updatePendingEvaluation(PendingEvaluation pendingEvaluation);

    /**
     * 删除待鉴定信息
     *
     * @param id 待鉴定信息主键
     * @return 结果
     */
    public int deletePendingEvaluationById(Long id);

    /**
     * 批量删除待鉴定信息
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePendingEvaluationByIds(Long[] ids);


    /**
     * 根据档案ID查找记录
     *
     * @param archiveId
     * @return 结果
     */
    public PendingEvaluation selectPendingEvaluationByArchiveId(Long archiveId);
}
