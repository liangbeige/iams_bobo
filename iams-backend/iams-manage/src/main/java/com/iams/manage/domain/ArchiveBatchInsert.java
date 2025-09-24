package com.iams.manage.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Setter
@Getter
public class ArchiveBatchInsert {

    private static final long serialVersionUID = 1L;
    private Long projectId;
    private List<Archive> records;

}
