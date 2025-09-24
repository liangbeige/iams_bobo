import request from '@/utils/request'

// 查询温度感知列表
export function listThreshold(query) {
  return request({
    url: '/system/threshold/list',
    method: 'get',
    params: query
  })
}

// 查询温度感知详细
export function getThreshold(id) {
  return request({
    url: '/system/threshold/' + id,
    method: 'get'
  })
}

// 新增温度感知
export function addThreshold(data) {
  return request({
    url: '/system/threshold',
    method: 'post',
    data: data
  })
}

// 修改温度感知
export function updateThreshold(data) {
  return request({
    url: '/system/threshold',
    method: 'put',
    data: data
  })
}

// 删除温度感知
export function delThreshold(id) {
  return request({
    url: '/system/threshold/' + id,
    method: 'delete'
  })
}
