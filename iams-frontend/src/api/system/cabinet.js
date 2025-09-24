import request from '@/utils/request'

export function saveCabinetConfig(data) {
  return request({
    url: '/system/cabinetConfig/save',
    method: 'post',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded' // 明确指定表单格式
    },
    data: data
  })
}

export function getCabinetConfigList(params) {
  return request({
    url: '/system/cabinetConfig/list',
    method: 'get',
    params: params
  })
}
