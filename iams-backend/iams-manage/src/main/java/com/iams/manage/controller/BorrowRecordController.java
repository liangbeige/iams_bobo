package com.iams.manage.controller;

import java.util.List;

import com.iams.manage.domain.workflow.dto.BorrowRecordDTO;
import com.iams.manage.service.impl.converter.BorrowRecordConverterImpl;
import io.swagger.v3.oas.annotations.Operation;
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
import com.iams.manage.domain.workflow.BorrowRecord;
import com.iams.manage.service.IBorrowRecordService;
import com.iams.common.utils.poi.ExcelUtil;
import com.iams.common.core.page.TableDataInfo;

import jakarta.validation.Valid;
/**
 * 借阅记录Controller
 *
 * @author LiuTao
 * @date 2025-04-07
 */
@RestController
@RequestMapping("/manage/record")
public class BorrowRecordController extends BaseController
{
    @Autowired
    private IBorrowRecordService borrowRecordService;

    /**
     * 查询借阅记录列表，目前仅支持针对实体类中的字段进行搜索
     * 后续如果要扩展，可以将这部分进行改变
     */
    //@PreAuthorize("@ss.hasPermi('manage:record:list')")
    @GetMapping("/list")
    @Operation(summary = "查询借阅记录列表", description = "获取借阅信息列表，可根据实体类中的关键字段进行筛选。返回DTO类。")
    public TableDataInfo list(BorrowRecord borrowRecord)
    {
        startPage();
        List<BorrowRecordDTO> list = borrowRecordService.selectBorrowRecordList(borrowRecord);
        return getDataTable(list);
    }


    /**
     * 查询借阅记录列表，目前仅支持针对实体类中的字段进行搜索
     * 后续如果要扩展，可以将这部分进行改变
     */
    @PreAuthorize("@ss.hasPermi('manage:record:list')")
    @GetMapping("/overdue")
    @Operation(summary = "查询逾期借阅记录列表", description = "实体档案超期未归还的获取借阅信息列表，可根据实体类中的关键字段进行筛选。返回DTO类。")
    public TableDataInfo listOverdue(BorrowRecord borrowRecord)
    {
        startPage();
        List<BorrowRecordDTO> list = borrowRecordService.selectOverdueBorrowRecordList(borrowRecord);
        return getDataTable(list);
    }


    /**
     * 导出借阅记录列表
     */
    @PreAuthorize("@ss.hasPermi('manage:record:export')")
    @Log(title = "借阅记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @Operation(summary = "导出借阅记录列表", description = "导出借阅信息列表，DTO类")
    public void export(HttpServletResponse response, BorrowRecord borrowRecord)
    {
        List<BorrowRecordDTO> list = borrowRecordService.selectBorrowRecordList(borrowRecord);
        ExcelUtil<BorrowRecordDTO> util = new ExcelUtil<>(BorrowRecordDTO.class);
        util.exportExcel(response, list, "借阅记录数据");
    }

    /**
     * 获取借阅记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('manage:record:query')")
    @GetMapping(value = "/{id}")
    @Operation(summary = "获取借阅记录详细信息", description = "根据id获取借阅信息，DTO类，用于修改数据前的获取原数据阶段。")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(borrowRecordService.selectBorrowRecordById(id));
    }

    /**
     * 新增借阅记录
     */
    @PreAuthorize("@ss.hasPermi('manage:record:add')")
    @PostMapping
    @Operation(summary = "新增借阅记录", description = "新增借阅信息，DTO类。")
    public AjaxResult add(@RequestBody @Valid BorrowRecordDTO dto)
    {
        return toAjax(borrowRecordService.insertBorrowRecord(dto));
    }

    /**
     * 修改借阅记录，实体与混合档的手动归还
     */
    @PreAuthorize("@ss.hasPermi('manage:record:edit')")
    @Log(title = "借阅记录", businessType = BusinessType.UPDATE)
    @PutMapping
    @Operation(summary = "提交保存修改后的借阅记录", description = "保存修改后的借阅信息，先借阅记录详细信息，后提交DTO类，")
    public AjaxResult edit(@RequestBody BorrowRecordDTO borrowRecordDTO)
    {
        return toAjax(borrowRecordService.updateOverdueBorrowRecord(borrowRecordDTO));
    }

    /**
     * 删除借阅记录
     */
    @PreAuthorize("@ss.hasPermi('manage:record:remove')")
    @Log(title = "借阅记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    @Operation(summary = "删除借阅记录", description = "根据id删除借阅信息，ids，调用流程相关服务终止实例。")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(borrowRecordService.deleteBorrowRecordByIds(ids));
    }

    /**
     * 修改借阅记录，实体与混合档的手动归还
     */
//    @PreAuthorize("@ss.hasPermi('manage:record:remind')")..
    @Log(title = "催还", businessType = BusinessType.UPDATE)
    @PutMapping("/remind")
    @Operation(summary = "催还", description = "邮件催还")
    public AjaxResult remind(@RequestBody BorrowRecordDTO borrowRecordDTO)
    {
        return toAjax(borrowRecordService.reminder(borrowRecordDTO));
    }

    @Log(title = "催办", businessType = BusinessType.UPDATE)
    @PutMapping("/expedite/{id}")
    @Operation(summary = "催办", description = "催办")
    public AjaxResult expedite(@PathVariable Long id)
    {
        return toAjax(borrowRecordService.expedite(id));
    }

    @PutMapping("/advance/{id}")
    @Operation(summary = "提前归还", description = "提前归还，输入为borrowRecord的ID")
    public AjaxResult advanceReturn(@PathVariable Long id)
    {

        return toAjax(borrowRecordService.advanceReturn(id));
    }

    /**
     * 查询借阅记录列表，目前仅支持针对实体类中的字段进行搜索
     * 后续如果要扩展，可以将这部分进行改变
     */
    @PreAuthorize("@ss.hasPermi('manage:record:list')")
    @GetMapping("/loanList")
    @Operation(summary = "查询借阅记录列表", description = "获取借阅信息列表，可根据实体类中的关键字段进行筛选。返回DTO类。")
    public TableDataInfo loanList(BorrowRecord borrowRecord)
    {
        startPage();
        List<BorrowRecordDTO> list = borrowRecordService.selectBorrowRecordList(borrowRecord);
        return getDataTable(list);
    }


    @PostMapping("/direct")
    @Operation(summary = "直接借阅", description = "直接借阅")
    public AjaxResult direct(@RequestBody BorrowRecordDTO borrowRecordDTO)
    {
        return toAjax(borrowRecordService.directBorrow(borrowRecordDTO));
    }
}
