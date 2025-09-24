package com.iams.manage.service.impl;


import com.iams.manage.config.ReminderWebSocketHandler;
import com.iams.manage.mapper.ReminderMessageMapper;
import com.iams.manage.service.IReminderMessageService;
import com.iams.manage.domain.ReminderMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ReminderMessageServiceImpl implements IReminderMessageService {

    @Autowired
    private ReminderMessageMapper reminderMessageMapper;

    @Autowired
    private ReminderWebSocketHandler webSocketHandler;

    @Override
    public List<ReminderMessage> selectReminderMessageList(ReminderMessage reminderMessage) {
        return reminderMessageMapper.selectReminderMessageList(reminderMessage);
    }

    @Override
    public ReminderMessage selectReminderMessageById(Long id) {
        return reminderMessageMapper.selectReminderMessageById(id);
    }

    @Override
    public int insertReminderMessage(ReminderMessage reminderMessage) {
        reminderMessage.setCreateTime(new Date());
        return reminderMessageMapper.insertReminderMessage(reminderMessage);
    }

    @Override
    public int updateReminderMessage(ReminderMessage reminderMessage) {
        return reminderMessageMapper.updateReminderMessage(reminderMessage);
    }

    @Override
    public int deleteReminderMessageByIds(Long[] ids) {
        return reminderMessageMapper.deleteReminderMessageByIds(ids);
    }

    @Override
    public int deleteReminderMessageById(Long id) {
        return reminderMessageMapper.deleteReminderMessageById(id);
    }

    @Override
    public void sendReminderMessage(Long userId, String title, String content, String taskId, String processInstanceId) {
        ReminderMessage message = new ReminderMessage();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setTaskId(taskId);
        message.setProcessInstanceId(processInstanceId);
        message.setMessageType("REMINDER");
        message.setCreateTime(new Date());
        message.setIsRead(false);

        // 保存到数据库
        reminderMessageMapper.insertReminderMessage(message);

        // 如果用户在线，实时推送
        if (webSocketHandler.isUserOnline(userId)) {
            webSocketHandler.sendReminderToUser(userId, message);
        }
    }

    @Override
    public List<ReminderMessage> getUnreadMessages(Long userId) {
        return reminderMessageMapper.selectUnreadMessagesByUserId(userId);
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return reminderMessageMapper.countUnreadMessagesByUserId(userId);
    }

    @Override
    public void markMessageAsRead(Long messageId) {
        reminderMessageMapper.markMessageAsRead(messageId);
    }

    @Override
    public void markAllMessagesAsRead(Long userId) {
        ReminderMessage queryMessage = new ReminderMessage();
        queryMessage.setUserId(userId);
        queryMessage.setIsRead(false);

        List<ReminderMessage> unreadMessages = reminderMessageMapper.selectReminderMessageList(queryMessage);
        for (ReminderMessage message : unreadMessages) {
            reminderMessageMapper.markMessageAsRead(message.getId());
        }
    }
}