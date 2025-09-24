package com.iams.manage.domain.StatisticsVO;

import lombok.Data;

@Data
public class UserCount {
    private String skuName;
    private Integer count;
    private String skuId;
    private Integer amount;

    public UserCount(String key, Integer value) {
    }

    // constructors, getters and setters...
}
