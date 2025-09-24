import request from '@/utils/request'

// 查询存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等列表
export function listIot_camera_device_info(query) {
  return request({
    url: '/system/iot_camera_device_info/list',
    method: 'get',
    params: query
  })
}

// 查询存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等详细
export function getIot_camera_device_info(deviceId) {
  return request({
    url: '/system/iot_camera_device_info/' + deviceId,
    method: 'get'
  })
}

// 新增存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等
export function addIot_camera_device_info(data) {
  return request({
    url: '/system/iot_camera_device_info',
    method: 'post',
    data: data
  })
}

// 修改存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等
export function updateIot_camera_device_info(data) {
  return request({
    url: '/system/iot_camera_device_info',
    method: 'put',
    data: data
  })
}

// 删除存储摄像头设备的详细信息，包括基本信息、状态、安装和维护信息等
export function delIot_camera_device_info(deviceId) {
  return request({
    url: '/system/iot_camera_device_info/' + deviceId,
    method: 'delete'
  })
}
