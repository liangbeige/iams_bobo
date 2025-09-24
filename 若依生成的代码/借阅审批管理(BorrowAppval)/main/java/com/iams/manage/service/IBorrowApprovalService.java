package com.iams.manage.service;

import java.util.List;
import com.iams.manage.domain.BorrowApproval;

/**
 * 借阅审批Service接口
 * 
 * @author zhjm
 * @date 2025-01-15
 */
public interface IBorrowApprovalService 
{
    /**
     * 查询借阅审批
     * 
     * @param id 借阅审批主键
     * @return 借阅审批
     */
    public BorrowApproval selectBorrowApprovalById(Long id);

    /**
     * 查询借阅审批列表
     * 
     * @param borrowApproval 借阅审批
     * @return 借阅审批集合
     */
    public List<BorrowApproval> selectBorrowApprovalList(BorrowApproval borrowApproval);

    /**
     * 新增借阅审批
     * 
     * @param borrowApproval 借阅审批
     * @return 结果
     */
    public int insertBorrowApproval(BorrowApproval borrowApproval);

    /**
     * 修改借阅审批
     * 
     * @param borrowApproval 借阅审批
     * @return 结果
     */
    public int updateBorrowApproval(BorrowApproval borrowApproval);

    /**
     * 批量删除借阅审批
     * 
     * @param ids 需要删除的借阅审批主键集合
     * @return 结果
     */
    public int deleteBorrowApprovalByIds(Long[] ids);

    /**
     * 删除借阅审批信息
     * 
     * @param id 借阅审批主键
     * @return 结果
     */
    public int deleteBorrowApprovalById(Long id);
}
