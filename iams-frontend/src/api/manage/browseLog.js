import request from '@/utils/request'

// 添加浏览日志
// 修改 API 调用函数，确保字段映射正确
// 在 @/api/manage/browseLog.js 中添加以下函数
export function addBrowseLog(data) {
    return request({
        url: '/manage/browseLog',
        method: 'post',
        data: data
    })
}

// 更新浏览日志
export function updateBrowseLog(id, data) {
    return request({
        url: `/manage/browseLog/${id}`,
        method: 'put',
        data: data
    })
}

// 获取文档的浏览日志列表
export function listBrowseLogByDocumentId(params) {
    return request({
        url: `/manage/browseLog/document/${params.documentId}`,
        method: 'get',
        params: {
            pageNum: params.pageNum,
            pageSize: params.pageSize
        }
    })
}

// 获取档案的浏览日志列表
export function listBrowseLogByArchive(archiveId) {
    return request({
        url: `/manage/browseLog/archive/${archiveId}`,
        method: 'get'
    })
}

// 获取用户的浏览日志列表
export function listBrowseLogByViewer(viewerId) {
    return request({
        url: `/manage/browseLog/viewer/${viewerId}`,
        method: 'get'
    })
}

// 删除浏览日志
export function deleteBrowseLog(id) {
    return request({
        url: `/manage/browseLog/${id}`,
        method: 'delete'
    })
}

// 批量删除浏览日志
export function batchDeleteBrowseLog(ids) {
    return request({
        url: `/manage/browseLog/batch/${ids}`,
        method: 'delete'
    })
}

// 获取档案的浏览日志
export function listBrowseLogByArchiveId(params) {
    return request({
        url: '/manage/browseLog/archive/' + params.archiveId,
        method: 'get',
        params: {
            pageNum: params.pageNum,
            pageSize: params.pageSize
        }
    })
}