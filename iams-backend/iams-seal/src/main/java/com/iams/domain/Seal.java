package com.iams.domain;

import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;
import lombok.Data;

@Data
public class Seal extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 印章ID */
    private Long id;

    /** 印章名称 */
    @Excel(name = "印章名称")
    private String sealName;

    /** 印章类型（OFFICIAL-公章,PERSONAL-私章） */
    @Excel(name = "印章类型", readConverterExp = "OFFICIAL=公章,PERSONAL=私章")
    private String sealType;

    /** 主文字 */
    @Excel(name = "主文字")
    private String mainText;

    /** 副文字 */
    @Excel(name = "副文字")
    private String viceText;

    /** 中心文字 */
    @Excel(name = "中心文字")
    private String centerText;

    /** 附加文字(私章) */
    @Excel(name = "附加文字")
    private String additionalText;

    /** 主文字字体 */
    @Excel(name = "主文字字体")
    private String mainFontFamily = "楷体";

    /** 主文字大小 */
    @Excel(name = "主文字大小")
    private Integer mainFontSize = 25;

    /** 印章颜色 */
    @Excel(name = "印章颜色")
    private String color = "#FF0000";

    /** 印章尺寸 */
    @Excel(name = "印章尺寸")
    private Integer imageSize = 300;

    /** 印章图片路径 */
    @Excel(name = "印章图片")
    private String imagePath; // 添加图片路径属性

    // getter和setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // 添加getter和setter方法
    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    // 其他getter和setter...
}