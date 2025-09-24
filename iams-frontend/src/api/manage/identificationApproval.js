import request from '@/utils/request'

// 查询鉴定记录列表
export function listIdentificationRecord(query) {
  return request({
    url: '/manage/identification/list',
    method: 'get',
    params: query
  })
}

// 查询鉴定记录详细
export function getIdentificationRecord(id) {
  return request({
    url: '/manage/identification/' + id,
    method: 'get'
  })
}

// 提交档案鉴定（支持批量）
export function submitIdentificationRecord(data) {
  return request({
    url: '/manage/identification/submit',
    method: 'post',
    data: data
  })
}

// 修改鉴定记录
export function updateIdentificationRecord(data) {
  return request({
    url: '/manage/identification',
    method: 'put',
    data: data
  })
}

// 删除鉴定记录
export function delIdentificationRecord(id) {
  return request({
    url: '/manage/identification/' + id,
    method: 'delete'
  })
}

// 根据档案ID查询鉴定记录
export function getIdentificationRecordByArchiveId(archiveId) {
  return request({
    url: '/manage/identification/archive/' + archiveId,
    method: 'get'
  })
}

// 获取鉴定统计数据
export function getIdentificationStatistics(query) {
  return request({
    url: '/manage/identification/statistics',
    method: 'get',
    params: query
  })
}

// 导出鉴定记录
export function exportIdentificationRecord(query) {
  return request({
    url: '/manage/identification/export',
    method: 'post',
    params: query
  })
}

// ============ 档案鉴定相关接口 ============

// 获取待鉴定档案统计数量（这个需要在Archive相关API中添加）
export function getPendingAppraisalCount() {
  return request({
    url: '/manage/archive/pending/count',
    method: 'get'
  })
}

// 获取待鉴定档案列表（这个需要在Archive相关API中添加）
export function listPendingAppraisalArchives(query) {
  return request({
    url: '/manage/archive/pending/list',
    method: 'get',
    params: query
  })
}

// 销毁档案（这个需要在Archive相关API中添加）
export function destroyArchives(data) {
  return request({
    url: '/manage/archive/destroy',
    method: 'post',
    data: data
  })
}