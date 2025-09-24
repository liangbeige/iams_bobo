import request from '@/utils/request'

// 查询归档申请列表
export function listArchiveFilingApproval(query) {
  return request({
    url: '/manage/archiveFilingApproval/list',
    method: 'get',
    params: query
  })
}

// 查询归档申请详细
export function getArchiveFilingApproval(id) {
  return request({
    url: '/manage/archiveFilingApproval/' + id,
    method: 'get'
  })
}

// 新增归档申请
export function addArchiveFilingApproval(data) {
  return request({
    url: '/manage/archiveFilingApproval',
    method: 'post',
    data: data
  })
}

// 修改归档申请
export function updateArchiveFilingApproval(data) {
  return request({
    url: '/manage/archiveFilingApproval',
    method: 'put',
    data: data
  })
}

// 删除归档申请
export function delArchiveFilingApproval(id) {
  return request({
    url: '/manage/archiveFilingApproval/' + id,
    method: 'delete'
  })
}
