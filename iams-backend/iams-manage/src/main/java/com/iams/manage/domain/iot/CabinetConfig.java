package com.iams.manage.domain.iot;

import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class CabinetConfig extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Excel(name = "主键ID")
    private Long id;

    @Excel(name = "配置数据")
    private String configData;

    @Excel(name = "公司名称")
    private String company;

    @Excel(name = "仓库编号")
    private String warehouse;

    @Excel(name = "方案编号")
    private String scheme;

    public void setId(Long id) { this.id = id; }
    public Long getId() { return id; }

    public void setConfigData(String configData) { this.configData = configData; }
    public String getConfigData() { return configData; }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(String warehouse) {
        this.warehouse = warehouse;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("configData", getConfigData())
                .append("createTime", getCreateTime())
                .toString();
    }
}
