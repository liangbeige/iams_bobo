import request from '@/utils/request'

// 查询文档信息列表
export function listDocument(query) {
  return request({
    url: '/manage/document/list',
    method: 'get',
    params: query
  })
}

export function listDocumentByProjectId(query) {
  return request({
    url: '/manage/document/selectByProjectId',
    method: 'get',
    params: query
  })
}

export function listDocumentByArchiveId(query) {
  return request({
    url: '/manage/document/list-by-archiveId',
    method: 'get',
    params: { archiveId: query }
  })
}

// 查询文档信息列表
export function listDocumentByIds(query) {
  return request({
    url: '/manage/document/list/by/ids',
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

// 查询文档最大序号
export function getMaxXuhao(id) {
  return request({
    url: '/manage/document/xuhao/' + id,
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

// 修改文档的额外信息
export function updateDocExtraInfo(data) {
  return request({
    url: '/manage/document/extraInfo',
    method: 'put',
    headers: {
      'Content-Type': 'application/json'
    },
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

// 传递图片到后台ocr识别
export function doOcr(data) {
  return request({
    url: '/doOcr',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    method: 'post',
    data: data
  })
}

// 传递图片到后台ocr识别
export function fullTextOcr(fileName) {
  return request({
    url: '/fullTextOcr',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    method: 'post',
    params: {fileName: fileName},
    timeout: 200000 // 设置超时时间为 10 秒（单位是毫秒）
  })
}

// api/manage/document.js
// 批量更新文档门类
export function batchUpdateDocumentCategory(data) {
  return request({
    url: '/manage/document/batchUpdateCategory',
    method: 'put',
    data: data
  })
}

// 获取没做ocr的文档列表
export function getNoOcrDocumentList(query) {
  return request({
    url: '/manage/document/noOcr',
    method: 'get',
    params: query
  })
}

// 获取做了ocr的文档列表
export function getOcredDocumentList(query) {
  return request({
    url: '/manage/document/ocred',
    method: 'get',
    params: query
  })
}

// 执行批量OCR
export function doBatchOcr() {
  return request({
    url: '/manage/document/batchOcr',
    method: 'get',
  })
}