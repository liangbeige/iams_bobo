package com.iams.manage.service.impl;

import java.util.List;
import com.iams.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.iams.manage.mapper.BorrowApprovalMapper;
import com.iams.manage.domain.BorrowApproval;
import com.iams.manage.service.IBorrowApprovalService;

/**
 * 借阅审批Service业务层处理
 * 
 * @author zhjm
 * @date 2025-01-15
 */
@Service
public class BorrowApprovalServiceImpl implements IBorrowApprovalService 
{
    @Autowired
    private BorrowApprovalMapper borrowApprovalMapper;

    /**
     * 查询借阅审批
     * 
     * @param id 借阅审批主键
     * @return 借阅审批
     */
    @Override
    public BorrowApproval selectBorrowApprovalById(Long id)
    {
        return borrowApprovalMapper.selectBorrowApprovalById(id);
    }

    /**
     * 查询借阅审批列表
     * 
     * @param borrowApproval 借阅审批
     * @return 借阅审批
     */
    @Override
    public List<BorrowApproval> selectBorrowApprovalList(BorrowApproval borrowApproval)
    {
        return borrowApprovalMapper.selectBorrowApprovalList(borrowApproval);
    }

    /**
     * 新增借阅审批
     * 
     * @param borrowApproval 借阅审批
     * @return 结果
     */
    @Override
    public int insertBorrowApproval(BorrowApproval borrowApproval)
    {
        borrowApproval.setCreateTime(DateUtils.getNowDate());
        return borrowApprovalMapper.insertBorrowApproval(borrowApproval);
    }

    /**
     * 修改借阅审批
     * 
     * @param borrowApproval 借阅审批
     * @return 结果
     */
    @Override
    public int updateBorrowApproval(BorrowApproval borrowApproval)
    {
        borrowApproval.setUpdateTime(DateUtils.getNowDate());
        return borrowApprovalMapper.updateBorrowApproval(borrowApproval);
    }

    /**
     * 批量删除借阅审批
     * 
     * @param ids 需要删除的借阅审批主键
     * @return 结果
     */
    @Override
    public int deleteBorrowApprovalByIds(Long[] ids)
    {
        return borrowApprovalMapper.deleteBorrowApprovalByIds(ids);
    }

    /**
     * 删除借阅审批信息
     * 
     * @param id 借阅审批主键
     * @return 结果
     */
    @Override
    public int deleteBorrowApprovalById(Long id)
    {
        return borrowApprovalMapper.deleteBorrowApprovalById(id);
    }
}
