import request from '@/utils/request'

// 查询水印管理列表
export function listWatermark(query) {
  return request({
    url: '/manage/watermark/list',
    method: 'get',
    params: query
  })
}

// 查询水印管理详细
export function getWatermark(id) {
  return request({
    url: '/manage/watermark/' + id,
    method: 'get'
  })
}

// 新增水印管理
export function addWatermark(data) {
  return request({
    url: '/manage/watermark',
    method: 'post',
    data: data
  })
}

// 修改水印管理
export function updateWatermark(data) {
  return request({
    url: '/manage/watermark',
    method: 'put',
    data: data
  })
}

// 删除水印管理
export function delWatermark(id) {
  return request({
    url: '/manage/watermark/' + id,
    method: 'delete'
  })
}

// 设置默认水印
export function setDefaultWatermark(id) {
  return request({
    url: '/manage/watermark/setDefault/' + id,
    method: 'put'
  })
}

// 获取默认水印id
export function getDefaultWatermark() {
  return request({
    url: '/manage/watermark/getDefault',
    method: 'get'
  })
}