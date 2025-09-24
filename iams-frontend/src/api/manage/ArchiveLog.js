import request from '@/utils/request'

// 查询档案日志列表
export function listArchiveLog(query) {
  return request({
    url: '/manage/ArchiveLog/list',
    method: 'get',
    params: query
  })
}

// 查询档案操作日志列表
export function listOpLogs(query) {
  return request({
    url: '/manage/ArchiveLog/opLogs',
    method: 'get',
    params: query
  })
}

// 查询档案借阅日志列表
export function listBorrowLogs(query) {
  return request({
    url: '/manage/ArchiveLog/borrowLogs',
    method: 'get',
    params: query
  })
}

// 查询档案日志详细
export function getArchiveLog(id) {
  return request({
    url: '/manage/ArchiveLog/' + id,
    method: 'get'
  })
}

// 新增档案日志
export function addArchiveLog(data) {
  return request({
    url: '/manage/ArchiveLog',
    method: 'post',
    data: data
  })
}

// 修改档案日志
export function updateArchiveLog(data) {
  return request({
    url: '/manage/ArchiveLog',
    method: 'put',
    data: data
  })
}

// 删除档案日志
export function delArchiveLog(id) {
  return request({
    url: '/manage/ArchiveLog/' + id,
    method: 'delete'
  })
}
