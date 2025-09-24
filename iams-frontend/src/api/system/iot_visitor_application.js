import request from '@/utils/request'

// 查询申请管理列表
export function listIot_visitor_application(query) {
  return request({
    url: '/system/iot_visitor_application/list',
    method: 'get',
    params: query
  })
}

// add查询申请管理列表
export function listIot_visitor_addApplication(query) {
  return request({
    url: '/system/iot_visitor_addApplication/list',
    method: 'get',
    params: query
  })
}

// add查询最大申请编号
export function getMaxApplicationId() {
  return request({
    url: '/system/iot_visitor_addApplication/max',
    method: 'get'
  })
}
// 查询申请管理详细
export function getIot_visitor_application(applicationId) {
  return request({
    url: '/system/iot_visitor_application/' + applicationId,
    method: 'get'
  })
}
// add查询申请管理详细
export function getIot_visitor_addApplication(applicationId) {
  return request({
    url: '/system/iot_visitor_addApplication/' + applicationId,
    method: 'get'
  })
}

// 新增申请管理
export function addIot_visitor_application(data) {
  return request({
    url: '/system/iot_visitor_application',
    method: 'post',
    data: data
  })
}

// add新增申请管理
export function addIot_visitor_addApplication(data) {
  return request({
    url: '/system/iot_visitor_addApplication',
    method: 'post',
    data: data
  })
}

// 修改申请管理
export function updateIot_visitor_application(data) {
  return request({
    url: '/system/iot_visitor_application',
    method: 'put',
    data: data
  })
}

// 删除申请管理
export function delIot_visitor_application(applicationId) {
  return request({
    url: '/system/iot_visitor_application/' + applicationId,
    method: 'delete'
  })
}

// 标记单条为已读
export function markAsRead(applicationId) {
  return request({
    url: '/system/iot_visitor_application/markAsRead/' + applicationId,
    method: 'put'
  });
}

// 标记全部为已读
export function markAllAsRead() {
  return request({
    url: '/system/iot_visitor_application/markAllAsRead',
    method: 'put'
  });
}

// 获取未读数量
export function getUnreadCount() {
  return request({
    url: '/system/iot_visitor_application/countUnread',
    method: 'get'
  });
}

