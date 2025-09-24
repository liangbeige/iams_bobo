package com.iams.manage.controller;

import com.baidu.aip.ocr.AipOcr;
import com.iams.common.core.domain.AjaxResult;
import com.iams.manage.domain.BaiduOcr;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.*;

/**
 * OcrController类负责处理OCR相关的请求。
 * 它利用百度OCR服务对上传的文件或文本进行识别，并返回识别结果。
 */

@RestController
public class OcrController {
    // 注入BaiduOcrProperties对象，用于获取百度OCR服务的配置信息
    private final BaiduOcr baiduOcrProperties;
    // 创建一个OkHttpClient对象，用于发送HTTP请求到百度OCR服务
    static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();

    // 构造函数，注入BaiduOcrProperties对象，用于初始化BaiduOcrProperties对象
    @Autowired
    public OcrController(BaiduOcr baiduOcrProperties) {
        this.baiduOcrProperties = baiduOcrProperties;
    }

    @RequestMapping(value = {"/", "/ocr"})
    public String index() {
        return "ocr";
    }
    /**
     * 处理OCR识别请求。
     *
     * @param file 用户上传的文件，将进行OCR识别。
     * @param model Spring模型，用于在识别后向视图传递数据。
     * @return 视图名称，根据识别结果决定是显示结果还是错误页面。
     */
    @PostMapping(value = "/doOcr")
    public AjaxResult ocr(MultipartFile file, Model model) {
        System.out.println(file);
        try {
            List<String> ocrResult = performOcr(file); // 执行OCR识别
//            List<String> ocrResult = new ArrayList<>();
//            for (int i = 0; i < 100; i++)
//                ocrResult.add("测试");
            model.addAttribute("ocrResult", ocrResult); // 将识别结果添加到模型中
        } catch (Exception e) {
            return AjaxResult.error("error"); // 识别失败，返回错误页面
        }
        return AjaxResult.success("ocr_result", model); // 识别成功，返回结果页面
    }

    /**
     * 执行OCR识别操作。
     *
     * @param file 需要进行OCR识别的文件。
     * @return 识别到的文本列表。
     * @throws Exception 如果识别过程中出现错误，则抛出异常。
     */
    private List<String> performOcr(MultipartFile file) throws Exception {
        AipOcr client = new AipOcr(baiduOcrProperties.getAppId(), baiduOcrProperties.getApiKey(), baiduOcrProperties.getSecretKey()); // 创建百度OCR客户端

        // 获取Access Token
        String accessToken = getAccessToken();

        HashMap<String, String> options = new HashMap<>(); // 设置OCR识别的选项
        options.put("language_type", "CHN_ENG");
        options.put("detect_direction", "true");
        options.put("detect_language", "true");
        options.put("probability", "true");

        byte[] buf = file.getBytes(); // 从文件中读取内容
        JSONObject res = client.basicAccurateGeneral(buf, options);  // 使用高精度接口进行通用文字识别

        List<String> wordsList = new ArrayList<>(); // 存储识别出的文本
        for (Object obj : res.getJSONArray("words_result")) { // 遍历识别结果，提取文本
            JSONObject jsonObj = (JSONObject) obj;
            wordsList.add(jsonObj.getString("words"));
        }
        System.out.println(wordsList);
        return wordsList;
    }

    /**
     * 从百度OCR服务获取Access Token。
     *
     * @return Access Token，用于身份验证。
     * @throws IOException 如果在获取Access Token过程中出现IO错误。
     */
    private String getAccessToken() throws IOException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id=" + baiduOcrProperties.getApiKey()
                + "&client_secret=" + baiduOcrProperties.getSecretKey());
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute(); // 发送请求，获取响应
        return new JSONObject(response.body().string()).getString("access_token"); // 从响应中提取Access Token
    }


}

