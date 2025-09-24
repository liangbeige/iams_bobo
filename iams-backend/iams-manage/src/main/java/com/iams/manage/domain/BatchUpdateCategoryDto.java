package com.iams.manage.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 批量更新文档门类DTO
 *
 * @author zhjm
 * @date 2025-01-10
 */
@Setter
@Getter
public class BatchUpdateCategoryDto
{
    /** 文档ID列表 */
    private List<Long> docIds;

    /** 门类代码 */
    private String categoryCode;

    /** 门类名称 */
    private String categoryName;

}