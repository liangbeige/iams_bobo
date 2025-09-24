package com.iams.manage.controller;

import com.iams.common.annotation.Log;
import com.iams.common.core.controller.BaseController;
import com.iams.common.core.domain.AjaxResult;
import com.iams.common.core.page.TableDataInfo;
import com.iams.common.enums.BusinessType;
import com.iams.common.utils.poi.ExcelUtil;
import com.iams.manage.domain.Archive;
import com.iams.manage.domain.workflow.DestroyRecord;
import com.iams.manage.domain.workflow.dto.DestroyRecordDTO;
import com.iams.manage.service.IArchiveService;
import com.iams.manage.service.IDestroyRecordService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 档案销毁Controller（简化版，无工作流）
 *
 * 修改说明：
 * 1. 保持原有接口结构不变，确保前端兼容
 * 2. 调整参数验证，适配新的数据表结构
 * 3. 添加新的销毁确认接口，支持完整的销毁流程
 * 4. 增强错误处理和日志记录
 *
 * @author LiuTao
 * @date 2025-06-25
 */
@Slf4j
@RestController
@RequestMapping("/manage/archiveDestroy")
public class ArchiveDestroyApprovalController extends BaseController {

    @Autowired
    private IDestroyRecordService destroyRecordService;

    @Autowired
    private IArchiveService archiveService;

    /**
     * 查询可销毁的档案列表（已归档且在架）
     * 修改说明：保持接口不变，确保前端兼容
     */
    @PreAuthorize("@ss.hasPermi('manage:archiveDestroy:list')")
    @GetMapping("/availableArchives")
    @Operation(summary = "查询可销毁档案列表", description = "获取所有已归档且在架的档案列表")
    public TableDataInfo availableArchives(Archive archive, @RequestParam(required = false) List<String> statuses) {
//
//        log.debug("查询档案列表，查询条件: {}, statuses: {}", archive, statuses);

        startPage();
//        // 设置查询条件：只查询已归档且在架的档案
//        archive.setStatus("Archived");
//        archive.setAvailability(0); // 0表示在架
//
//        List<Archive> list = archiveService.selectArchiveList(archive);
//
//        log.debug("查询到可销毁档案数量: {}", list.size());
//        return getDataTable(list);
//    }
//        List<Archive> allResults = new ArrayList<>();
        if (archive.getStatus() != null && !archive.getStatus().trim().isEmpty()) {
            statuses = Arrays.asList(archive.getStatus());
        } else if (statuses == null || statuses.isEmpty()) {
            statuses = Arrays.asList("Archived", "Destroying", "Destroyed");
        }
        // 直接调用service方法，传入状态列表
        List<Archive> list = archiveService.selectArchiveListByStatuses(archive, statuses);
        return getDataTable(list);
    }
        // 🔥 处理多状态查询逻辑
//        List<String> statusesToQuery;
//
//        if (archive.getStatus() != null && !archive.getStatus().trim().isEmpty()) {
//            // 如果设置了单一状态，只查询这一个状态
//            statusesToQuery = Arrays.asList(archive.getStatus());
//        } else if (statuses != null && !statuses.isEmpty()) {
//            // 如果前端传递了 statuses 参数，使用前端的参数
//            statusesToQuery = statuses;
//        } else {
//            // 默认查询三种状态
//            statusesToQuery = Arrays.asList("Archived", "Destroying", "Destroyed");
//        }
//
//        log.debug("将要查询的状态: {}", statusesToQuery);
//
//        // 分别查询每种状态的档案
//        for (String status : statusesToQuery) {
//            // 创建临时查询条件，避免修改原始参数
//            Archive tempArchive = new Archive();
//
//            // 🔥 复制所有查询条件
//            if (archive.getDanghao() != null) tempArchive.setDanghao(archive.getDanghao());
//            if (archive.getName() != null) tempArchive.setName(archive.getName());
//            if (archive.getRfid() != null) tempArchive.setRfid(archive.getRfid());
//            if (archive.getCarrierType() != null) tempArchive.setCarrierType(archive.getCarrierType());
//            if (archive.getCategoryId() != null) tempArchive.setCategoryId(archive.getCategoryId());
//            if (archive.getFondsName() != null) tempArchive.setFondsName(archive.getFondsName());
//            if (archive.getProjectId() != null) tempArchive.setProjectId(archive.getProjectId());
//            if (archive.getAvailability() != null) tempArchive.setAvailability(archive.getAvailability());
//            if (archive.getSecretLevel() != null) tempArchive.setSecretLevel(archive.getSecretLevel());
//            if (archive.getSecretPeroid() != null) tempArchive.setSecretPeroid(archive.getSecretPeroid());
//
//            // 🔥 设置当前要查询的状态
//            tempArchive.setStatus(status);
//
//            // 查询当前状态的档案
//            List<Archive> statusResults = archiveService.selectArchiveList(tempArchive);
//            allResults.addAll(statusResults);
//
//            log.debug("状态 {} 查询到 {} 条记录", status, statusResults.size());
//        }
//
//        log.debug("总共查询到档案数量: {}", allResults.size());
//        return getDataTable(allResults);
//    }

    /**
     * 查询销毁记录列表
     * 修改说明：使用简化的DTO返回，提高查询性能
     */
    @PreAuthorize("@ss.hasPermi('manage:archiveDestroy:list')")
    @GetMapping("/records")
    @Operation(summary = "查询销毁记录列表", description = "获取所有销毁记录")
    public TableDataInfo destroyRecords(DestroyRecord destroyRecord) {
        log.debug("查询销毁记录列表，查询条件: {}", destroyRecord);

        startPage();
        List<DestroyRecordDTO> list = destroyRecordService.selectSimpleDestroyRecordList(destroyRecord);

        log.debug("查询到销毁记录数量: {}", list.size());
        return getDataTable(list);
    }

    /**
     * 导出销毁记录列表
     * 修改说明：保持功能不变
     */
    @PreAuthorize("@ss.hasPermi('manage:archiveDestroy:export')")
    @Log(title = "档案销毁记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @Operation(summary = "导出销毁记录列表")
    public void export(HttpServletResponse response, DestroyRecord destroyRecord) {
        log.info("开始导出销毁记录列表");

        List<DestroyRecordDTO> list = destroyRecordService.selectSimpleDestroyRecordList(destroyRecord);
        ExcelUtil<DestroyRecordDTO> util = new ExcelUtil<>(DestroyRecordDTO.class);
        util.exportExcel(response, list, "档案销毁记录");

        log.info("销毁记录列表导出完成，记录数: {}", list.size());
    }

    /**
     * 获取销毁记录详细信息
     * 修改说明：返回完整的DTO信息，包含关联数据
     */
    @PreAuthorize("@ss.hasPermi('manage:archiveDestroy:query')")
    @GetMapping("/records/{id}")
    @Operation(summary = "获取销毁记录详细信息")
    public AjaxResult getRecordInfo(@PathVariable("id") Long id) {
        log.debug("查询销毁记录详情，ID: {}", id);

        DestroyRecordDTO record = destroyRecordService.selectDestroyRecordById(id);
        if (record == null) {
            return error("销毁记录不存在");
        }

        return success(record);
    }

    /**
     * 提交销毁申请（单个档案）
     * 修改说明：增强参数验证和错误处理
     */
    @PreAuthorize("@ss.hasPermi('manage:archiveDestroy:add')")
    @Log(title = "提交档案销毁申请", businessType = BusinessType.INSERT)
    @PostMapping("/submit")
    @Operation(summary = "提交销毁申请", description = "提交单个档案的销毁申请")
    public AjaxResult submitDestroyApplication(@RequestBody @Valid DestroyRecordDTO destroyRecordDTO) {
        log.info("收到销毁申请，档案ID: {}", destroyRecordDTO.getArchiveId());

        try {
            // 参数校验
            if (destroyRecordDTO.getArchiveId() == null) {
                return error("档案ID不能为空");
            }

            // 检查前端表单数据
            if (destroyRecordDTO.getReason() == null || destroyRecordDTO.getReason().trim().isEmpty()) {
                return error("请选择销毁原因");
            }
            if (destroyRecordDTO.getDescription() == null || destroyRecordDTO.getDescription().trim().isEmpty()) {
                return error("请填写详细说明");
            }
            if (destroyRecordDTO.getApplicant() == null || destroyRecordDTO.getApplicant().trim().isEmpty()) {
                return error("请选择申请人");
            }

            int result = destroyRecordService.submitDestroyApplication(destroyRecordDTO);

            if (result > 0) {
                log.info("销毁申请提交成功，档案ID: {}", destroyRecordDTO.getArchiveId());
                return success("销毁申请提交成功");
            } else {
                return error("销毁申请提交失败");
            }

        } catch (RuntimeException e) {
            log.error("提交销毁申请失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("提交销毁申请异常: ", e);
            return error("系统异常，请联系管理员");
        }
    }

    /**
     * 批量提交销毁申请
     * 修改说明：增强错误处理，返回详细的处理结果
     */
    @PreAuthorize("@ss.hasPermi('manage:archiveDestroy:add')")
    @Log(title = "批量提交档案销毁申请", businessType = BusinessType.INSERT)
    @PostMapping("/batchSubmit")
    @Operation(summary = "批量提交销毁申请")
    public AjaxResult batchSubmitDestroyApplication(@RequestBody BatchDestroyRequest request) {
        log.info("收到批量销毁申请，档案数量: {}",
                request.getArchiveIds() != null ? request.getArchiveIds().length : 0);

        try {
            // 参数校验
            if (request.getArchiveIds() == null || request.getArchiveIds().length == 0) {
                return error("请选择要销毁的档案");
            }
            if (request.getPurpose() == null || request.getPurpose().trim().isEmpty()) {
                return error("请填写销毁原因");
            }

            int result = destroyRecordService.batchSubmitDestroyApplication(request);

            log.info("批量销毁申请处理完成，成功数量: {}", result);
            return success("批量销毁申请提交成功，共处理 " + result + " 个档案");

        } catch (RuntimeException e) {
            log.error("批量提交销毁申请失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("批量提交销毁申请异常: ", e);
            return error("系统异常，请联系管理员");
        }
    }

    /**
     * 确认完成销毁（第二步：实际销毁操作）
     * 修改说明：这是业务流程的第二步，档案从"待下架"变为"已销毁"
     */
    @PreAuthorize("@ss.hasPermi('manage:archiveDestroy:complete')")
    @Log(title = "确认完成档案销毁", businessType = BusinessType.UPDATE)
    @PostMapping("/complete/{archiveId}")
    @Operation(summary = "确认完成销毁", description = "确认档案已被物理销毁")
    public AjaxResult completeDestroy(@PathVariable Long archiveId) {
        log.info("收到销毁确认请求，档案ID: {}", archiveId);

        try {
            int result = destroyRecordService.completeDestroy(archiveId);

            if (result > 0) {
                log.info("销毁确认成功，档案ID: {}", archiveId);
                return success("档案销毁确认成功");
            } else {
                return error("销毁确认失败");
            }

        } catch (RuntimeException e) {
            log.error("销毁确认失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("销毁确认异常: ", e);
            return error("系统异常，请联系管理员");
        }
    }

    /**
     * 批量确认完成销毁
     * 新增接口：支持批量确认销毁
     */
    @PreAuthorize("@ss.hasPermi('manage:archiveDestroy:complete')")
    @Log(title = "批量确认完成档案销毁", businessType = BusinessType.UPDATE)
    @PostMapping("/batchComplete")
    @Operation(summary = "批量确认完成销毁", description = "批量确认档案已被物理销毁")
    public AjaxResult batchCompleteDestroy(@RequestBody Long[] archiveIds) {
        log.info("收到批量销毁确认请求，档案数量: {}", archiveIds != null ? archiveIds.length : 0);

        if (archiveIds == null || archiveIds.length == 0) {
            return error("请选择要确认销毁的档案");
        }

        int successCount = 0;
        List<String> errorMessages = new java.util.ArrayList<>();

        for (Long archiveId : archiveIds) {
            try {
                destroyRecordService.completeDestroy(archiveId);
                successCount++;
            } catch (Exception e) {
                errorMessages.add("档案ID " + archiveId + " 确认失败：" + e.getMessage());
            }
        }

        if (errorMessages.isEmpty()) {
            log.info("批量销毁确认全部成功，处理数量: {}", successCount);
            return success("批量销毁确认成功，共处理 " + successCount + " 个档案");
        } else {
            String resultMsg = String.format("成功确认 %d 个，失败 %d 个。失败原因：%s",
                    successCount, errorMessages.size(), String.join("; ", errorMessages));
            log.warn("批量销毁确认部分失败: {}", resultMsg);
            return warn(resultMsg);
        }
    }

    /**
     * 标记销毁失败
     * 修改说明：保持功能不变
     */
    @PreAuthorize("@ss.hasPermi('manage:archiveDestroy:fail')")
    @Log(title = "标记销毁失败", businessType = BusinessType.UPDATE)
    @PostMapping("/fail/{archiveId}")
    @Operation(summary = "标记销毁失败")
    public AjaxResult failDestroy(@PathVariable Long archiveId, @RequestParam String reason) {
        log.info("收到销毁失败标记请求，档案ID: {}, 原因: {}", archiveId, reason);

        try {
            if (reason == null || reason.trim().isEmpty()) {
                return error("请填写失败原因");
            }

            int result = destroyRecordService.failDestroy(archiveId, reason);

            if (result > 0) {
                log.info("销毁失败标记成功，档案ID: {}", archiveId);
                return success("销毁失败标记成功");
            } else {
                return error("销毁失败标记失败");
            }

        } catch (RuntimeException e) {
            log.error("标记销毁失败失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("标记销毁失败异常: ", e);
            return error("系统异常，请联系管理员");
        }
    }

    /**
     * 取消销毁申请（仅限待下架状态）
     * 修改说明：保持功能不变
     */
    @PreAuthorize("@ss.hasPermi('manage:archiveDestroy:cancel')")
    @Log(title = "取消销毁申请", businessType = BusinessType.UPDATE)
    @PostMapping("/cancel/{id}")
    @Operation(summary = "取消销毁申请")
    public AjaxResult cancelDestroy(@PathVariable Long id) {
        log.info("收到取消销毁申请请求，记录ID: {}", id);

        try {
            int result = destroyRecordService.cancelDestroyApplication(id);

            if (result > 0) {
                log.info("取消销毁申请成功，记录ID: {}", id);
                return success("销毁申请取消成功");
            } else {
                return error("取消销毁申请失败");
            }

        } catch (RuntimeException e) {
            log.error("取消销毁申请失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("取消销毁申请异常: ", e);
            return error("系统异常，请联系管理员");
        }
    }

    /**
     * 删除销毁记录
     * 修改说明：保持功能不变
     */
    @PreAuthorize("@ss.hasPermi('manage:archiveDestroy:remove')")
    @Log(title = "删除销毁记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/records/{ids}")
    @Operation(summary = "删除销毁记录")
    public AjaxResult remove(@PathVariable Long[] ids) {
        log.info("收到删除销毁记录请求，记录数量: {}", ids != null ? ids.length : 0);

        if (ids == null || ids.length == 0) {
            return error("请选择要删除的记录");
        }

        try {
            int result = destroyRecordService.deleteDestroyRecordByIds(ids);

            if (result > 0) {
                log.info("删除销毁记录成功，删除数量: {}", result);
                return success("删除成功");
            } else {
                return error("删除失败");
            }

        } catch (Exception e) {
            log.error("删除销毁记录异常: ", e);
            return error("删除失败：" + e.getMessage());
        }
    }

    /**
     * RFID同步接口（处理离线下架操作）
     * 新增接口：支持盘点车离线操作后的数据同步
     */
    @PreAuthorize("@ss.hasPermi('manage:archiveDestroy:sync')")
    @Log(title = "同步离线销毁操作", businessType = BusinessType.UPDATE)
    @PostMapping("/sync/{archiveId}")
    @Operation(summary = "同步离线销毁操作", description = "盘点车离线下架后的数据同步")
    public AjaxResult syncOfflineDestroy(@PathVariable Long archiveId) {
        log.info("收到离线销毁同步请求，档案ID: {}", archiveId);

        try {
            int result = destroyRecordService.syncOfflineDestroyOperation(archiveId);

            if (result > 0) {
                log.info("离线销毁同步成功，档案ID: {}", archiveId);
                return success("离线操作同步成功");
            } else {
                return error("离线操作同步失败");
            }

        } catch (RuntimeException e) {
            log.error("离线销毁同步失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("离线销毁同步异常: ", e);
            return error("系统异常，请联系管理员");
        }
    }

    /**
     * 批量销毁申请请求体
     * 修改说明：保持结构不变，确保前端兼容
     */
    public static class BatchDestroyRequest {
        private Long[] archiveIds;
        private String purpose;
        private String description;
        private String applicant;

        // getter/setter
        public Long[] getArchiveIds() { return archiveIds; }
        public void setArchiveIds(Long[] archiveIds) { this.archiveIds = archiveIds; }
        public String getPurpose() { return purpose; }
        public void setPurpose(String purpose) { this.purpose = purpose; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getApplicant() { return applicant; }
        public void setApplicant(String applicant) { this.applicant = applicant; }
    }


    /**
     * 根据档案ID撤销销毁申请
     * 新增接口：支持从档案列表直接撤销销毁申请
     */
    @PreAuthorize("@ss.hasPermi('manage:archiveDestroy:cancel')")
    @Log(title = "根据档案ID撤销销毁申请", businessType = BusinessType.UPDATE)
    @PostMapping("/cancelByArchiveId/{archiveId}")
    @Operation(summary = "根据档案ID撤销销毁申请")
    public AjaxResult cancelDestroyByArchiveId(@PathVariable Long archiveId) {
        log.info("收到根据档案ID撤销销毁申请请求，档案ID: {}", archiveId);

        try {
            int result = destroyRecordService.cancelDestroyByArchiveId(archiveId);

            if (result > 0) {
                log.info("撤销销毁申请成功，档案ID: {}", archiveId);
                return success("销毁申请撤销成功，档案已恢复为在架状态");
            } else {
                return error("撤销销毁申请失败");
            }

        } catch (RuntimeException e) {
            log.error("撤销销毁申请失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("撤销销毁申请异常: ", e);
            return error("系统异常，请联系管理员");
        }
    }
}