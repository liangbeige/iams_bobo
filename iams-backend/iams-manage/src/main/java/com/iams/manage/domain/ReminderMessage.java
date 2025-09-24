package com.iams.manage.domain;


import com.iams.common.annotation.Excel;
import com.iams.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class ReminderMessage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Excel(name = "消息ID")
    private Long id; // 消息ID

    @Excel(name = "用户ID")
    private Long userId; // 用户ID

    @Excel(name = "消息标题")
    private String title; // 消息标题

    @Excel(name = "消息内容")
    private String content; // 消息内容

    @Excel(name = "任务ID")
    private String taskId; // 任务ID

    @Excel(name = "流程实例ID")
    private String processInstanceId; // 流程实例ID

    @Excel(name = "业务键")
    private String businessKey; // 业务键

    @Excel(name = "是否已读")
    private Boolean isRead; // 是否已读(0-未读 1-已读)

    @Excel(name = "消息类型")
    private String messageType; // 消息类型

    @Excel(name = "创建时间")
    private Date createTime; // 创建时间

    @Excel(name = "已读时间")
    private Date readTime; // 已读时间

}
