import request from '@/utils/request'

// 查询目录管理列表
export function listDirectory(query) {
  return request({
    url: '/manage/directory/list',
    method: 'get',
    params: query
  })
}

// 查询目录管理详细
export function getDirectory(id) {
  return request({
    url: '/manage/directory/' + id,
    method: 'get'
  })
}

// 新增目录管理
export function addDirectory(data) {
  return request({
    url: '/manage/directory',
    method: 'post',
    data: data
  })
}

// 修改目录管理
export function updateDirectory(data) {
  return request({
    url: '/manage/directory',
    method: 'put',
    data: data
  })
}

// 删除目录管理
export function delDirectory(id) {
  return request({
    url: '/manage/directory/' + id,
    method: 'delete'
  })
}
