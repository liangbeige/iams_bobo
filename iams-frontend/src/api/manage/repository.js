import request from '@/utils/request'

// 查询库房管理列表
export function listRepository(query) {
  return request({
    url: '/manage/repository/list',
    method: 'get',
    params: query
  })
}

// 查询库房管理详细
export function getRepository(id) {
  return request({
    url: '/manage/repository/' + id,
    method: 'get'
  })
}

// 新增库房管理
export function addRepository(data) {
  return request({
    url: '/manage/repository',
    method: 'post',
    data: data
  })
}

// 修改库房管理
export function updateRepository(data) {
  return request({
    url: '/manage/repository',
    method: 'put',
    data: data
  })
}

// 删除库房管理
export function delRepository(id) {
  return request({
    url: '/manage/repository/' + id,
    method: 'delete'
  })
}
