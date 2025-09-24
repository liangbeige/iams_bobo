package com.iams.manage.mapper;

import java.util.List;
import com.iams.manage.domain.workflow.BorrowRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 借阅记录Mapper接口
 * 
 * @author LiuTao
 * @date 2025-04-07
 */
@Mapper
public interface BorrowRecordMapper
{
    /**
     * 查询借阅记录
     * 
     * @param id 借阅记录主键
     * @return 借阅记录
     */
    public BorrowRecord selectBorrowRecordById(Long id);

    /**
     * 查询借阅记录列表
     * 
     * @param borrowRecord 借阅记录
     * @return 借阅记录集合
     */
    public List<BorrowRecord> selectBorrowRecordList(BorrowRecord borrowRecord);

    /**
     * 新增借阅记录
     * 
     * @param borrowRecord 借阅记录
     * @return 结果
     */
    public int insertBorrowRecord(BorrowRecord borrowRecord);

    /**
     * 修改借阅记录
     * 
     * @param borrowRecord 借阅记录
     * @return 结果
     */
    public int updateBorrowRecord(BorrowRecord borrowRecord);

    /**
     * 删除借阅记录
     * 
     * @param id 借阅记录主键
     * @return 结果
     */
    public int deleteBorrowRecordById(Long id);

    /**
     * 批量删除借阅记录
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBorrowRecordByIds(Long[] ids);

    public BorrowRecord selectBorrowRecordByBusinessKey(String businessKey);

    public BorrowRecord selectBorrowRecordByArchiveId(Long archiveId);
}
