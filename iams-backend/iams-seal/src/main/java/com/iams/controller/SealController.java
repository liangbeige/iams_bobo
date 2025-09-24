package com.iams.controller;


import com.iams.common.core.controller.BaseController;
import com.iams.common.core.domain.AjaxResult;
import com.iams.common.core.page.TableDataInfo;
import com.iams.common.utils.bean.BeanUtils;
import com.iams.domain.Seal;
import com.iams.domain.dto.SealDTO;
import com.iams.manage.service.IArchiveService;
import com.iams.mapper.SealMapper;
import com.iams.service.ISealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.iams.common.core.domain.AjaxResult.success;
import static com.iams.common.utils.PageUtils.startPage;
import static org.elasticsearch.search.aggregations.support.CoreValuesSourceType.log;

@RestController
@RequestMapping("/manage/seal")
public class SealController extends BaseController {


    @Autowired
    private ISealService sealService;

    @Autowired
    private SealMapper sealMapper; // 确保这里正确注入

    @Autowired
    private IArchiveService archiveService; // 需要注入档案服务

//    @Value("${seal.storage-path}")
//    private String storagePath;
//
//    @Value("${seal.storage-folder}")
//    private String storageFolder;



    /**
     * 获取印章列表
     */
    @GetMapping("/list")
    public TableDataInfo list(Seal seal) {
        startPage();
        List<Seal> list = sealService.selectSealList(seal);
        return getDataTable(list);
    }


    // ----------- 根据档号获取公章相关 -----------
    /**
     * 根据档号获取公章相关
     */

    @GetMapping("/official/generateByDocument")
    public ResponseEntity<byte[]> generateOfficialByDocument(
                    @RequestParam("documentId") Long documentId) {
        log.info("🔔 接收到请求 - documentId: {}", documentId);
        try {
            // 1. 根据文档ID获取档号
            String danghao = archiveService.getDanghaoByDocumentId(documentId);
            log.info("获取到档号: {}", danghao);

            // 2. 调用印章生成服务
            byte[] imageBytes = sealService.generateOfficialSeal(danghao, null, null);

            // 3. 验证PNG有效性（防御性编程）
            if (imageBytes == null || imageBytes.length < 8 ||
                    !(imageBytes[0] == (byte) 0x89 && imageBytes[1] == 'P' &&
                            imageBytes[2] == 'N' && imageBytes[3] == 'G')) {
                throw new IllegalStateException("生成的印章数据不是有效PNG格式");
            }



            // 4. 返回二进制响应（明确设置Content-Type）
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG) // 关键头
                    .cacheControl(CacheControl.noCache())
                    .header("Pragma", "no-cache")
                    .body(imageBytes);

        } catch (Exception e) {
            // 5. 错误处理（仍返回PNG格式的错误提示）
            byte[] errorPng = createErrorPng(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.IMAGE_PNG)
                    .body(errorPng);
        }
    }

    // 生成错误提示PNG（示例实现）
    private byte[] createErrorPng(String message) {
        // 这里应该实现生成包含错误信息的PNG图片
        // 简单返回1x1透明像素作为示例
        return new byte[]{
                -119, 80, 78, 71, 13, 10, 26, 10, // PNG签名
                0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0, // IHDR块
                31, -12, -101, -55, 0, 0, 0, 19, 73, 68, 65, 84, 120, -38, 99, 120, -103, -96, -55, 0, 4, // IDAT块
                -106, -83, 88, 46, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126 // IEND块
        };
    }



    @GetMapping("/official/preview")
    public ResponseEntity<AjaxResult> generateOfficial(
            @RequestParam String mainText,
            @RequestParam(required = false) String viceText,
            @RequestParam(required = false) String centerText) {
        try {
            // 调用你的实现类方法生成图片二进制数据
            byte[] imageBytes = sealService.generateOfficialSeal(mainText, viceText, centerText);

            // 转为Base64字符串
            String base64 = Base64.getEncoder().encodeToString(imageBytes);

            // 创建 AjaxResult 对象
            AjaxResult ajaxResult = new AjaxResult(200, "成功", base64);


            // 返回标准JSON
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON) // 明确声明JSON
                    .body(ajaxResult);
        } catch (IOException e) {
            AjaxResult ajaxResult = new AjaxResult(500, "生成公章失败：" + e.getMessage(), null);

            // 如果生成图片失败，返回错误信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ajaxResult);
        }

    }


    /**
     * 生成私章图片
     */
    @GetMapping("/personal/preview")
    public ResponseEntity<AjaxResult> generatePersonal(
            @RequestParam String name,
            @RequestParam String additionalText,
            @RequestParam(required = false) String mainFontFamily,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) Integer imageSize) {
        try {

            // 调用服务层
            byte[] imageBytes = sealService.generatePersonalSeal(name, additionalText);

            // 转为Base64字符串
            String base64 = Base64.getEncoder().encodeToString(imageBytes);

            // 创建 AjaxResult 对象
            AjaxResult ajaxResult = new AjaxResult(200, "成功", base64);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new AjaxResult(200, "成功", base64));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AjaxResult(500, "生成私章失败: " + e.getMessage(), null));
        }
    }

    /**
     * 保存印章配置
     */
//    @PostMapping("/add")
//    public AjaxResult add(@RequestBody SealDTO sealDTO) throws IOException {
//
//        // 0. 先插入数据库获取自增ID（关键修改）
//        Seal seal = new Seal();
//        BeanUtils.copyProperties(sealDTO, seal);
//        seal.setImagePath(""); // 先设空路径，后续更新
//        sealMapper.insert(seal); // 执行插入，获取自增ID
//
//        // 1. 使用数据库ID作为文件名（如：123.png）
//        String fileName = seal.getId() + ".png";
//        String folderPath = storagePath + File.separator + storageFolder;
//        File folder = new File(folderPath);
//        if (!folder.exists()) {
//            folder.mkdirs();
//        }
//
//        String fullPath = folderPath + File.separator + fileName;
//        String relativePath = "/" + storageFolder + "/" + fileName;
//
//
//        // 2. 生成图片文件（原有逻辑不变）
//        if ("OFFICIAL".equals(sealDTO.getSealType())) {
//            sealService.saveOfficialSealToLocal(
//                    sealDTO.getMainText(),
//                    sealDTO.getViceText(),
//                    sealDTO.getCenterText(),
//                    fullPath
//            );
//        } else {
//            sealService.savePersonalSealToLocal(
//                    sealDTO.getMainText(),
//                    sealDTO.getAdditionalText(),
//                    fullPath
//            );
//        }
//
//        // 返回静态资源URL（如 "/seal-static/123.png"）
//        String staticAccessUrl = "/seal-static/" + seal.getId() + ".png";
//        seal.setImagePath(staticAccessUrl); // 存储静态资源路径
//        sealMapper.updateById(seal);
//
//        return success(staticAccessUrl); // 返回可直接访问的URL

//        // 3. 更新数据库中的文件路径（关键修改）
//        seal.setImagePath(relativePath);
//        sealMapper.updateById(seal); // 更新路径

//        return success(seal.getId()); // 返回ID供前端使用

//        System.out.println("接收到的 sealType: " + sealDTO.getSealType()); // 调试日志
//        // 1. 生成文件名
//        String fileName = UUID.randomUUID() + ".png";
//        // 2. 构建完整路径
//        String folderPath = storagePath + File.separator + storageFolder;
//        File folder = new File(folderPath);
//        if (!folder.exists()) {
//            folder.mkdirs();
//        }
//
//        String fullPath = folderPath + File.separator + fileName;
//        String relativePath = "/" + storageFolder + "/" + fileName; // 数据库存储相对路径
//
//        // 2. 调用不同的本地存储方法
//        if ("OFFICIAL".equals(sealDTO.getSealType())) {
//            sealService.saveOfficialSealToLocal(
//                    sealDTO.getMainText(),
//                    sealDTO.getViceText(),
//                    sealDTO.getCenterText(),
//                    fullPath
//            );
//        } else {
//            sealService.savePersonalSealToLocal(
//                    sealDTO.getMainText(), // 私章时mainText作为姓名
//                    sealDTO.getAdditionalText(),
//                    fullPath
//            );
//        }
//
//        // 3. 保存到数据库
//
//        // 在拷贝前打印DTO所有字段值
//        System.out.println("DTO内容: " + sealDTO);
//
//        Seal seal = new Seal();
//        BeanUtils.copyProperties(sealDTO, seal);
//
//        // 在拷贝后打印Entity所有字段值
//        System.out.println("拷贝后的Seal: " + seal);
//
////        // 显式检查sealType是否拷贝成功
////        if(seal.getSealType() == null) {
////            seal.setSealType(sealDTO.getSealType()); // 手动补救
////        }
//
//        seal.setImagePath(relativePath); // 存储文件路径
////        seal.setCreateTime(LocalDateTime.now());
//        return success(sealMapper.insert(seal));
//    }







}
