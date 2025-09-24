package com.iams.manage.service;

import com.iams.manage.domain.ReminderMessage;
import java.util.List;


public interface IReminderMessageService {

    List<ReminderMessage> selectReminderMessageList(ReminderMessage reminderMessage);

    ReminderMessage selectReminderMessageById(Long id);

    int insertReminderMessage(ReminderMessage reminderMessage);

    int updateReminderMessage(ReminderMessage reminderMessage);

    int deleteReminderMessageById(Long id);

    int deleteReminderMessageByIds(Long[] ids);

    // 催办相关方法
    public void sendReminderMessage(Long userId, String title, String content, String taskId, String processInstanceId);

    public List<ReminderMessage> getUnreadMessages(Long userId);

    public Long getUnreadCount(Long userId);

    public void markMessageAsRead(Long messageId);

    public void markAllMessagesAsRead(Long userId);

}
