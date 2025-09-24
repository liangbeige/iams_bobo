import request from '@/utils/request'

// 查询空调设备信息，存储空调的静态属性及实时状态列表
export function listIot_air_conditioner(query) {
  return request({
    url: '/system/iot_air_conditioner/list',
    method: 'get',
    params: query
  })
}

// 查询空调设备信息，存储空调的静态属性及实时状态详细
export function getIot_air_conditioner(id) {
  return request({
    url: '/system/iot_air_conditioner/' + id,
    method: 'get'
  })
}

// 新增空调设备信息，存储空调的静态属性及实时状态
export function addIot_air_conditioner(data) {
  return request({
    url: '/system/iot_air_conditioner',
    method: 'post',
    data: data
  })
}

// 修改空调设备信息，存储空调的静态属性及实时状态
export function updateIot_air_conditioner(data) {
  return request({
    url: '/system/iot_air_conditioner',
    method: 'put',
    data: data
  })
}

// 删除空调设备信息，存储空调的静态属性及实时状态
export function delIot_air_conditioner(id) {
  return request({
    url: '/system/iot_air_conditioner/' + id,
    method: 'delete'
  })
}

export function PowerOnOff(remote) {
  return request({
    url: '/system/iot_air_conditioner/PowerOnOff',
    method: 'post',
    data: remote
  })
}

export function changeAirTemp(remote) {
  return request({
    url: '/system/iot_air_conditioner/changeAirTemp',
    method: 'post',
    data: remote
  })
}

export function updateMode(remote) {
  return request({
    url: '/system/iot_air_conditioner/updateMode',
    method: 'post',
    data: remote
  })
}

export function updateFanSpeed(remote) {
  return request({
    url: '/system/iot_air_conditioner/updateFanSpeed',
    method: 'post',
    data: remote
  })
}
