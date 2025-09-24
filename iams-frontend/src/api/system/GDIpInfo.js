import request from '@/utils/request'

// 查询固定列IP信息管理列表
export function listInfo(query) {
  return request({
    url: '/system/info/list',
    method: 'get',
    params: query
  })
}

// 查询固定列IP信息管理详细
export function getInfo(gdlNo) {
  return request({
    url: '/system/info/' + gdlNo,
    method: 'get'
  })
}

// 新增固定列IP信息管理
export function addInfo(data) {
  return request({
    url: '/system/info',
    method: 'post',
    data: data
  })
}

// 修改固定列IP信息管理
export function updateInfo(data) {
  return request({
    url: '/system/info',
    method: 'put',
    data: data
  })
}

// 删除固定列IP信息管理
export function delInfo(gdlNo) {
  return request({
    url: '/system/info/' + gdlNo,
    method: 'delete'
  })
}
