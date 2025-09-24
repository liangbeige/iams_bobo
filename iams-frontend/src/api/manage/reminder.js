import request from "@/utils/request.js";

// 查询催办消息列表
export function listReminderMessage(query) {
    return request({
        url: '/manage/reminderMessage/list',
        method: 'get',
        params: query
    })
}

// 查询催办消息详细
export function getReminderMessage(id) {
    return request({
        url: '/manage/reminderMessage/' + id,
        method: 'get'
    })
}

// 新增催办消息
export function addReminderMessage(data) {
    return request({
        url: '/manage/reminderMessage',
        method: 'post',
        data: data
    })
}

// 修改催办消息
export function updateReminderMessage(data) {
    return request({
        url: '/manage/reminderMessage',
        method: 'put',
        data: data
    })
}

// 删除催办消息
export function delReminderMessage(id) {
    return request({
        url: '/manage/reminderMessage/' + id,
        method: 'delete'
    })
}

// 获取未读消息列表
export function getUnreadMessages() {
    return request({
        url: '/manage/reminderMessage/unread',
        method: 'get'
    })
}

// 获取未读消息数量
export function getUnreadCount() {
    return request({
        url: '/manage/reminderMessage/unread/count',
        method: 'get'
    })
}

// 标记消息已读
export function markMessageAsRead(messageId) {
    return request({
        url: `/manage/reminderMessage/read/${messageId}`,
        method: 'post'
    })
}

// 全部标记已读
export function markAllMessagesAsRead() {
    return request({
        url: '/manage/reminderMessage/read/all',
        method: 'post'
    })
}

// 发送催办消息
export function sendReminderMessage(data) {
    return request({
        url: '/manage/reminderMessagesend',
        method: 'post',
        data: data
    })
}
