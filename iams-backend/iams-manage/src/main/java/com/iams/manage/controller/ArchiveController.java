package com.iams.manage.controller;

import java.util.*;
import java.util.stream.Collectors;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.iams.common.utils.SecurityUtils;
import com.iams.common.utils.StringUtils;
import com.iams.common.utils.uuid.IdUtils;
import com.iams.common.utils.uuid.UUID;
import com.iams.manage.domain.ArchiveBatchInsert;
import com.iams.manage.domain.Fonds;
import com.iams.manage.domain.InventoryDTO.InventoryResultDTO;
import com.iams.manage.service.*;
import com.iams.common.utils.SearchContextHolder;
import com.iams.manage.domain.Document;
import jakarta.servlet.http.HttpServletResponse;
import com.iams.manage.utils.HexStringGenerator;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.iams.common.annotation.Log;
import com.iams.common.core.controller.BaseController;
import com.iams.common.core.domain.AjaxResult;
import com.iams.common.enums.BusinessType;
import com.iams.manage.domain.Archive;
import com.iams.common.utils.poi.ExcelUtil;
import com.iams.common.core.page.TableDataInfo;

import com.iams.elasticsearch.domain.ElasticsearchArchive;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.iams.manage.service.IArchiveService;


/**
 * 档案列表Controller
 *
 * @author zhjm
 * @date 2025-01-10
 */
@RestController
@RequestMapping("/manage/archive")
public class ArchiveController extends BaseController
{
    @Autowired
    private IArchiveService archiveService;

    @Autowired
    private IDocumentService documentService;

    @Autowired
    private IFondsService fondsService;

    @Autowired
    private IElasticsearchArchiveService ElasticsearchArchiveService;

    @Autowired
    private IProjectService projectService;

    @Autowired
    private RestTemplate restTemplate;



    /**
     * 上传档案销毁佐证材料
     */
    @PostMapping("/uploadDestructionCertificate")
    public AjaxResult uploadDestructionCertificate(@RequestParam("archiveId") Long archiveId,
                                                   @RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            return AjaxResult.error("上传文件不能为空");
        }
        try {
            String fileUrl = archiveService.uploadDestructionCertificate(archiveId, file);
            AjaxResult ajax = AjaxResult.success("上传成功");
            ajax.put("url", fileUrl); // 返回URL，以便前端可能立即使用
            return ajax;
        } catch (Exception e) {
            return AjaxResult.error("上传失败: " + e.getMessage());
        }
    }


    /**
     * 查询档案列表列表
     */
    @GetMapping("/list")
    public TableDataInfo list(Archive archive)
    {
        startPage();
        List<Archive> list = archiveService.selectArchiveList(archive);
        return getDataTable(list);
    }


    @GetMapping("/borrowArchiveInVolume")
    public TableDataInfo borrowArchiveInVolume(Archive archive) {
        startPage();
        List<Archive> list = archiveService.borrowArchiveInVolume(archive);
        return getDataTable(list);
    }

    /**
     *  获取档案列表列表-全体
     */

    @GetMapping("/list-all")
    public TableDataInfo listAll(Archive archive) {
        List<Archive> list = archiveService.selectArchiveListAll(archive);
        return getDataTable(list);
    }

    @PostMapping("/fourCharacteristicsTest/{id}")
    public AjaxResult fourCharacteristicsTest(@PathVariable("id") Long id)
    {
        return AjaxResult.success(archiveService.generateFourCharacteristicsReport(id));
    }

    @GetMapping("/Elasticsearch")
    public TableDataInfo contentList(@RequestParam Map<String, String> allParams) {
        try {
            // 获取当前用户角色
            boolean isAdmin = SecurityUtils.getLoginUser().getUser().isAdmin();

            // 1. 获取分页参数
            int pageNum = 1;
            int pageSize = 10;

            // 尝试从PageHelper获取分页参数
            if (PageHelper.getLocalPage() != null) {
                pageNum = PageHelper.getLocalPage().getPageNum();
                pageSize = PageHelper.getLocalPage().getPageSize();
            } else {
                // 如果PageHelper没有设置，从请求参数中获取
                pageNum = Integer.parseInt(allParams.getOrDefault("pageNum", "1"));
                pageSize = Integer.parseInt(allParams.getOrDefault("pageSize", "10"));
            }

            // 清理PageHelper，避免影响ES查询
            PageHelper.clearPage();

            // 2. 将分页参数传递给ES服务
            allParams.put("pageNum", String.valueOf(pageNum));
            allParams.put("pageSize", String.valueOf(pageSize));

            // 3. 执行ES查询（保持原有逻辑）
            List<ElasticsearchArchive> list = ElasticsearchArchiveService.CombineSearch(allParams);

            // 🔥 4. 直接提取档号（去掉去重逻辑）
            List<String> danghaos = list.stream()
                    .map(ElasticsearchArchive::getMysqlDanghao)
                    .filter(Objects::nonNull)
                    .filter(danghao -> !danghao.trim().isEmpty())  // 过滤空字符串
                    .collect(Collectors.toList());  // 🔥 直接使用List，不去重

            if (!danghaos.isEmpty()) {
                // 5. 批量查询档案状态信息
                List<Map<String, Object>> statusList = archiveService.getArchiveStatusByDanghaos(danghaos);

                // 6. 构建状态映射（处理重复key的情况）
                Map<String, Map<String, Object>> statusMap = statusList.stream()
                        .collect(Collectors.toMap(
                                item -> (String) item.get("danghao"),
                                item -> item,
                                (existing, replacement) -> existing  // 遇到重复key时保留第一个
                        ));
                // 🔥 7. 补充状态信息到每个档案
                list = list.stream().map(archive -> {
                        // 补充状态信息
                        Map<String, Object> statusInfo = statusMap.get(archive.getMysqlDanghao());
                        if (statusInfo != null) {
                            archive.setStatus((String) statusInfo.get("status"));
                            archive.setShitiLocation((String) statusInfo.get("shiti_location"));
                            archive.setExactLocation((String) statusInfo.get("exact_location"));
                        }
                        return archive;
                }).collect(Collectors.toList());
                System.out.println("状态信息补充完成，数据量: " + list.size());
            }
            // 🔥 统一获取总数
            long total = ElasticsearchArchiveService.getTotalCount(allParams);
            System.out.println("总数: " + total);

            // 9. 手动创建Page对象
            Page<ElasticsearchArchive> page = new Page<>(pageNum, pageSize);
            page.addAll(list);
            page.setTotal(total);

            // 10. 构建返回结果
            TableDataInfo dataTable = getDataTable(page);
            return dataTable;
        } catch (Exception e) {
            System.err.println("查询失败: " + e.getMessage());
            e.printStackTrace();

            TableDataInfo errorResult = new TableDataInfo();
            errorResult.setCode(500);
            errorResult.setMsg("查询失败: " + e.getMessage());
            return errorResult;
        }
    }



    // 根据 mysqlDanghao 获取档案的 id
    @GetMapping("/get-id-by-mysql-danghao")
    @PreAuthorize("@ss.hasPermi('manage:archive:query')")
    public  ResponseEntity<Map<String, Object>> getArchiveIdByMysqlDanghao(@RequestParam String mysqlDanghao) {
        Long archiveId = archiveService.getArchiveIdByMysqlDanghao(mysqlDanghao);
        if (archiveId != null) {
            Map<String, Object> result = new HashMap<>();
            result.put("data", archiveId);
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.ok(null);
        }
    }

    @GetMapping("/get-location-by-mysql-danghao")
    // @PreAuthorize("@ss.hasPermi('manage:archive:query')")
    public  ResponseEntity<Map<String, Object>> getArchiveLocationByDanghao(@RequestParam String mysqlDanghao) {
        Map<String, Object> location = archiveService.getArchiveLocationByDanghao(mysqlDanghao);
        if (location != null) {
            Map<String, Object> result = new HashMap<>();
            result.put("data", location);
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.ok(null);
        }
    }


    /**
     * 导出档案列表列表
     */
    @PreAuthorize("@ss.hasPermi('manage:archive:export')")
    @Log(title = "档案列表", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Archive archive)
    {
        List<Archive> list = archiveService.selectArchiveList(archive);
        ExcelUtil<Archive> util = new ExcelUtil<Archive>(Archive.class);
        util.exportExcel(response, list, "档案列表数据");
    }

    /**
     * 获取档案列表详细信息
     */
    @PreAuthorize("@ss.hasPermi('manage:archive:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
            return success(archiveService.selectArchiveById(id));
    }

    /**
     * 获取档案列表详细信息
     */
    @PreAuthorize("@ss.hasPermi('manage:archive:query')")
    @GetMapping(value = "/get-by-rfid/{rfid}")
    public AjaxResult getArchiveByRfid(@PathVariable("rfid") String rfid)
    {
        return success(archiveService.selectArchiveByRfid(rfid));
    }

    /**
     * 获取档案权限
     */
    //@PreAuthorize("@ss.hasPermi('manage:archive:query')")
    @GetMapping(value = "/permission/{id}")
    public AjaxResult getPermissionInfo(@PathVariable("id") Long id)
    {
        if(archiveService.checkArchivePermission(id))
            return AjaxResult.success(true);
        else
            return AjaxResult.error("没有权限查阅该档案，请先借阅");
    }



    /**
     * 新增档案列表
     */
    @PreAuthorize("@ss.hasPermi('manage:archive:add')")
    @Log(title = "档案列表", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Archive archive)
    {
        try {
            int result = archiveService.insertArchive(archive);

            // 更新项目档案统计
            if (result > 0) {
                // 新档案保存成功后，更新关联项目的档案统计
                if (archive.getFondsId() != null) {
                    Fonds fonds = fondsService.selectFondsById(archive.getFondsId());
                    fonds.setArchiveCount(fonds.getArchiveCount() + result);
                    fondsService.updateFonds(fonds);
                }

                // 新档案保存成功后，更新关联项目的档案统计
                if (archive.getProjectId() != null) {
                    projectService.updateArchiveCount(archive.getProjectId());
                }
                return AjaxResult.success();
            }
            return AjaxResult.error();

        } catch (com.iams.common.exception.ServiceException e) {
            // 🔥 捕获业务异常（包括档号重复）
            logger.error("新增档案业务异常: {}", e.getMessage());
            return AjaxResult.error(e.getMessage());
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 数据库唯一约束冲突（作为最后防线）
            logger.error("档号重复异常", e);
            String errorMsg = "档号重复";
            if (e.getMessage().contains("档号")) {
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("Key \\(danghao\\)=\\(([^)]+)\\)");
                java.util.regex.Matcher matcher = pattern.matcher(e.getMessage());
                if (matcher.find()) {
                    errorMsg = "档号【" + matcher.group(1) + "】已存在";
                }
            }
            return AjaxResult.error(errorMsg);
        } catch (Exception e) {
            logger.error("新增档案失败", e);
            return AjaxResult.error("新增失败：" + e.getMessage());
        }
    }

    /**
     * 修改档案列表
     */
    @PreAuthorize("@ss.hasPermi('manage:archive:edit')")
    @Log(title = "档案列表", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Archive archive)
    {
        try {

        // 获取修改前的档案记录
        Long archiveId = archive.getId();
        Archive originalArchive = archiveService.selectArchiveById(archiveId);

        // 更新档案
        int result = archiveService.updateArchive(archive);

        if (result > 0) {
            // 处理项目变更的统计更新
            handleProjectStatisticUpdate(originalArchive, archive);
            return AjaxResult.success();
        }
        return AjaxResult.error();

        } catch (com.iams.common.exception.ServiceException e) {
            // 🔥 捕获业务异常（包括档号重复）- 参考添加时的逻辑
            logger.error("修改档案业务异常: {}", e.getMessage());
            return AjaxResult.error(e.getMessage());
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 数据库唯一约束冲突（作为最后防线）
            logger.error("档号重复异常", e);
            String errorMsg = "档号重复";
            if (e.getMessage().contains("档号")) {
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("Key \\(danghao\\)=\\(([^)]+)\\)");
                java.util.regex.Matcher matcher = pattern.matcher(e.getMessage());
                if (matcher.find()) {
                    errorMsg = "档号【" + matcher.group(1) + "】已存在";
                }
            }
            return AjaxResult.error(errorMsg);
        } catch (Exception e) {
            logger.error("修改档案失败", e);
            return AjaxResult.error("修改失败：" + e.getMessage());
        }

    }

    /**
     * 处理档案项目变更时的统计更新
     */
    private void handleProjectStatisticUpdate(Archive original, Archive updated) {
        Long oldProjectId = original.getProjectId();
        Long newProjectId = updated.getProjectId();

        // 如果项目ID发生了变化
        if (oldProjectId != null && !oldProjectId.equals(newProjectId)) {
            // 更新原项目的档案统计
            projectService.updateArchiveCount(oldProjectId);
        }

        // 如果新增或变更了项目ID
        if (newProjectId != null) {
            // 更新新项目的档案统计
            projectService.updateArchiveCount(newProjectId);
        }
    }

    /**
     * 删除档案列表
     */
    @PreAuthorize("@ss.hasPermi('manage:archive:remove')")
    @Log(title = "档案列表", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {

//        return toAjax(archiveService.deleteArchiveByIds(ids));
        // 在删除前获取所有受影响的项目ID
        Set<Long> projectIds = getAffectedProjectIds(ids);

        for (Long archiveId : ids) {
            Fonds fonds = fondsService.selectFondsById(archiveService.selectArchiveById(archiveId).getFondsId());
            fonds.setArchiveCount(fonds.getArchiveCount() - 1);
            fondsService.updateFonds(fonds);
        }
        // 执行删除操作
        int result = archiveService.deleteArchiveByIds(ids);

        // 更新项目档案统计


        if (result > 0) {
            // 更新所有相关项目的档案统计
            for (Long projectId : projectIds) {
                projectService.updateArchiveCount(projectId);
            }
            return AjaxResult.success();
        }
        return AjaxResult.error();
    }

    /**
     * 获取受删除操作影响的项目ID集合
     */
    private Set<Long> getAffectedProjectIds(Long[] archiveIds) {
        return archiveService.getProjectIdsForArchives(archiveIds);
    }

    /**
     * 新增档案文件列表
     */
    @PreAuthorize("@ss.hasPermi('manage:archive:edit')")
    @Log(title = "档案列表", businessType = BusinessType.UPDATE)
    @PutMapping("/directory")
    public AjaxResult addDirectoryFiles(@RequestBody Document document)
    {
        return toAjax(archiveService.addDirectoryFiles(document));
    }

    /**
     * 删除档案文件列表
     */
    @PreAuthorize("@ss.hasPermi('manage:archive:edit')")
    @Log(title = "档案列表", businessType = BusinessType.UPDATE)
    @PutMapping("/directory/delete")
    public AjaxResult deleteDirectoryFiles(@RequestBody Document document) {
        return toAjax(archiveService.deleteDirectoryFiles(document));
    }

    /**
     * 根据location查询档案列表
     */
//    @PreAuthorize("@ss.hasPermi('manage:archive:list')")
    @GetMapping("/location")
    public TableDataInfo getArchiveByLocation(@RequestParam String shitiLocation,
                                              @RequestParam(required = false) String exactLocation) {
        String location = shitiLocation;
        if (exactLocation != null) {
            location = location + '-' + exactLocation;
        }
        List<Archive> list = archiveService.getArchiveByLocation(location);
//        System.out.println(list);
        return getDataTable(list);
    }

    /**
     * 拖拽移动档案
     */
    @PreAuthorize("@ss.hasPermi('manage:archive:move')")
    @Log(title = "档案列表", businessType = BusinessType.UPDATE)
    @PostMapping("/move")
    public AjaxResult moveArchive(@RequestBody Map<String, Object> requestParams) {
        Long archiveId = Long.parseLong(requestParams.get("archiveId").toString());
        String targetLocation = requestParams.get("targetLocation").toString();
        return toAjax(archiveService.moveArchive(archiveId, targetLocation));
    }

    /**
     * 通过上下架更新档案位置
     */
    @PreAuthorize("@ss.hasPermi('manage:archive:update')")
    @Log(title = "档案列表", businessType = BusinessType.UPDATE)
    @PostMapping("/updateLocation")
    public AjaxResult updateLocation(@RequestBody List<Archive> archives) {
        return toAjax(archiveService.updateLocation(archives));
    }

    /**
     * 获取未上架的档案列表
     */
    @PreAuthorize("@ss.hasPermi('manage:archive:list')")
    @GetMapping("/upArchiveList")
    public TableDataInfo getUpArchiveList(@RequestParam Map<String, Object> query) {
        startPage();
        List<Archive> list = archiveService.getUpArchiveList(query);
        return getDataTable(list);
    }

    /**
     * 获取上架的档案列表
     */
    @PreAuthorize("@ss.hasPermi('manage:archive:list')")
    @GetMapping("/getArchivedList")
    public List<Archive> getDownArchiveList() {
        return archiveService.getArchivedList();
    }

    // 生成档号需要
    @PreAuthorize("@ss.hasPermi('manage:archive:query')")
    @GetMapping("/get-next-archive-number")
    public AjaxResult getNextArchiveNumber(@RequestParam String id ) {
        String uuid = IdUtils.fastUUID();
        return AjaxResult.success(uuid);
    }

    // 文档批量挂接的接口
    @PreAuthorize("@ss.hasPermi('manage:archive:edit')")
    @Log(title = "档案列表", businessType = BusinessType.UPDATE)
    @PostMapping("/batch-mount")
    public AjaxResult batchMount(@RequestBody Map<String, Object> data) {
        // {docIds={docIds=[38], directoryId=1}}
        // 获取 docIds 对象
        Map<String, Object> docIdsMap = (Map<String, Object>) data.get("docIds");

        // 从 docIds 对象中获取 docIds 数组
        List<Integer> docIds = (List<Integer>) docIdsMap.get("docIds");

        // 获取 directoryId
        Integer directoryId = (Integer) docIdsMap.get("directoryId");

        for (Integer docId : docIds) {
            // 得到修改目录前文档对象
            Document document = documentService.selectDocumentById(docId.longValue());
            // 在档案目录中删除原文档id
            archiveService.deleteDirectoryFiles(document);
            // 更新文档id
            document.setDirectory(directoryId.longValue());
            // 更新文档
            documentService.updateDocument(document);
            // 在档案目录中添加新文档id
            archiveService.addDirectoryFiles(document);
        }
        return AjaxResult.success(data.size());
    }

    @PostMapping("/guidang")
    public AjaxResult guidang(@RequestBody Map<String, Object> params) {
        try {
            // 从 Map 中提取参数
            Object idObj = params.get("id");
            Object testConfigObj = params.get("testConfig");

            // 参数校验
            if (idObj == null) {
                return AjaxResult.error("ID不能为空");
            }
            if (testConfigObj == null || StringUtils.isBlank(testConfigObj.toString())) {
                return AjaxResult.error("检测配置不能为空");
            }

            // 类型转换
            Long id = Long.valueOf(idObj.toString());
            String testConfig = testConfigObj.toString();

            // 调用服务层进行归档操作
            boolean success = archiveService.guidang(id, testConfig);

            if (success) {
                return AjaxResult.success("归档成功");
            } else {
                return AjaxResult.error("归档失败");
            }
        } catch (NumberFormatException e) {
            return AjaxResult.error("ID格式错误");
        } catch (Exception e) {
            return AjaxResult.error("归档操作异常：" + e.getMessage());
        }
    }

    @PostMapping("/unarchive")
    public AjaxResult unarchive(@RequestBody Archive archive) {

        return toAjax(archiveService.updateArchive(archive));

    }

    /**
     * 批量导入档案
     */
    /**
     * 批量导入档案
     */
    @Log(title = "档案导入", businessType = BusinessType.IMPORT)
    @PostMapping("/batchImport")
    public AjaxResult batchImport(@RequestBody ArchiveBatchInsert request) {
        try {
            if (request.getRecords() == null || request.getRecords().isEmpty()) {
                return AjaxResult.error("导入数据不能为空");
            }

            // 简单的数据验证
            List<String> validationErrors = validateBatchArchives(request.getRecords());
            if (!validationErrors.isEmpty()) {
                return AjaxResult.error("数据验证失败：" + String.join("; ", validationErrors));
            }

            int successCount = archiveService.batchImportArchives(
                    request.getRecords(),
                    request.getProjectId()
            );

            return AjaxResult.success("成功导入 " + successCount + " 条档案数据");

        } catch (com.iams.common.exception.ServiceException e) {
            // 业务异常，直接返回错误信息
            logger.error("批量导入业务异常: {}", e.getMessage());
            return AjaxResult.error(e.getMessage());
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 数据库唯一约束冲突
            logger.error("档号重复异常", e);
            String errorMsg = "档号重复";
            if (e.getMessage().contains("档号")) {
                // 尝试提取具体的重复档号
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("Key \\(danghao\\)=\\(([^)]+)\\)");
                java.util.regex.Matcher matcher = pattern.matcher(e.getMessage());
                if (matcher.find()) {
                    errorMsg = "档号【" + matcher.group(1) + "】已存在";
                }
            }
            return AjaxResult.error(errorMsg);
        } catch (Exception e) {
            logger.error("批量导入失败", e);
            return AjaxResult.error("导入失败：" + e.getMessage());
        }
    }

    /**
     * 验证批量导入的档案数据
     */
    private List<String> validateBatchArchives(List<Archive> archives) {
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < archives.size(); i++) {
            Archive archive = archives.get(i);
            String prefix = "第" + (i + 1) + "行：";

            if (archive.getDanghao() == null || archive.getDanghao().trim().isEmpty()) {
                errors.add(prefix + "档号不能为空");
            }
            if (archive.getName() == null || archive.getName().trim().isEmpty()) {
                errors.add(prefix + "档案名称不能为空");
            }

            // 可以添加更多验证规则
            if (archive.getJuanhao() == null || archive.getJuanhao().trim().isEmpty()) {
                errors.add(prefix + "案卷号不能为空");
            }
            if (archive.getFormationUnit() == null || archive.getFormationUnit().trim().isEmpty()) {
                errors.add(prefix + "形成单位不能为空");
            }
        }

        return errors;
    }


    @GetMapping("/selectByCategoryId")
    public List<Archive> selectByCategoryId(@RequestParam String categoryId, @RequestParam Long projectId) {
        return archiveService.selectByCategoryId(categoryId, projectId);

    }

    @PostMapping("/printRFID/{ID}")
    public AjaxResult printRFID(@PathVariable("ID") Long ID) {
        Archive archive = archiveService.selectArchiveById(ID);

        String testEPC = HexStringGenerator.generate23CharHexString();

        if(archiveService.checkRfidUnique(testEPC)) {
            archive.setRfid(testEPC + "00");
            archiveService.updateArchive(archive);
        }
        // 可以写死：192.168.1.15
        String ipaddr = SecurityUtils.getLoginUser().getIpaddr();

        try {
            String url = "http://" + "192.168.1.15" + ":3000/execute";
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 构造请求体 - 传递RFID参数
            Map<String, Object> requestBody = new HashMap<>();

            // 创建参数列表，包含RFID参数
            List<String> args = new ArrayList<>();
            args.add("-r");  // 或者使用 "--rfid"
            args.add(archive.getRfid());  // 传递RFID数据

            args.add("-t");
            args.add(projectService.selectProjectById(archive.getProjectId()).getBianhao() + "——" + archive.getCategoryId().charAt(0));

            // 如果需要传递其他参数，可以继续添加
            // args.add("-p");  // 打印机型号
            // args.add("HT330");
            // args.add("-w");  // 打印宽度
            // args.add("600");
            // args.add("-d");  // 打印浓度
            // args.add("25");
            // args.add("-l");  // 循环次数
            // args.add("1");

            requestBody.put("args", args);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 发送POST请求
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            Map<String, Object> result = response.getBody();

            // 检查执行结果
            if (result != null && Boolean.TRUE.equals(result.get("success"))) {
                return AjaxResult.success("打印成功")
                        .put("output", result.get("stdout"))
                        .put("timestamp", result.get("timestamp"))
                        .put("rfid", archive.getRfid());  // 返回使用的RFID
            } else {
                return AjaxResult.error("打印失败: " + result.get("error"))
                        .put("stderr", result.get("stderr"));
            }

        } catch (Exception e) {
            return AjaxResult.error("打印失败: " + e.getMessage());
        }
    }


    /**
     * 获取待鉴定档案统计数量
     */
    @PreAuthorize("@ss.hasPermi('manage:archive:list')")
    @GetMapping("/pending/count")
    public AjaxResult getPendingAppraisalCount() {
        try {
            // 查询所有档案
            Archive queryParam = new Archive();
            List<Archive> allArchives = archiveService.selectArchiveList(queryParam);

            // 筛选需要鉴定的档案
            long count = allArchives.stream()
                    .filter(this::needsAppraisal)
                    .filter(arc -> !isDestroyedOrDestroying(arc)) // 排除销毁状态
                    .count();

            return AjaxResult.success(count);
        } catch (Exception e) {
            logger.error("获取待鉴定档案数量失败", e);
            return AjaxResult.error("获取待鉴定档案数量失败: " + e.getMessage());
        }
    }

    /**
     * 判断档案是否需要鉴定
     */
    private boolean needsAppraisal(Archive archive) {
        // 已销毁的档案不需要鉴定
        if ("Destroyed".equals(archive.getStatus())) {
            return false;
        }

        // 检查必填字段是否缺失
        boolean hasMissingFields = StringUtils.isEmpty(archive.getRetentionPeriod()) ||
                StringUtils.isEmpty(archive.getSecretLevel()) ||
                StringUtils.isEmpty(archive.getSecretPeroid());

        // 检查是否过期
        boolean isExpired = isRetentionExpired(archive) || isSecretExpired(archive);

        return hasMissingFields || isExpired;
    }

    /**
     * 计算档案鉴定原因
     */
    private String calculateAppraisalReasons(Archive archive) {
        List<String> reasons = new ArrayList<>();

        if (StringUtils.isEmpty(archive.getRetentionPeriod())) {
            reasons.add("保管期限缺失");
        }
        if (StringUtils.isEmpty(archive.getSecretLevel())) {
            reasons.add("保密级别缺失");
        }
        if (StringUtils.isEmpty(archive.getSecretPeroid())) {
            reasons.add("保密期限缺失");
        }

        if (isRetentionExpired(archive)) {
            reasons.add("保管期限过期");
        }
        if (isSecretExpired(archive)) {
            reasons.add("保密期限过期");
        }

        return String.join(",", reasons);
    }

    /**
     * 检查保管期限是否过期
     */
    private boolean isRetentionExpired(Archive archive) {
        if (StringUtils.isEmpty(archive.getRetentionPeriod()) || archive.getCreateTime() == null) {
            return false;
        }

        try {
            int retentionYears = Integer.parseInt(archive.getRetentionPeriod());
            Calendar cal = Calendar.getInstance();
            cal.setTime(archive.getCreateTime());
            cal.add(Calendar.YEAR, retentionYears);

            return new Date().after(cal.getTime());
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 检查保密期限是否过期
     */
    private boolean isSecretExpired(Archive archive) {
        if (StringUtils.isEmpty(archive.getSecretPeroid()) || archive.getCreateTime() == null) {
            return false;
        }

        try {
            int secretYears = Integer.parseInt(archive.getSecretPeroid());
            Calendar cal = Calendar.getInstance();
            cal.setTime(archive.getCreateTime());
            cal.add(Calendar.YEAR, secretYears);

            return new Date().after(cal.getTime());
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 获取待鉴定档案列表
     */
    @PreAuthorize("@ss.hasPermi('manage:archive:list')")
    @GetMapping("/pending/list")
    public TableDataInfo getPendingAppraisalList(Archive archive) {
        try {
            startPage();

            // 查询所有档案
            List<Archive> allArchives = archiveService.selectArchiveList(archive);

            // 筛选需要鉴定的档案
            List<Archive> pendingArchives = allArchives.stream()
                    .filter(this::needsAppraisal)
                    .filter(arc -> !isDestroyedOrDestroying(arc)) // 新增：排除销毁状态
                    .collect(Collectors.toList());

            // 为每个档案添加鉴定原因到remark字段
            for (Archive arc : pendingArchives) {
                String reasons = calculateAppraisalReasons(arc);
                arc.setRemark(reasons);
            }

            return getDataTable(pendingArchives);
        } catch (Exception e) {
            logger.error("获取待鉴定档案列表失败", e);
            return getDataTable(new ArrayList<>());
        }
    }

    /**
     * 判断档案是否为销毁中或已销毁状态
     *
     * @param archive 档案
     * @return true 如果是销毁中或已销毁状态
     */
    private boolean isDestroyedOrDestroying(Archive archive) {
        if (archive == null || archive.getStatus() == null) {
            return false;
        }

        String status = archive.getStatus();
        return "Destroyed".equals(status) || "Destroying".equals(status);
    }

    @GetMapping("/printRFID")
    public AjaxResult printRFID() {
        return AjaxResult.success(HexStringGenerator.generate23CharHexString() + "0");
    }

    /**
     * 批量上传盘点结果
     */
    @PostMapping("/inventory/upload")
    public AjaxResult uploadInventoryResult(@RequestBody InventoryResultDTO inventoryResult) {
        try {
            // 1. 保存盘点主记录
            Long inventoryId = archiveService.saveInventoryMain(inventoryResult);

            // 2. 批量保存盘点明细
            archiveService.saveInventoryDetails(inventoryId, inventoryResult.getRecords());


            Map<String, Object> report = new HashMap<>();
            report.put("inventoryId", inventoryId);
            report.put("totalCount", inventoryResult.getTotalCount());
            report.put("scannedCount", inventoryResult.getScannedCount());
            report.put("unscannedCount", inventoryResult.getTotalCount() - inventoryResult.getScannedCount());
            report.put("inventoryRate", String.format("%.2f%%",
                    (inventoryResult.getScannedCount() * 100.0 / inventoryResult.getTotalCount())));

            return AjaxResult.success("盘点数据上传成功", report);
        } catch (Exception e) {
            logger.error("盘点数据上传失败", e);
            return AjaxResult.error("盘点数据上传失败: " + e.getMessage());
        }
    }

    /**
     * 临时功能
     */
    @PostMapping("/addCategoryRootName")
    public AjaxResult addCategoryRootName() {
        return success(archiveService.addCategoryRootName());
    }

}
