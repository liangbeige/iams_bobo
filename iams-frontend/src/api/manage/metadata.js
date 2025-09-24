import request from '@/utils/request'

// 查询元数据列表
export function listMetadata(query) {
    return request({
        url: '/manage/document/metadata/list',
        method: 'get',
        params: query
    })
}

// 查询元数据详细
export function getMetadata(id) {
    return request({
        url: '/manage/document/metadata/' + id,
        method: 'get'
    })
}

// 新增元数据
export function addMetadata(data) {
    return request({
        url: '/manage/document/metadata',
        method: 'post',
        data: data
    })
}

// 修改元数据
export function updateMetadata(data) {
    return request({
        url: '/manage/document/metadata',
        method: 'put',
        data: data
    })
}

// 删除元数据
export function delMetadata(id) {
    return request({
        url: '/manage/document/metadata/' + id,
        method: 'delete'
    })
}

// 复制元数据
export function copyMetadata(id) {
    return request({
        url: '/manage/document/metadata/copy/' + id,
        method: 'post'
    })
}

// 应用元数据
export function applyMetadata(data) {
    return request({
        url: '/manage/document/metadata/apply',
        method: 'post',
        data: data
    })
}

// 查询所有元数据（不分页）
export function listMetadataAll() {
    return request({
        url: '/manage/document/metadata/list-all',
        method: 'get'
    })
}