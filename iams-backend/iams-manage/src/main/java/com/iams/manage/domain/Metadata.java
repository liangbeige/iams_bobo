package com.iams.manage.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Metadata implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 元数据id */
    private Integer id;

    /** 元数据名称 */
    private String name;

    /** 元数据编码 */
    private String code;

    /** 关键字 */
    private String keywords;

}
