package com.iams.manage.service.impl;

import java.util.List;
import com.iams.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.iams.manage.mapper.BorrowRecordMapper;
import com.iams.manage.domain.BorrowRecord;
import com.iams.manage.service.IBorrowRecordService;

/**
 * 借阅记录Service业务层处理
 * 
 * @author 张金明
 * @date 2025-01-15
 */
@Service
public class BorrowRecordServiceImpl implements IBorrowRecordService 
{
    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    /**
     * 查询借阅记录
     * 
     * @param id 借阅记录主键
     * @return 借阅记录
     */
    @Override
    public BorrowRecord selectBorrowRecordById(Long id)
    {
        return borrowRecordMapper.selectBorrowRecordById(id);
    }

    /**
     * 查询借阅记录列表
     * 
     * @param borrowRecord 借阅记录
     * @return 借阅记录
     */
    @Override
    public List<BorrowRecord> selectBorrowRecordList(BorrowRecord borrowRecord)
    {
        return borrowRecordMapper.selectBorrowRecordList(borrowRecord);
    }

    /**
     * 新增借阅记录
     * 
     * @param borrowRecord 借阅记录
     * @return 结果
     */
    @Override
    public int insertBorrowRecord(BorrowRecord borrowRecord)
    {
        borrowRecord.setCreateTime(DateUtils.getNowDate());
        return borrowRecordMapper.insertBorrowRecord(borrowRecord);
    }

    /**
     * 修改借阅记录
     * 
     * @param borrowRecord 借阅记录
     * @return 结果
     */
    @Override
    public int updateBorrowRecord(BorrowRecord borrowRecord)
    {
        borrowRecord.setUpdateTime(DateUtils.getNowDate());
        return borrowRecordMapper.updateBorrowRecord(borrowRecord);
    }

    /**
     * 批量删除借阅记录
     * 
     * @param ids 需要删除的借阅记录主键
     * @return 结果
     */
    @Override
    public int deleteBorrowRecordByIds(Long[] ids)
    {
        return borrowRecordMapper.deleteBorrowRecordByIds(ids);
    }

    /**
     * 删除借阅记录信息
     * 
     * @param id 借阅记录主键
     * @return 结果
     */
    @Override
    public int deleteBorrowRecordById(Long id)
    {
        return borrowRecordMapper.deleteBorrowRecordById(id);
    }
}
