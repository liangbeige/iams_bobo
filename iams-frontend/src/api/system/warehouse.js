import request from '@/utils/request'

// 查询仓库管理列表
export function listWarehouse(query) {
  return request({
    url: '/system/warehouse/list',
    method: 'get',
    params: query
  })
}

// 查询仓库管理详细
export function getWarehouse(warehouseId) {
  return request({
    url: '/system/warehouse/' + warehouseId,
    method: 'get'
  })
}

// 新增仓库管理
export function addWarehouse(data) {
  return request({
    url: '/system/warehouse',
    method: 'post',
    data: data
  })
}

// 修改仓库管理
export function updateWarehouse(data) {
  return request({
    url: '/system/warehouse',
    method: 'put',
    data: data
  })
}

// 删除仓库管理
export function delWarehouse(warehouseId) {
  return request({
    url: '/system/warehouse/' + warehouseId,
    method: 'delete'
  })
}
