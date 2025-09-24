import request from '@/utils/request'

// 查询排风扇设备信息列表
export function listIot_exhaust_fun(query) {
  return request({
    url: '/system/iot_exhaust_fun/list',
    method: 'get',
    params: query
  })
}

// 查询排风扇设备信息详细
export function getIot_exhaust_fun(deviceId) {
  return request({
    url: '/system/iot_exhaust_fun/' + deviceId,
    method: 'get'
  })
}

// 新增排风扇设备信息
export function addIot_exhaust_fun(data) {
  return request({
    url: '/system/iot_exhaust_fun',
    method: 'post',
    data: data
  })
}

// 修改排风扇设备信息
export function updateIot_exhaust_fun(data) {
  return request({
    url: '/system/iot_exhaust_fun',
    method: 'put',
    data: data
  })
}

// 删除排风扇设备信息
export function delIot_exhaust_fun(deviceId) {
  return request({
    url: '/system/iot_exhaust_fun/' + deviceId,
    method: 'delete'
  })
}

export function FunPowerOnOff(remote) {
  return request({
    url: '/system/iot_exhaust_fun/FunPowerOnOff',
    method: 'post',
    data: remote
  })
}

export function updateFanSpeed(remote) {
  return request({
    url: '/system/iot_exhaust_fun/updateFanSpeed',
    method: 'post',
    data: remote
  })
}
