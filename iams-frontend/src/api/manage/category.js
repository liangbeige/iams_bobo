import request from '@/utils/request'

// 查询档案门类管理列表
export function listCategory(query) {
  return request({
    url: '/manage/category/list',
    method: 'get',
    params: query
  })
}

// 查询档案门类管理详细
export function getCategory(id) {
  return request({
    url: '/manage/category/' + id,
    method: 'get'
  })
}

export function getCategoryName(id) {
  return request({
    url: '/manage/category/name/' + id,
    method: 'get'
  })
}


// 获取全宗门类配置
export function getFondsCategories(fondsId) {
  return request({
    url: `/manage/fonds/category-config/${fondsId}`,
    method: 'get'
  })
}

// 保存全宗门类配置
export function saveFondsCategories(fondsId, categoryCodes) {
  return request({
    url: `/manage/fonds/category-config/${fondsId}`,
    method: 'post',
    data: { categoryCodes }
  })
}

// 新增档案门类管理
export function addCategory(data) {
  return request({
    url: '/manage/category',
    method: 'post',
    data: data
  })
}

// 修改档案门类管理
export function updateCategory(data) {
  return request({
    url: '/manage/category',
    method: 'put',
    data: data
  })
}

// 删除档案门类管理
export function delCategory(id) {
  return request({
    url: '/manage/category/' + id,
    method: 'delete'
  })
}
