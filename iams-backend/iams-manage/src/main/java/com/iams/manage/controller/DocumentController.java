package com.iams.manage.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import com.iams.common.config.RuoYiConfig;
import com.iams.common.utils.file.FileUploadUtils;
import com.iams.common.utils.file.FileUtils;
import com.iams.framework.config.ServerConfig;
import com.iams.manage.domain.BatchUpdateCategoryDto;
import com.iams.manage.domain.ExtraInfo;
import com.iams.manage.task.OcrBatchTask;
import com.iams.minio.config.MinIOInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.iams.common.annotation.Log;
import com.iams.common.core.controller.BaseController;
import com.iams.common.core.domain.AjaxResult;
import com.iams.common.enums.BusinessType;
import com.iams.manage.domain.Document;
import com.iams.manage.service.IDocumentService;
import com.iams.common.utils.poi.ExcelUtil;
import com.iams.common.core.page.TableDataInfo;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档信息Controller
 * 
 * @author zhjm
 * @date 2025-01-10
 */
@RestController
@RequestMapping("/manage/document")
public class DocumentController extends BaseController
{
    @Autowired
    private ServerConfig serverConfig;

    @Autowired
    private IDocumentService documentService;

    @Autowired
    private FileStorageService fileStorageService;//注入实列
    @Autowired
    private OcrBatchTask ocrBatchTask;

    @Resource
    private MinIOInfo minIOInfo;

    /**
     * 查询文档信息列表
     */
    @PreAuthorize("@ss.hasPermi('manage:document:list')")
    @GetMapping("/list")
    public TableDataInfo list(Document document)
    {
        startPage();
        List<Document> list = documentService.selectDocumentList(document);
        return getDataTable(list);
    }

    /**
     * 根据给定的id列表查询文档信息列表
     */
    @PreAuthorize("@ss.hasPermi('manage:document:list')")
    @GetMapping("/list/by/ids")
    public TableDataInfo list_by_ids(String ids)
    {
        // 使用 Java 8 的 Stream API 将字符串分割并转换为 Long 数组
        Long[] idarray = Arrays.stream(ids.split(",")).map(Long::parseLong).toArray(Long[]::new);
        startPage();
        List<Document> list = documentService.selectDocumentListByIds(idarray);
        return getDataTable(list);
    }

    /**
     * 导出文档信息列表
     */
    @PreAuthorize("@ss.hasPermi('manage:document:export')")
    @Log(title = "文档信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Document document)
    {
        List<Document> list = documentService.selectDocumentList(document);
        ExcelUtil<Document> util = new ExcelUtil<Document>(Document.class);
        util.exportExcel(response, list, "文档信息数据");
    }

    /**
     * 获取文档信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('manage:document:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) throws Exception {
        // 获取MINIO服务器的IP:Port
        String minioHostAddress = minIOInfo.getEndpoint();
        Document document = documentService.selectDocumentById(id);
        // 防止文件名里面出现#等特殊字符，对其进行URL编码
        String encodedMinIOName = URLEncoder.encode(document.getMinioName(), StandardCharsets.UTF_8);
        // 替换 + 为 %20）
        encodedMinIOName = encodedMinIOName.replace("+", "%20");
        // 文件访问路径：minioHostAddress + Bucket + encodedMinIOName
        document.setFilePath(minioHostAddress + "/" + document.getFileLocation() + "/" + encodedMinIOName);
        return success(document);
    }

    /**
     * 根据 archiveId 查询文档信息列表
     */
    @PreAuthorize("@ss.hasPermi('manage:document:list')")
    @GetMapping("/list-by-archiveId")
    public TableDataInfo listByArchiveId(Long archiveId)
    {
        startPage();
        List<Document> list = documentService.selectListByArchiveId(archiveId);
        return getDataTable(list);
    }

    /**
     * 新增文档信息
     */
    @PreAuthorize("@ss.hasPermi('manage:document:add')")
    @Log(title = "文档信息", businessType = BusinessType.INSERT)
    @PostMapping
    public Long add(@RequestBody Document document)
    {
        // file_location中存放文件在minio中的bucket名称，
        // bucket中的文件名存放在minio_name字段
        document.setFileLocation(minIOInfo.getBucket());
        // file_path不存储minio中的主机地址和存储路径，
        // 访问文件的时候，把构造好的URL链接放在filePath中，返回给前端
        document.setFilePath("");
        return documentService.insertDocument(document);
    }

    /**
     * 修改文档信息
     */
    @PreAuthorize("@ss.hasPermi('manage:document:edit')")
    @Log(title = "文档信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Document document)
    {
        return toAjax(documentService.updateDocument(document));
    }

    /**
     * 上传文档文件
     */
    @PreAuthorize("@ss.hasPermi('manage:document:edit')")
    @Log(title = "文档附件", businessType = BusinessType.UPDATE)
    @PutMapping("/upload")
    public AjaxResult UploadFile(MultipartFile file)
    {
        try
        {
            // 上传文件路径
            String filePath = RuoYiConfig.getUploadPath();
            // 上传并返回新文件名称
            String fileName = FileUploadUtils.upload(filePath, file);
            String url = serverConfig.getUrl() + fileName;
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", url);
            ajax.put("fileName", fileName);
            ajax.put("newFileName", FileUtils.getName(fileName));
            ajax.put("originalFilename", file.getOriginalFilename());
            return ajax;
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 删除文档信息
     */
    @PreAuthorize("@ss.hasPermi('manage:document:remove')")
    @Log(title = "文档信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
//        return toAjax(documentService.deleteDocumentByIds(ids));
        int result = documentService.deleteDocumentByIds(ids);
        if (result > 0) {
            return AjaxResult.success("删除成功（MySQL删除" + result + "条，ES可能部分成功）");
        }
        return AjaxResult.error("删除失败");
    }

    /**
     * 得到最大的文档序号
     */
    @PreAuthorize("@ss.hasPermi('manage:document:query')")
    @GetMapping(value ="/xuhao/{id}")
    public AjaxResult getMaxXuhao(@PathVariable("id") Long id)
    {
        return success(documentService.selectMaxXuhaoByArchiveId(id));
    }

    /**
     * 录入文档额外信息
     */
    @PreAuthorize("@ss.hasPermi('manage:document:edit')")
    @Log(title = "文档额外信息", businessType = BusinessType.UPDATE)
    @PutMapping("/extraInfo")
    public AjaxResult updateExtraInfo(@RequestBody ExtraInfo extraInfo)
    {
        return toAjax(documentService.updateDocExtraInfo(extraInfo));
    }

    /**
     * 批量更新文档门类
     */
    @PreAuthorize("@ss.hasPermi('manage:document:edit')")
    @Log(title = "文档管理-批量更新门类", businessType = BusinessType.UPDATE)
    @PutMapping("/batchUpdateCategory")
    public AjaxResult batchUpdateCategory(@RequestBody BatchUpdateCategoryDto dto)
    {
        return toAjax(documentService.batchUpdateDocumentCategory(dto));
    }

    // 根据id修改content
//    @PreAuthorize("@ss.hasPermi('manage:document:edit')")
    @Log(title = "文档信息", businessType = BusinessType.UPDATE)
    @PostMapping ("/updateContent")
    public AjaxResult updateContent(@RequestParam Long id,
                                    @RequestParam String content,
                                    @RequestParam String title,
                                    @RequestParam String number,
                                    @RequestParam String unit){
        Document document = documentService.selectDocumentById(id);
        document.setContent(content);
        document.setTitle(title);
        document.setNumber(number);
        document.setUnit(unit);
        return toAjax(documentService.updateDocument(document));
    }

    @GetMapping("/ocred")
    public TableDataInfo listOcred(Document document)
    {
        startPage();
        List<Document> list = documentService.selectOcredDocumentList(document);
        return getDataTable(list);
    }

    @GetMapping("/noOcr")
    public TableDataInfo listNoOcr(Document document)
    {
        startPage();
        List<Document> list = documentService.selectNoOcrDocumentList(document);
        return getDataTable(list);
    }

    @GetMapping("/batchOcr")
    public void batchOcr()
    {
        System.out.println("开始批量处理文件");
        ocrBatchTask.performOcrBatchTask();
    }

    @GetMapping("/selectByProjectId")
    public TableDataInfo selectByProjectId(Long projectId)
    {
        startPage();
        List<Document> list = documentService.selectByProjectId(projectId);
        return getDataTable(list);
    }

}
