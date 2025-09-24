package com.iams.manage.controller;


import com.iams.common.core.controller.BaseController;
import com.iams.common.core.domain.AjaxResult;
import com.iams.common.core.page.TableDataInfo;
import com.iams.manage.domain.ReminderMessage;
import com.iams.manage.domain.SendReminderRequest;
import com.iams.manage.service.IReminderMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manage/reminderMessage")
public class ReminderMessageController extends BaseController {

    @Autowired
    private IReminderMessageService reminderMessageService;

    @GetMapping("/list")
    public TableDataInfo list(ReminderMessage reminderMessage)
    {
        startPage();
        List<ReminderMessage> list = reminderMessageService.selectReminderMessageList(reminderMessage);
        return getDataTable(list);
    }

    /*
     * 新增催办消息
     */
    @PostMapping
    public AjaxResult add(@RequestBody ReminderMessage reminderMessage)
    {
        return toAjax(reminderMessageService.insertReminderMessage(reminderMessage));
    }

    /**
     * 获取当前用户未读消息
     */
    @GetMapping("/unread")
    public AjaxResult getUnreadMessages() {
        Long userId = getUserId();
        List<ReminderMessage> messages = reminderMessageService.getUnreadMessages(userId);
        return AjaxResult.success(messages);
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread/count")
    public AjaxResult getUnreadCount() {
        Long userId = getUserId();
        Long count = reminderMessageService.getUnreadCount(userId);
        return AjaxResult.success(count);
    }

    /**
     * 标记消息已读
     */
    @PostMapping("/read/{messageId}")
    public AjaxResult markAsRead(@PathVariable Long messageId) {
        reminderMessageService.markMessageAsRead(messageId);
        return AjaxResult.success();
    }

    /**
     * 全部标记已读
     */
    @PostMapping("/read/all")
    public AjaxResult markAllAsRead() {
        Long userId = getUserId();
        reminderMessageService.markAllMessagesAsRead(userId);
        return AjaxResult.success();
    }

    /**
     * 发送催办消息
     */
    @PostMapping("/send")
    public AjaxResult sendReminder(@RequestBody SendReminderRequest request) {
        reminderMessageService.sendReminderMessage(
                request.getUserId(),
                request.getTitle(),
                request.getContent(),
                request.getTaskId(),
                request.getProcessInstanceId()
        );
        return AjaxResult.success();
    }
}
