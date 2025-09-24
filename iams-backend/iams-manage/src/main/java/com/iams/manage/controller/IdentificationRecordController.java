package com.iams.manage.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.iams.common.annotation.Log;
import com.iams.common.core.controller.BaseController;
import com.iams.common.core.domain.AjaxResult;
import com.iams.common.enums.BusinessType;
import com.iams.manage.domain.workflow.IdentificationRecord;
import com.iams.manage.domain.workflow.dto.IdentificationRecordDTO;
import com.iams.manage.service.IIdentificationRecordService;
import com.iams.common.utils.poi.ExcelUtil;
import com.iams.common.core.page.TableDataInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 档案鉴定记录Controller
 *
 * @author LiuTao
 * @date 2025-06-26
 */
@RestController
@RequestMapping("/manage/identification")
public class IdentificationRecordController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(IdentificationRecordController.class);

    @Autowired
    private IIdentificationRecordService identificationRecordService;

    /**
     * 查询鉴定记录列表
     */
    @PreAuthorize("@ss.hasPermi('manage:identification:list')")
    @GetMapping("/list")
    public TableDataInfo list(IdentificationRecord identificationRecord) {

        try {
            startPage();
            List<IdentificationRecord> list = identificationRecordService.selectIdentificationRecordList(identificationRecord);
            return getDataTable(list);
        } catch (Exception e) {
            logger.error("查询鉴定记录列表失败", e);
            return getDataTable(List.of());
        }
    }

    /**
     * 导出鉴定记录列表
     */
    @PreAuthorize("@ss.hasPermi('manage:identification:export')")
    @Log(title = "鉴定记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, IdentificationRecord identificationRecord) {
        try {
            List<IdentificationRecord> list = identificationRecordService.selectIdentificationRecordList(identificationRecord);
            ExcelUtil<IdentificationRecord> util = new ExcelUtil<IdentificationRecord>(IdentificationRecord.class);
            util.exportExcel(response, list, "鉴定记录数据");
        } catch (Exception e) {
            logger.error("导出鉴定记录失败", e);
        }
    }

    /**
     * 获取鉴定记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('manage:identification:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        try {
            IdentificationRecord record = identificationRecordService.selectIdentificationRecordById(id);
            return success(record);
        } catch (Exception e) {
            logger.error("获取鉴定记录详细信息失败", e);
            return error("获取鉴定记录详细信息失败: " + e.getMessage());
        }
    }

    /**
     * 提交档案鉴定（支持批量）
     */
    @PreAuthorize("@ss.hasPermi('manage:identification:submit')")
    @Log(title = "档案鉴定", businessType = BusinessType.INSERT)
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody @Valid IdentificationRecordDTO identificationRecordDTO) {
        try {
            logger.info("接收到鉴定提交请求: {}", identificationRecordDTO);
            logger.info("=== 鉴定数据接收调试 ===");
            logger.info("接收到的原始DTO: {}", identificationRecordDTO);
            logger.info("archiveIds: {}", identificationRecordDTO.getArchiveIds());
            logger.info("appraiser: {}", identificationRecordDTO.getAppraiser());

            // 关键：检查这些字段是否有值
            logger.info("afterRetentionPeriod: [{}]", identificationRecordDTO.getAfterRetentionPeriod());
            logger.info("afterSecretLevel: [{}]", identificationRecordDTO.getAfterSecretLevel());
            logger.info("afterSecretPeriod: [{}]", identificationRecordDTO.getAfterSecretPeriod());

            logger.info("appraisalResult: {}", identificationRecordDTO.getAppraisalResult());
            logger.info("appraisalReason: {}", identificationRecordDTO.getAppraisalReason());
            logger.info("=== 调试结束 ===");

            // 验证必填字段
            if (identificationRecordDTO.getArchiveIds() == null || identificationRecordDTO.getArchiveIds().isEmpty()) {
                if (identificationRecordDTO.getArchiveId() == null) {
                    return error("档案ID不能为空");
                }
            }

            if (identificationRecordDTO.getAppraiser() == null || identificationRecordDTO.getAppraiser().trim().isEmpty()) {
                return error("鉴定人员不能为空");
            }

            if (identificationRecordDTO.getAppraisalResult() == null || identificationRecordDTO.getAppraisalResult().trim().isEmpty()) {
                return error("鉴定结果不能为空");
            }

            int result = identificationRecordService.submitIdentificationRecord(identificationRecordDTO);

            if (result > 0) {
                return success("鉴定提交成功，共处理 " + result + " 个档案");
            } else {
                return error("鉴定提交失败");
            }

        } catch (Exception e) {
            logger.error("提交档案鉴定失败", e);
            return error("提交档案鉴定失败: " + e.getMessage());
        }
    }

    /**
     * 修改鉴定记录
     */
    @PreAuthorize("@ss.hasPermi('manage:identification:edit')")
    @Log(title = "鉴定记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody @Valid IdentificationRecord identificationRecord) {
        try {
            int result = identificationRecordService.updateIdentificationRecord(identificationRecord);
            return toAjax(result);
        } catch (Exception e) {
            logger.error("修改鉴定记录失败", e);
            return error("修改鉴定记录失败: " + e.getMessage());
        }
    }

    /**
     * 删除鉴定记录
     */
    @PreAuthorize("@ss.hasPermi('manage:identification:remove')")
    @Log(title = "鉴定记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        try {
            int result = identificationRecordService.deleteIdentificationRecordByIds(ids);
            return toAjax(result);
        } catch (Exception e) {
            logger.error("删除鉴定记录失败", e);
            return error("删除鉴定记录失败: " + e.getMessage());
        }
    }

    /**
     * 根据档案ID查询鉴定记录
     */
    @PreAuthorize("@ss.hasPermi('manage:identification:query')")
    @GetMapping("/archive/{archiveId}")
    public AjaxResult getByArchiveId(@PathVariable("archiveId") Long archiveId) {
        try {
            List<IdentificationRecord> list = identificationRecordService.selectIdentificationRecordByArchiveId(archiveId);
            return success(list);
        } catch (Exception e) {
            logger.error("根据档案ID查询鉴定记录失败", e);
            return error("查询鉴定记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取鉴定统计数据
     */
    @PreAuthorize("@ss.hasPermi('manage:identification:query')")
    @GetMapping("/statistics")
    public AjaxResult getStatistics() {
        try {
            Map<String, Object> statistics = identificationRecordService.getIdentificationStatistics(null);
            return success(statistics);
        } catch (Exception e) {
            logger.error("获取鉴定统计数据失败", e);
            return error("获取鉴定统计数据失败: " + e.getMessage());
        }
    }
}