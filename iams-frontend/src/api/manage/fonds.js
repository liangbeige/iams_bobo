import request from '@/utils/request'

// 查询全宗管理列表
export function listFonds(query) {
  return request({
    url: '/manage/fonds/list',
    method: 'get',
    params: query
  })
}

// 查询全宗管理详细
export function getFonds(id) {
  return request({
    url: '/manage/fonds/' + id,
    method: 'get'
  })
}

export function UpdateCategoryCodesToCodeName(){
  return request({
    url: '/manage/fonds/CategoryCodesToCodeName',
    method: 'post'
  })
}

// 新增全宗管理
export function addFonds(data) {
  return request({
    url: '/manage/fonds',
    method: 'post',
    data: data
  })
}

// 修改全宗管理
export function updateFonds(data) {
  return request({
    url: '/manage/fonds',
    method: 'put',
    data: data
  })
}

// 删除全宗管理
export function delFonds(id) {
  return request({
    url: '/manage/fonds/' + id,
    method: 'delete'
  })
}
