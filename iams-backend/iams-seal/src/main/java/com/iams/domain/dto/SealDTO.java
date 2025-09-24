package com.iams.domain.dto;

import lombok.Data;

@Data
public class SealDTO {
    private String sealType; // OFFICIAL/PERSONAL
    private String sealName;

    // 公章字段
    private String mainText;
    private String viceText;
    private String centerText;

    // 私章字段
    private String additionalText;

    // 公共字段
    private String mainFontFamily;
    private String color;
//    private Integer imageSize;
    private Integer mainFontSize = 25; // 与Entity默认值一致

    private String imagePath;


    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}