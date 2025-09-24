import request from '@/utils/request'


export function listPendingEvaluation() {
    return request({
        url: '/task/pendingEvaluation/list',
        method: 'get'
    })
}


export function exportPendingEvaluation() {
    return request({
        url: '/task/pendingEvaluation/export',
        method: 'post'
    })
}