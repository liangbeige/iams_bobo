import request from '@/utils/request'

// 查询组卷管理列表
export function listVolume(query) {
  return request({
    url: '/manage/volume/list',
    method: 'get',
    params: query
  })
}

// 查询组卷管理详细
export function getVolume(id) {
  return request({
    url: '/manage/volume/' + id,
    method: 'get'
  })
}

// 新增组卷管理
export function addVolume(data) {
  return request({
    url: '/manage/volume',
    method: 'post',
    data: data
  })
}

// 修改组卷管理
export function updateVolume(data) {
  return request({
    url: '/manage/volume',
    method: 'put',
    data: data
  })
}

// 删除组卷管理
export function delVolume(id) {
  return request({
    url: '/manage/volume/' + id,
    method: 'delete'
  })
}
