package com.iams.manage.service.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Base64;

/**
 * 图片转化base64后再UrlEncode结果
 */
public class BaseImg64 {

    /**
     * 将一张本地图片转化成Base64字符串
     */
    public static String getImageStrFromPath(String imgPath) {
        InputStream in = null;
        byte[] data = null;
        try {
            in = new FileInputStream(imgPath);
            data = in.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 对字节数组Base64编码
        String base64Encoded = Base64.getEncoder().encodeToString(data);

        // 返回Base64编码过再URLEncode的字节数组字符串
        try {
            return URLEncoder.encode(base64Encoded, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
