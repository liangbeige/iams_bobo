import request from '@/utils/request'

// ==================== 档案列表相关 ====================
/**
 * 查询可销毁的档案列表（已归档且在架）
 */
export function listAvailableArchives(query) {
  return request({
    url: '/manage/archiveDestroy/availableArchives',
    method: 'get',
    params: query
  })
}


/**
 * 提交销毁申请（单个档案）
 */
export function submitDestroyApplication(data) {
  return request({
    url: '/manage/archiveDestroy/submit',
    method: 'post',
    data: data
  })
}

/**
 * 批量提交销毁申请
 */
export function batchSubmitDestroyApplication(data) {
  return request({
    url: '/manage/archiveDestroy/batchSubmit',
    method: 'post',
    data: data
  })
}

// ==================== 销毁确认相关 ====================
// /**
//  * 确认完成销毁（单个档案）
//  */
// export function completeDestroy(archiveId) {
//   return request({
//     url: '/manage/archiveDestroy/complete/' + archiveId,
//     method: 'post'
//   })
// }

/**
 * 批量确认完成销毁
 */
export function batchCompleteDestroy(archiveIds) {
  return request({
    url: '/manage/archiveDestroy/batchComplete',
    method: 'post',
    data: archiveIds
  })
}

/**
 * 标记销毁失败
 */
export function failDestroy(archiveId, reason) {
  return request({
    url: '/manage/archiveDestroy/fail/' + archiveId,
    method: 'post',
    params: { reason }
  })
}

// ==================== 销毁记录管理相关 ====================
/**
 * 查询销毁记录列表
 */
export function listDestroyRecords(query) {
  return request({
    url: '/manage/archiveDestroy/records',
    method: 'get',
    params: query
  })
}

/**
 * 获取销毁记录详细信息
 */
export function getDestroyRecord(id) {
  return request({
    url: '/manage/archiveDestroy/records/' + id,
    method: 'get'
  })
}

/**
 * 取消销毁申请（仅限待下架状态）
 */
export function cancelDestroyApplication(id) {
  return request({
    url: '/manage/archiveDestroy/cancel/' + id,
    method: 'post'
  })
}

/**
 * 删除销毁记录
 */
export function delDestroyRecord(ids) {
  return request({
    url: '/manage/archiveDestroy/records/' + ids,
    method: 'delete'
  })
}

// ==================== 数据导出相关 ====================
/**
 * 导出销毁记录
 */
export function exportDestroyRecord(query) {
  return request({
    url: '/manage/archiveDestroy/export',
    method: 'post',
    params: query
  })
}

// ==================== RFID同步相关 ====================
/**
 * 同步离线销毁操作（盘点车离线下架后的同步）
 */
export function syncOfflineDestroy(archiveId) {
  return request({
    url: '/manage/archiveDestroy/sync/' + archiveId,
    method: 'post'
  })
}

// ==================== 废弃的接口（保留以防其他地方调用）====================
/**
 * @deprecated 请使用 submitDestroyApplication
 */
export function addArchiveDestroyRecord(data) {
  console.warn('addArchiveDestroyRecord 已废弃，请使用 submitDestroyApplication')
  return submitDestroyApplication(data)
}

/**
 * @deprecated 请使用 listDestroyRecords
 */
export function getArchiveDestroyRecord(id) {
  console.warn('getArchiveDestroyRecord 已废弃，请使用 getDestroyRecord')
  return getDestroyRecord(id)
}

/**
 * @deprecated 请使用 completeDestroy
 */
export function completeArchiveDestroy(archiveId) {
  console.warn('completeArchiveDestroy 已废弃，请使用 completeDestroy')
  return completeDestroy(archiveId)
}

/**
 * @deprecated 暂不支持修改销毁记录
 */
export function updateArchiveDestroyRecord(data) {
  console.warn('updateArchiveDestroyRecord 已废弃，暂不支持修改销毁记录')
  return Promise.reject(new Error('暂不支持修改销毁记录'))
}

/**
 * 确认完成销毁（单个档案）
 */
export function completeDestroy(archiveId) {
  return request({
    url: '/manage/archiveDestroy/complete/' + archiveId,
    method: 'post'
  })
}

/**
 * 根据档案ID撤销销毁申请
 */
export function cancelDestroyByArchiveId(archiveId) {
  return request({
    url: '/manage/archiveDestroy/cancelByArchiveId/' + archiveId,
    method: 'post'
  })
}
