// api/test/category.js

import request from '@/utils/request'

// 获取根节点列表
export function getCategoryRoots(query) {
    return request({
        url: '/manage/category/roots',
        method: 'get',
        params: query

    })
}

// // 新增：根据 uniqueKey 获取根节点ID
// export function getRootIdByUniqueKey(uniqueKey) {
//     return request({
//         url: '/manage/category/root-id/' + uniqueKey,
//         method: 'get'
//     })
// }

// 新增：根据 uniqueKey 获取门类树
export function getCategoryTreeByUniqueKey(uniqueKey) {
    return request({
        url: '/manage/category/by-unique-key/' + uniqueKey,
        method: 'get'
    })
}

// 获取分类树
export function getCategoryTree(rootId) {
    return request({
        url: '/manage/category/tree/' + rootId,
        method: 'get'
    })
}

// 获取树形选择数据
export function getCategoryTreeSelect(rootId) {
    return request({
        url: `/manage/category/tree-select/${rootId}`,
        method: 'get'
    })
}

// 新增根分类
export function addCategoryRoot(data) {
    return request({
        url: '/manage/category/root',
        // url: '/manage/category',
        method: 'post',
        data: data
    })
}

// 更新分类树
export function updateCategoryTree(rootId, data) {
    return request({
        url: `/manage/category/tree/${rootId}`,
        method: 'put',  // 使用 PUT 方法更符合 RESTful
        data: data
    })
}

// 更新单个分类
export function updateCategory(data) {
    return request({
        url: `/manage/category/${data.id}`,
        method: 'put',
        data: data
    })
}

// 删除分类
export function deleteCategory(id) {
    return request({
        url: `/manage/category/${id}`,
        method: 'delete'
    })
}

// 检查编码唯一性
export function checkCategoryCodeUnique(data) {
    return request({
        url: '/manage/category/check-code',
        method: 'get',
        params: data
    })
}

// 获取分类列表（分页）
export function getCategoryList(query) {
    return request({
        url: '/manage/category/list',
        method: 'get',
        params: query
    })
}

// 导出分类数据
export function exportCategory(data) {
    return request({
        url: '/manage/category/export',
        method: 'post',
        data: data,
        responseType: 'blob'
    })
}

export function getCategoryTreeByCode(categoryCode) {
    return request({
        url: '/manage/category/by-code/' + categoryCode,
        method: 'get'
    })
}

export function getCategoryByCode(code)
{
    return request({
        url: '/manage/category/by-code/subclass/' + code,
        method: 'get'
    })
}

export function getCategoryByCodeWithRoot(params)
{
    return request({
        url: '/manage/category/getByCodeWithRoot',
        method: 'get',
        params: params
    })
}

// 复制树分类
export function copyCategoryTree(data) {
    return request({
        url: `/manage/category/copy/${data}`,
        method: 'post'
    })
}
