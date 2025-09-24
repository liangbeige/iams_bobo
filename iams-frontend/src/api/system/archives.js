import request from '@/utils/request'

// 查询档案信息，用于存储档案的基本信息及其在密集柜中的存储位置列表
export function listArchives(query) {
  return request({
    url: '/system/archives/list',
    method: 'get',
    params: query
  })
}

// 查询档案信息，用于存储档案的基本信息及其在密集柜中的存储位置详细
export function getArchives(archiveId) {
  return request({
    url: '/system/archives/' + archiveId,
    method: 'get'
  })
}

// 新增档案信息，用于存储档案的基本信息及其在密集柜中的存储位置
export function addArchives(data) {
  return request({
    url: '/system/archives',
    method: 'post',
    data: data
  })
}

// 修改档案信息，用于存储档案的基本信息及其在密集柜中的存储位置
export function updateArchives(data) {
  return request({
    url: '/system/archives',
    method: 'put',
    data: data
  })
}

// 删除档案信息，用于存储档案的基本信息及其在密集柜中的存储位置
export function delArchives(archiveId) {
  return request({
    url: '/system/archives/' + archiveId,
    method: 'delete'
  })
}
