package com.iams.manage.domain.TaskDTO;

import com.iams.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ActFormDataDTO extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 控件ID与类型 */
    private String controlId;
    private String controlType;

    /** 控件标签 */
    private String controlLabel;

    /** 是否设置为流程参数 */
    private String controlIsParam;

    /** 表单值 */
    private String controlValue;
    private String controlDefault;
}
