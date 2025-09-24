import request from '@/utils/request'

// 查询文档信息列表
export function listDocument(query) {
  return request({
    url: '/manage/document/list',
    method: 'get',
    params: query
  })
}

// 查询文档信息详细
export function getDocument(id) {
  return request({
    url: '/manage/document/' + id,
    method: 'get'
  })
}

// 新增文档信息
export function addDocument(data) {
  return request({
    url: '/manage/document',
    method: 'post',
    data: data
  })
}

// 修改文档信息
export function updateDocument(data) {
  return request({
    url: '/manage/document',
    method: 'put',
    data: data
  })
}

// 删除文档信息
export function delDocument(id) {
  return request({
    url: '/manage/document/' + id,
    method: 'delete'
  })
}
