package com.iams.manage.domain;

import com.iams.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class Compartment extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String bianhao;
    private String name;
    private Long cabinetId;
    private Long leNo;
    private Long divNo;
    private Long capacity;
    private Long size;
    private String createBy;
    private String updateBy;
    private Date createTime;
    private Date updateTime;
}
