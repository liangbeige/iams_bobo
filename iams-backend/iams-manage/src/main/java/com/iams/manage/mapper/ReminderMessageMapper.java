package com.iams.manage.mapper;

import com.iams.manage.domain.ReminderMessage;

import java.util.List;

public interface ReminderMessageMapper {

    public List<ReminderMessage> selectReminderMessageList(ReminderMessage reminderMessage);

    public ReminderMessage selectReminderMessageById(Long id);

    public int insertReminderMessage(ReminderMessage reminderMessage);

    public int updateReminderMessage(ReminderMessage reminderMessage);

    public int deleteReminderMessageById(Long id);

    public int deleteReminderMessageByIds(Long[] ids);

    // 自定义查询方法
    public List<ReminderMessage> selectUnreadMessagesByUserId(Long userId);

    public Long countUnreadMessagesByUserId(Long userId);

    public int markMessageAsRead(Long id);

}
