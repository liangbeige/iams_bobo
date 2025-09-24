package com.iams.manage.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;

/**
 * 水印管理对象 tb_watermark
 * 
 * @author zhjm
 * @date 2025-01-05
 */
public class Watermark extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 水印ID */
    @Excel(name = "水印ID")
    private Long id;

    /** 水印文本内容 */
    @Excel(name = "水印文本内容")
    private String content;

    /** 水印文本颜色 */
    @Excel(name = "水印文本颜色")
    private String color;

    /** 水印文本字体大小 */
    @Excel(name = "水印文本字体大小")
    private Long fontsize;

    /** 水印文本字重 */
    @Excel(name = "水印文本字重")
    private String fontweight;

    /** 水印文本字体 */
    @Excel(name = "水印文本字体")
    private String fontfamily;

    /** 水印文本字体样式 */
    @Excel(name = "水印文本字体样式")
    private String fontstyle;

    /** 旋转角度 */
    @Excel(name = "旋转角度")
    private Long rotate;

    /** 水平间距 */
    @Excel(name = "水平间距")
    private Long gapX;

    /** 垂直间距 */
    @Excel(name = "垂直间距")
    private Long gapY;

    /** 水平偏移 */
    @Excel(name = "水平偏移")
    private Long offsetX;

    /** 垂直偏移 */
    @Excel(name = "垂直偏移")
    private Long offsetY;

    /** 默认水印id */
    @Excel(name = "默认水印id")
    private Long defaultId;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setContent(String content) 
    {
        this.content = content;
    }

    public String getContent() 
    {
        return content;
    }
    public void setColor(String color) 
    {
        this.color = color;
    }

    public String getColor() 
    {
        return color;
    }
    public void setFontsize(Long fontsize) 
    {
        this.fontsize = fontsize;
    }

    public Long getFontsize() 
    {
        return fontsize;
    }
    public void setFontweight(String fontweight) 
    {
        this.fontweight = fontweight;
    }

    public String getFontweight() 
    {
        return fontweight;
    }
    public void setFontfamily(String fontfamily) 
    {
        this.fontfamily = fontfamily;
    }

    public String getFontfamily() 
    {
        return fontfamily;
    }
    public void setFontstyle(String fontstyle) 
    {
        this.fontstyle = fontstyle;
    }

    public String getFontstyle() 
    {
        return fontstyle;
    }
    public void setRotate(Long rotate) 
    {
        this.rotate = rotate;
    }

    public Long getRotate() 
    {
        return rotate;
    }
    public void setGapX(Long gapX) 
    {
        this.gapX = gapX;
    }

    public Long getGapX() 
    {
        return gapX;
    }
    public void setGapY(Long gapY) 
    {
        this.gapY = gapY;
    }

    public Long getGapY() 
    {
        return gapY;
    }
    public void setOffsetX(Long offsetX) 
    {
        this.offsetX = offsetX;
    }

    public Long getOffsetX() 
    {
        return offsetX;
    }
    public void setOffsetY(Long offsetY) 
    {
        this.offsetY = offsetY;
    }

    public Long getOffsetY() 
    {
        return offsetY;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("content", getContent())
            .append("color", getColor())
            .append("fontsize", getFontsize())
            .append("fontweight", getFontweight())
            .append("fontfamily", getFontfamily())
            .append("fontstyle", getFontstyle())
            .append("rotate", getRotate())
            .append("gapX", getGapX())
            .append("gapY", getGapY())
            .append("offsetX", getOffsetX())
            .append("offsetY", getOffsetY())
            .append("createTime", getCreateTime())
            .append("createBy", getCreateBy())
            .append("updateTime", getUpdateTime())
            .append("updateBy", getUpdateBy())
            .append("remark", getRemark())
            .append("defaultId", getDefaultId())
            .toString();
    }

    public Long getDefaultId() {
        return defaultId;
    }

    public void setDefaultId(Long defaultId) {
        this.defaultId = defaultId;
    }
}
