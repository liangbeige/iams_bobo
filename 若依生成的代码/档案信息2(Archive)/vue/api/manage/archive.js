import request from '@/utils/request'

// 查询档案列表列表
export function listArchive(query) {
  return request({
    url: '/manage/archive/list',
    method: 'get',
    params: query
  })
}

// 查询档案列表详细
export function getArchive(id) {
  return request({
    url: '/manage/archive/' + id,
    method: 'get'
  })
}

// 新增档案列表
export function addArchive(data) {
  return request({
    url: '/manage/archive',
    method: 'post',
    data: data
  })
}

// 修改档案列表
export function updateArchive(data) {
  return request({
    url: '/manage/archive',
    method: 'put',
    data: data
  })
}

// 删除档案列表
export function delArchive(id) {
  return request({
    url: '/manage/archive/' + id,
    method: 'delete'
  })
}
