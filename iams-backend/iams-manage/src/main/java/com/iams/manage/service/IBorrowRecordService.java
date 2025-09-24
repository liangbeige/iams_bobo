package com.iams.manage.service;

import java.util.List;
import com.iams.manage.domain.workflow.BorrowRecord;
import com.iams.manage.domain.workflow.dto.BorrowRecordDTO;

/**
 * 借阅记录Service接口
 * 
 * @author LiuTao
 * @date 2025-04-07
 */
public interface IBorrowRecordService
{
    /**
     * 查询借阅记录
     * 
     * @param id 借阅记录主键
     * @return 借阅记录
     */
    public BorrowRecordDTO selectBorrowRecordById(Long id);



    /**
     * 查询借阅记录列表
     * 
     * @param borrowRecord 借阅记录
     * @return 借阅记录集合
     */
    public List<BorrowRecordDTO> selectBorrowRecordList(BorrowRecord borrowRecord);

    /**
     * 查询已逾期的借阅记录列表——实体档案
     *
     * @param borrowRecord 借阅记录
     * @return 借阅记录集合
     */
    public List<BorrowRecordDTO> selectOverdueBorrowRecordList(BorrowRecord borrowRecord);


    /**
     * 新增借阅记录
     * 
     * @param borrowRecord 借阅记录
     * @return 结果
     */
    public int insertBorrowRecord(BorrowRecordDTO borrowRecord);

    /**
     * 修改借阅记录
     * 
     * @param borrowRecordDTO 借阅记录(前端调用)
     * @return 结果
     */
    public int updateBorrowRecord(BorrowRecordDTO borrowRecordDTO);

    /**
     * 修改借阅记录
     *
     * @param borrowRecord 借阅记录（后端流程监听器调用）
     */
    public void updateBorrowRecord(BorrowRecord borrowRecord);


    /**
     * 修改借阅记录， 实体归还
     *
     * @param borrowRecordDTO 借阅记录的id
     */
    public int updateOverdueBorrowRecord(BorrowRecordDTO borrowRecordDTO);


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

    /**
     *
     *
     *
     */
    public BorrowRecord selectBorrowRecordByBusinessKey(String businessKey);


    /**
     * 定时任务，修改借阅状态
     *
     */
    public void borrowRecordStatus();


    /**
     * 催还
     *
     */
    public int reminder(BorrowRecordDTO borrowRecordDTO);


    /**
     * 催办
     */
    public int expedite(Long id);


    /**
     * 提前归还，输入为borrowRecord的ID
     */
    public int advanceReturn(Long id);


    public int directBorrow(BorrowRecordDTO borrowRecordDTO);

}
