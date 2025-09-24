import request from '@/utils/request'

// 查询档案柜管理列表
export function listCabinet(query) {
  return request({
    url: '/manage/cabinet/list',
    method: 'get',
    params: query
  })
}

// 查询档案柜idS
export function getCabinetIds(query) {
  return request({
    url: '/manage/cabinet/get-ids',
    method: 'get',
    params: query
  })
}

// 根据区号列表获取档案柜列表
export function listCabinetByQuNoList(query) {
  return request({
    url: '/manage/cabinet/list-by-qunolist',
    method: 'get',
    params: { quNoList: query }
  })
}

// 查询档案柜管理详细
export function getCabinet(id) {
  return request({
    url: '/manage/cabinet/' + id,
    method: 'get'
  })
}

// 新增档案柜管理
export function addCabinet(data) {
  return request({
    url: '/manage/cabinet',
    method: 'post',
    data: data
  })
}

// 修改档案柜管理
export function updateCabinet(data) {
  return request({
    url: '/manage/cabinet',
    method: 'put',
    data: data
  })
}

// 删除档案柜管理
export function delCabinet(id) {
  return request({
    url: '/manage/cabinet/' + id,
    method: 'delete'
  })
}

// 根据位置修改size
export function updateCabinetSize(query, op) {
  return request({
    url: '/manage/cabinet/size',
    method: 'put',
    params: { shitiLocation: query, option: op }
  })
}

// 同步位置统计数据
export function synchronizeLocationStats(locationData) {
  return request({
    url: '/manage/cabinet/synchronize',
    method: 'post',
    data: locationData
  })
}
