import request from '@/utils/request'

// 查询密集柜信息，用于存储密集柜的基本信息及其配置列表
export function listCabinets(query) {
  return request({
    url: '/system/cabinets/list',
    method: 'get',
    params: query
  })
}

// 查询密集柜信息，用于存储密集柜的基本信息及其配置详细
export function getCabinets(cabinetId) {
  return request({
    url: '/system/cabinets/' + cabinetId,
    method: 'get'
  })
}

// 新增密集柜信息，用于存储密集柜的基本信息及其配置
export function addCabinets(data) {
  return request({
    url: '/system/cabinets',
    method: 'post',
    data: data
  })
}

// 修改密集柜信息，用于存储密集柜的基本信息及其配置
export function updateCabinets(data) {
  return request({
    url: '/system/cabinets',
    method: 'put',
    data: data
  })
}

// 删除密集柜信息，用于存储密集柜的基本信息及其配置
export function delCabinets(cabinetId) {
  return request({
    url: '/system/cabinets/' + cabinetId,
    method: 'delete'
  })
}
