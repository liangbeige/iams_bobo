package com.iams.manage.mapper;

import com.iams.manage.domain.workflow.DestroyRecord;
import java.util.List;

/**
 * 销毁记录Mapper接口
 *
 * 修改说明：
 * 1. 保持接口方法不变，确保服务层兼容
 * 2. XML文件中的字段映射需要调整为新的数据表结构
 */
public interface DestroyRecordMapper {

    /**
     * 查询销毁记录
     *
     * @param id 销毁记录主键
     * @return 销毁记录
     */
    public DestroyRecord selectDestroyRecordById(Long id);

    /**
     * 查询销毁记录列表
     *
     * @param destroyRecord 销毁记录
     * @return 销毁记录集合
     */
    public List<DestroyRecord> selectDestroyRecordList(DestroyRecord destroyRecord);

    /**
     * 新增销毁记录
     *
     * @param destroyRecord 销毁记录
     * @return 结果
     */
    public int insertDestroyRecord(DestroyRecord destroyRecord);

    /**
     * 修改销毁记录
     *
     * @param destroyRecord 销毁记录
     * @return 结果
     */
    public int updateDestroyRecord(DestroyRecord destroyRecord);

    /**
     * 删除销毁记录
     *
     * @param id 销毁记录主键
     * @return 结果
     */
    public int deleteDestroyRecordById(Long id);

    /**
     * 批量删除销毁记录
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteDestroyRecordByIds(Long[] ids);

    /**
     * 根据档案ID查询销毁记录
     *
     * @param archiveId 档案ID
     * @return 销毁记录列表
     */
    public List<DestroyRecord> selectDestroyRecordByArchiveId(Long archiveId);

    /**
     * 根据用户ID查询销毁记录
     *
     * @param userId 用户ID
     * @return 销毁记录列表
     */
    public List<DestroyRecord> selectDestroyRecordByUserId(Long userId);

    /**
     * 根据状态查询销毁记录数量
     *
     * @param status 状态
     * @return 数量
     */
    public int countByStatus(String status);

    /**
     * 根据用户ID和状态查询销毁记录数量
     *
     * @param userId 用户ID
     * @param status 状态
     * @return 数量
     */
    public int countByUserIdAndStatus(Long userId, String status);
}