package com.iams.manage.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.iams.common.annotation.Log;
import com.iams.common.core.controller.BaseController;
import com.iams.common.core.domain.AjaxResult;
import com.iams.common.enums.BusinessType;
import com.iams.manage.domain.BorrowApproval;
import com.iams.manage.service.IBorrowApprovalService;
import com.iams.common.utils.poi.ExcelUtil;
import com.iams.common.core.page.TableDataInfo;

/**
 * 借阅审批Controller
 * 
 * @author zhjm
 * @date 2025-01-15
 */
@RestController
@RequestMapping("/manage/approval")
public class BorrowApprovalController extends BaseController
{
    @Autowired
    private IBorrowApprovalService borrowApprovalService;

    /**
     * 查询借阅审批列表
     */
    @PreAuthorize("@ss.hasPermi('manage:approval:list')")
    @GetMapping("/list")
    public TableDataInfo list(BorrowApproval borrowApproval)
    {
        startPage();
        List<BorrowApproval> list = borrowApprovalService.selectBorrowApprovalList(borrowApproval);
        return getDataTable(list);
    }

    /**
     * 导出借阅审批列表
     */
    @PreAuthorize("@ss.hasPermi('manage:approval:export')")
    @Log(title = "借阅审批", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BorrowApproval borrowApproval)
    {
        List<BorrowApproval> list = borrowApprovalService.selectBorrowApprovalList(borrowApproval);
        ExcelUtil<BorrowApproval> util = new ExcelUtil<BorrowApproval>(BorrowApproval.class);
        util.exportExcel(response, list, "借阅审批数据");
    }

    /**
     * 获取借阅审批详细信息
     */
    @PreAuthorize("@ss.hasPermi('manage:approval:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(borrowApprovalService.selectBorrowApprovalById(id));
    }

    /**
     * 新增借阅审批
     */
    @PreAuthorize("@ss.hasPermi('manage:approval:add')")
    @Log(title = "借阅审批", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BorrowApproval borrowApproval)
    {
        return toAjax(borrowApprovalService.insertBorrowApproval(borrowApproval));
    }

    /**
     * 修改借阅审批
     */
    @PreAuthorize("@ss.hasPermi('manage:approval:edit')")
    @Log(title = "借阅审批", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BorrowApproval borrowApproval)
    {
        return toAjax(borrowApprovalService.updateBorrowApproval(borrowApproval));
    }

    /**
     * 删除借阅审批
     */
    @PreAuthorize("@ss.hasPermi('manage:approval:remove')")
    @Log(title = "借阅审批", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(borrowApprovalService.deleteBorrowApprovalByIds(ids));
    }
}
