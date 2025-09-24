package com.iams.manage.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iams.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ArchiveHash extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long archiveId;

    private String authenticityHash;

    private String integrityHash;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastCheckTime;

    private Integer checkCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private String configCharacteristics;
}
