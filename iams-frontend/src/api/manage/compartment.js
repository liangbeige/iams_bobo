import request from '@/utils/request'

export function getListByCabinetIds(query) {
  return request({
    url: '/manage/compartment/list-by-cabinetIds',
    method: 'get',
    params: { cabinetIds: query }
  })
}

// 查询储物格管理列表
export function listCompartment(query) {
  return request({
    url: '/manage/compartment/list',
    method: 'get',
    params: query
  })
}

export function updateCompartmentSize(query, op) {
  return request({
    url: '/manage/compartment/size',
    method: 'put',
    params: { location: query, option: op }
  })
}