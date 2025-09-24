package com.iams.manage.service;

import java.util.List;
import com.iams.manage.domain.BorrowRecord;

/**
 * 借阅记录Service接口
 * 
 * @author 张金明
 * @date 2025-01-15
 */
public interface IBorrowRecordService 
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
     * 批量删除借阅记录
     * 
     * @param ids 需要删除的借阅记录主键集合
     * @return 结果
     */
    public int deleteBorrowRecordByIds(Long[] ids);

    /**
     * 删除借阅记录信息
     * 
     * @param id 借阅记录主键
     * @return 结果
     */
    public int deleteBorrowRecordById(Long id);
}
